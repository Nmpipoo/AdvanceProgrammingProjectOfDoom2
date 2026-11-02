package se233.project2.model;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se233.project2.controller.InputHandler;
import se233.project2.exception.GameException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// All game state and rules. The game loop thread updates it and the JavaFX thread draws it,
// so both sides lock on this object while they touch it.
public class GameWorld {

    public enum State { MENU, PLAYING, PAUSED, CHOOSE_UPGRADE, GAME_OVER, VICTORY }

    // Short-lived feedback where something happened: "+50" rising from a broken brick
    // (with an explosion), or "LASER!" over the paddle.
    public record Popup(String text, double x, double y, double age, boolean explosion) {
    }

    public static final double WIDTH = 800;
    public static final double HEIGHT = 640;
    public static final double HUD_HEIGHT = 40;
    public static final double EXPLOSION_SECONDS = 0.36;
    public static final double POPUP_SECONDS = 0.8;
    // Bonus bricks always drop the laser capsule; other bricks sometimes do.
    public static final double LASER_DROP_CHANCE = 0.06;
    // Directions the extra balls launch in, after the main ball's.
    private static final double[] EXTRA_BALL_ANGLES = {Math.toRadians(-30), 0};

    // Upgrade cards, centered in a row.
    public static final double CARD_WIDTH = 220;
    public static final double CARD_HEIGHT = 150;
    public static final double CARD_GAP = 30;
    public static final double CARD_TOP = 240;

    private static final String STAGE_PATH = "/se233/project2/stages/stage%d.txt";
    private static final Logger logger = LogManager.getLogger(GameWorld.class);

    private final List<String[]> stages;
    private final Random random;
    private final Laser laser = new Laser();
    private final List<Laser.Capsule> capsules = new ArrayList<>();
    private final List<Popup> popups = new ArrayList<>();
    private final List<Ball> balls = new ArrayList<>(List.of(new Ball()));
    private List<Brick> bricks = new ArrayList<>();
    private Paddle paddle = new Paddle();
    private PlayerStats stats = new PlayerStats();

    private State state = State.MENU;
    private int stage;
    private boolean debugVisible;
    private List<Upgrade> upgradeChoices = List.of();
    private int selectedChoice;

    public GameWorld() {
        this(loadStages(), new Random());
    }

    public GameWorld(List<String[]> stages) {
        this(stages, new Random());
    }

    public GameWorld(List<String[]> stages, Random random) {
        if (stages.isEmpty()) {
            throw new GameException("The game needs at least one stage");
        }
        this.stages = stages;
        this.random = random;
    }

    // Reads stage1.txt, stage2.txt, ... from resources until one is missing. Each line is a row
    // of brick letters; lines starting with # are comments. Every stage is checked now, so a
    // broken file is reported at start-up rather than halfway through a game.
    public static List<String[]> loadStages() {
        List<String[]> stages = new ArrayList<>();
        for (int number = 1; ; number++) {
            String path = String.format(STAGE_PATH, number);
            try (InputStream in = GameWorld.class.getResourceAsStream(path)) {
                if (in == null) {
                    break;
                }
                String[] rows = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines()
                        .map(String::stripTrailing)
                        .filter(line -> !line.startsWith("#"))
                        .toArray(String[]::new);
                List<Brick> bricks;
                try {
                    bricks = Brick.fromRows(rows);
                } catch (GameException e) {
                    throw new GameException("stage" + number + ": " + e.getMessage(), e);
                }
                if (bricks.stream().noneMatch(Brick::isBreakable)) {
                    throw new GameException("stage" + number + " has no breakable bricks");
                }
                stages.add(rows);
            } catch (IOException e) {
                throw new GameException("Could not read " + path, e);
            }
        }
        if (stages.isEmpty()) {
            throw new GameException("No stage files found under " + String.format(STAGE_PATH, 1));
        }
        logger.info("Loaded {} stages", stages.size());
        return stages;
    }

    // Left edge of upgrade card `index` out of `count`.
    public static double cardLeft(int index, int count) {
        return (WIDTH - count * CARD_WIDTH - (count - 1) * CARD_GAP) / 2 + index * (CARD_WIDTH + CARD_GAP);
    }

    // The upgrade card under (x, y), or -1.
    public static int cardAt(double x, double y, int count) {
        for (int i = 0; i < count; i++) {
            if (x >= cardLeft(i, count) && x <= cardLeft(i, count) + CARD_WIDTH && y >= CARD_TOP && y <= CARD_TOP + CARD_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    public void update(double dt, InputHandler input) {
        if (input.consumePress(KeyCode.F3)) {
            debugVisible = !debugVisible;
        }
        switch (state) {
            case MENU -> {
                if (input.consumePress(KeyCode.ENTER)) {
                    startNewGame();
                }
            }
            case PLAYING -> {
                if (input.consumePress(KeyCode.P) || input.consumePress(KeyCode.ESCAPE)) {
                    setState(State.PAUSED);
                } else if (debugVisible && input.consumePress(KeyCode.N)) {
                    // Demo shortcut (F3 then N): clear the stage without scoring.
                    logger.warn("Debug: stage {} skipped", stage);
                    bricks.removeIf(Brick::isBreakable);
                    onStageCleared();
                } else {
                    updatePlay(dt, input);
                }
            }
            case PAUSED -> {
                if (input.consumePress(KeyCode.P) || input.consumePress(KeyCode.ESCAPE)) {
                    setState(State.PLAYING);
                } else if (input.consumePress(KeyCode.Q)) {
                    logger.info("Player quit to menu with score {}", getScore());
                    setState(State.MENU);
                }
            }
            case CHOOSE_UPGRADE -> updateUpgradeChoice(input);
            case GAME_OVER, VICTORY -> {
                if (input.consumePress(KeyCode.ENTER)) {
                    startNewGame();
                } else if (input.consumePress(KeyCode.ESCAPE)) {
                    setState(State.MENU);
                }
            }
        }
        input.clearPresses();
    }

    private void updatePlay(double dt, InputHandler input) {
        int direction = 0;
        if (input.isHeld(KeyCode.LEFT) || input.isHeld(KeyCode.A)) {
            direction--;
        }
        if (input.isHeld(KeyCode.RIGHT) || input.isHeld(KeyCode.D)) {
            direction++;
        }
        paddle.move(direction, dt);

        if (!balls.get(0).isLaunched()) {
            balls.get(0).followPaddle(paddle);
            if (input.consumePress(KeyCode.SPACE)) {
                launchBalls();
            }
        } else {
            for (Ball ball : balls) {
                Ball.Hits hits = ball.step(paddle, bricks, dt);
                if (hits.paddle()) {
                    stats.resetCombo();
                    ball.setPierceLeft(stats.pierce);
                    logger.debug("Ball hit paddle at offset {}", Math.round(ball.getCenterX() - paddle.getCenterX()));
                }
                hits.bricks().forEach(this::onBrickHit);
            }
            balls.removeIf(ball -> ball.getY() > HEIGHT);
            if (balls.isEmpty()) {
                loseLife();
            }
        }

        // Hold SPACE to fire while the laser is active (once the ball is in play).
        boolean firing = balls.get(0).isLaunched() && input.isHeld(KeyCode.SPACE);
        boolean wasActive = laser.isActive();
        laser.update(dt, paddle, firing, bricks).forEach(this::onBrickHit);
        if (wasActive && !laser.isActive()) {
            logger.info("Laser wore off");
        }

        capsules.removeIf(capsule -> {
            capsule.fall(dt);
            if (capsule.intersects(paddle)) {
                laser.activate();
                popups.add(new Popup("LASER!", paddle.getCenterX(), paddle.getY() - 20, 0, false));
                logger.info("Player caught the laser power-up ({}s)", (int) Laser.DURATION);
                return true;
            }
            return capsule.getY() > HEIGHT;
        });

        balls.forEach(ball -> ball.tick(dt));
        bricks.forEach(brick -> brick.tick(dt));
        popups.replaceAll(p -> new Popup(p.text(), p.x(), p.y(), p.age() + dt, p.explosion()));
        popups.removeIf(p -> p.age() >= POPUP_SECONDS);

        bricks.removeIf(Brick::isDestroyed);
        if (state == State.PLAYING && isStageCleared()) {
            onStageCleared();
        }
    }

    private void launchBalls() {
        Ball first = balls.get(0);
        first.launch();
        first.setPierceLeft(stats.pierce);
        for (int i = 0; i < stats.extraBalls; i++) {
            Ball extra = new Ball(stats.ballSpeed);
            extra.followPaddle(paddle);
            extra.launch(EXTRA_BALL_ANGLES[i % EXTRA_BALL_ANGLES.length]);
            extra.setPierceLeft(stats.pierce);
            balls.add(extra);
        }
        logger.debug("Player launched {} ball(s) at x={}", balls.size(), Math.round(first.getCenterX()));
    }

    private void onBrickHit(Brick brick) {
        if (brick.isDestroyed()) {
            int awarded = stats.addBrickPoints(brick.getPoints());
            popups.add(new Popup("+" + awarded, brick.getCenterX(), brick.getCenterY(), 0, true));
            logger.info("{} brick destroyed: +{} (combo x{}, score {})", brick.getType(), awarded, stats.combo, getScore());
            if (brick.getType() == Brick.Type.BONUS || random.nextDouble() < LASER_DROP_CHANCE) {
                capsules.add(new Laser.Capsule(brick.getCenterX(), brick.getCenterY()));
                logger.debug("Laser power-up dropped at x={}", Math.round(brick.getCenterX()));
            }
        } else if (brick.isBreakable()) {
            logger.debug("{} brick hit, {} hits left", brick.getType(), brick.getHitPoints());
        } else {
            logger.debug("Ball bounced off an unbreakable brick");
        }
    }

    private void loseLife() {
        stats.lives--;
        stats.resetCombo();
        resetBalls();
        laser.reset();
        capsules.clear();
        logger.warn("Ball lost, {} lives left", stats.lives);
        if (stats.lives <= 0) {
            logger.info("Game over on stage {} with score {}", stage, getScore());
            setState(State.GAME_OVER);
        }
    }

    private void onStageCleared() {
        resetBalls();
        laser.reset();
        stats.resetCombo();
        logger.info("Stage {} cleared with score {}", stage, getScore());
        if (stage == stages.size()) {
            logger.info("All {} stages cleared, final score {}", stages.size(), getScore());
            setState(State.VICTORY);
            return;
        }
        upgradeChoices = Upgrade.roll(stats, random);
        selectedChoice = -1;
        if (upgradeChoices.isEmpty()) {
            nextStage();
        } else {
            setState(State.CHOOSE_UPGRADE);
        }
    }

    // Hovering a card highlights it; clicking one picks it.
    private void updateUpgradeChoice(InputHandler input) {
        int count = upgradeChoices.size();
        Point2D mouse = input.consumeMouseMove();
        if (mouse != null) {
            selectedChoice = cardAt(mouse.getX(), mouse.getY(), count);
        }
        Point2D click = input.consumeClick();
        int picked = click == null ? -1 : cardAt(click.getX(), click.getY(), count);
        if (picked >= 0) {
            Upgrade upgrade = upgradeChoices.get(picked);
            upgrade.apply(stats);
            nextStage();
            popups.add(new Popup("+ " + upgrade.getName(), paddle.getCenterX(), paddle.getY() - 20, 0, false));
        }
    }

    // Unbreakable bricks don't have to be cleared.
    public boolean isStageCleared() {
        return bricks.stream().noneMatch(Brick::isBreakable);
    }

    private void startNewGame() {
        stats = new PlayerStats();
        stage = 0;
        paddle = new Paddle();
        nextStage();
        logger.info("New game started: lives={}, stages={}", stats.lives, stages.size());
    }

    // Builds the next stage with the player's current stats.
    private void nextStage() {
        stage++;
        paddle.recenter();
        paddle.applyStats(stats);
        bricks = Brick.fromRows(stages.get(stage - 1));
        resetBalls();
        laser.reset();
        capsules.clear();
        popups.clear();
        logger.info("Stage {} loaded with {} bricks", stage, bricks.size());
        setState(State.PLAYING);
    }

    // Back to a single ball resting on the paddle.
    private void resetBalls() {
        balls.clear();
        balls.add(new Ball(stats.ballSpeed));
        balls.get(0).followPaddle(paddle);
    }

    private void setState(State next) {
        logger.debug("State {} -> {}", state, next);
        state = next;
    }

    public State getState() {
        return state;
    }

    public int getScore() {
        return stats.score;
    }

    public int getCombo() {
        return stats.combo;
    }

    public int getLives() {
        return stats.lives;
    }

    public PlayerStats getStats() {
        return stats;
    }

    public int getStage() {
        return stage;
    }

    public int getStageCount() {
        return stages.size();
    }

    public boolean isDebugVisible() {
        return debugVisible;
    }

    public List<Upgrade> getUpgradeChoices() {
        return upgradeChoices;
    }

    public int getSelectedChoice() {
        return selectedChoice;
    }

    public Laser getLaser() {
        return laser;
    }

    public List<Laser.Capsule> getCapsules() {
        return Collections.unmodifiableList(capsules);
    }

    public List<Popup> getPopups() {
        return Collections.unmodifiableList(popups);
    }

    public Paddle getPaddle() {
        return paddle;
    }

    // The main ball (the one that rests on the paddle before launch).
    public Ball getBall() {
        return balls.get(0);
    }

    public List<Ball> getBalls() {
        return Collections.unmodifiableList(balls);
    }

    public List<Brick> getBricks() {
        return Collections.unmodifiableList(bricks);
    }
}
