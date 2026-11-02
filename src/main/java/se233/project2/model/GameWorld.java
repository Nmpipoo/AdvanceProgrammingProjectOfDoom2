package se233.project2.model;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se233.project2.controller.InputHandler;
import se233.project2.exception.GameException;
import se233.project2.model.brick.Brick;
import se233.project2.model.brick.BrickLayout;
import se233.project2.model.brick.BrickType;
import se233.project2.model.upgrade.Upgrade;
import se233.project2.model.upgrade.UpgradeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// All game state. The game loop thread updates it and the JavaFX thread draws it,
// so both sides lock on this object while they touch it.
public class GameWorld {

    public static final double WIDTH = 800;
    public static final double HEIGHT = 640;
    public static final double HUD_HEIGHT = 40;
    public static final int STARTING_LIVES = PlayerStats.STARTING_LIVES;
    public static final double BANNER_SECONDS = 1.5;
    // Bonus bricks always drop the laser capsule; other bricks sometimes do.
    public static final double LASER_DROP_CHANCE = 0.06;
    // Directions the extra balls launch in, after the main ball's.
    private static final double[] EXTRA_BALL_ANGLES = {Math.toRadians(-30), 0};

    private static final List<KeyCode> CHOICE_KEYS = List.of(KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3);
    private static final List<KeyCode> NUMPAD_KEYS = List.of(KeyCode.NUMPAD1, KeyCode.NUMPAD2, KeyCode.NUMPAD3);

    private static final Logger logger = LogManager.getLogger(GameWorld.class);

    private final List<String[]> stages;
    private final ScoreManager scoreManager = new ScoreManager();
    private final UpgradeManager upgradeManager;
    private final Physics physics = new Physics();
    private final Laser laser = new Laser();
    private final Effects effects = new Effects();
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final Random random;

    private GameState state = GameState.MENU;
    private PlayerStats stats = new PlayerStats();
    private int stage;
    private boolean debugVisible;
    private String bannerTitle = "";
    private String bannerSubtitle = "";
    private double bannerTime;

    private Paddle paddle = new Paddle();
    private final List<Ball> balls = new ArrayList<>(List.of(new Ball()));
    private List<Brick> bricks = new ArrayList<>();
    private List<Upgrade> upgradeChoices = List.of();
    private int selectedChoice;

    public GameWorld() {
        this(StageLoader.loadAll(), new Random());
    }

    public GameWorld(List<String[]> stages) {
        this(stages, new Random());
    }

    public GameWorld(List<String[]> stages, Random random) {
        if (stages.isEmpty()) {
            throw new GameException("The game needs at least one stage");
        }
        this.stages = stages;
        this.upgradeManager = new UpgradeManager(random);
        this.random = random;
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
                    setState(GameState.PAUSED);
                } else if (debugVisible && input.consumePress(KeyCode.N)) {
                    skipStage();
                } else {
                    updatePlay(dt, input);
                }
            }
            case PAUSED -> {
                if (input.consumePress(KeyCode.P) || input.consumePress(KeyCode.ESCAPE)) {
                    setState(GameState.PLAYING);
                } else if (input.consumePress(KeyCode.Q)) {
                    logger.info("Player quit to menu with score {}", getScore());
                    setState(GameState.MENU);
                }
            }
            case CHOOSE_UPGRADE -> updateUpgradeChoice(input);
            case GAME_OVER, VICTORY -> {
                if (input.consumePress(KeyCode.ENTER)) {
                    startNewGame();
                } else if (input.consumePress(KeyCode.ESCAPE)) {
                    setState(GameState.MENU);
                }
            }
        }
        input.clearPresses();
    }

    private void updatePlay(double dt, InputHandler input) {
        bannerTime = Math.max(0, bannerTime - dt);

        int direction = 0;
        if (input.isHeld(KeyCode.LEFT) || input.isHeld(KeyCode.A)) {
            direction--;
        }
        if (input.isHeld(KeyCode.RIGHT) || input.isHeld(KeyCode.D)) {
            direction++;
        }
        paddle.move(direction, dt);

        Ball first = balls.get(0);
        if (!first.isLaunched()) {
            first.followPaddle(paddle);
            if (input.consumePress(KeyCode.SPACE)) {
                launchBalls();
            }
        } else {
            for (Ball ball : balls) {
                Physics.Result result = physics.step(ball, paddle, bricks, dt);
                if (result.paddleHit()) {
                    scoreManager.resetCombo();
                    ball.setPierceLeft(stats.getPierce());
                    logger.debug("Ball hit paddle at offset {}", Math.round(ball.getCenterX() - paddle.getCenterX()));
                }
                for (Brick brick : result.bricksHit()) {
                    onBrickHit(brick);
                }
            }
            balls.removeIf(ball -> ball.getY() > HEIGHT);
            if (balls.isEmpty()) {
                loseLife();
            }
        }

        // Hold SPACE to fire while the laser is active (once the ball is in play).
        boolean firing = balls.get(0).isLaunched() && input.isHeld(KeyCode.SPACE);
        boolean wasActive = laser.isActive();
        for (Brick brick : laser.update(dt, paddle, firing, bricks)) {
            onBrickHit(brick);
        }
        if (wasActive && !laser.isActive()) {
            logger.info("Laser wore off");
        }
        updatePowerUps(dt);

        for (Ball ball : balls) {
            ball.tick(dt);
        }
        for (Brick brick : bricks) {
            brick.tick(dt);
        }
        effects.update(dt);

        bricks.removeIf(Brick::isDestroyed);
        if (state == GameState.PLAYING && isStageCleared()) {
            onStageCleared();
        }
    }

    private void launchBalls() {
        Ball first = balls.get(0);
        first.launch();
        first.setPierceLeft(stats.getPierce());
        for (int i = 0; i < stats.getExtraBalls(); i++) {
            Ball extra = new Ball(stats.getBallSpeed());
            extra.followPaddle(paddle);
            extra.launch(EXTRA_BALL_ANGLES[i % EXTRA_BALL_ANGLES.length]);
            extra.setPierceLeft(stats.getPierce());
            balls.add(extra);
        }
        logger.debug("Player launched {} ball(s) at x={}", balls.size(), Math.round(first.getCenterX()));
    }

    private void updatePowerUps(double dt) {
        powerUps.removeIf(powerUp -> {
            powerUp.fall(dt);
            if (powerUp.intersects(paddle)) {
                laser.activate();
                effects.floatText("LASER!", paddle.getCenterX(), paddle.getY() - 20);
                logger.info("Player caught the laser power-up ({}s)", (int) Laser.DURATION);
                return true;
            }
            return powerUp.getY() > HEIGHT;
        });
    }

    private void onBrickHit(Brick brick) {
        if (brick.isDestroyed()) {
            int awarded = scoreManager.brickDestroyed(brick.getPoints());
            effects.explode(brick.getCenterX(), brick.getCenterY());
            effects.floatText("+" + awarded, brick.getCenterX(), brick.getCenterY());
            logger.info("{} brick destroyed: +{} (combo x{}, score {})",
                    brick.getType(), awarded, scoreManager.getCombo(), getScore());
            if (brick.getType() == BrickType.BONUS || random.nextDouble() < LASER_DROP_CHANCE) {
                powerUps.add(new PowerUp(brick.getCenterX(), brick.getCenterY()));
                logger.debug("Laser power-up dropped at x={}", Math.round(brick.getCenterX()));
            }
        } else if (brick.isBreakable()) {
            logger.debug("{} brick hit, {} hits left", brick.getType(), brick.getHitPoints());
        } else {
            logger.debug("Ball bounced off an unbreakable brick");
        }
    }

    private void loseLife() {
        stats.loseLife();
        scoreManager.resetCombo();
        resetBalls();
        laser.reset();
        powerUps.clear();
        logger.warn("Ball lost, {} lives left", stats.getLives());
        if (stats.getLives() <= 0) {
            logger.info("Game over on stage {} with score {}", stage, getScore());
            setState(GameState.GAME_OVER);
        } else {
            showBanner("LIFE LOST", "");
        }
    }

    private void onStageCleared() {
        resetBalls();
        scoreManager.resetCombo();
        logger.info("Stage {} cleared with score {}", stage, getScore());
        if (stage == stages.size()) {
            logger.info("All {} stages cleared, final score {}", stages.size(), getScore());
            setState(GameState.VICTORY);
            return;
        }
        upgradeChoices = upgradeManager.rollChoices(stats);
        selectedChoice = 0;
        if (upgradeChoices.isEmpty()) {
            nextStage("");
        } else {
            setState(GameState.CHOOSE_UPGRADE);
        }
    }

    private void updateUpgradeChoice(InputHandler input) {
        int count = upgradeChoices.size();
        if (input.consumePress(KeyCode.LEFT) || input.consumePress(KeyCode.A)) {
            selectedChoice = (selectedChoice + count - 1) % count;
        }
        if (input.consumePress(KeyCode.RIGHT) || input.consumePress(KeyCode.D)) {
            selectedChoice = (selectedChoice + 1) % count;
        }
        int picked = -1;
        for (int i = 0; i < count; i++) {
            if (input.consumePress(CHOICE_KEYS.get(i)) || input.consumePress(NUMPAD_KEYS.get(i))) {
                picked = i;
            }
        }
        if (picked < 0 && (input.consumePress(KeyCode.ENTER) || input.consumePress(KeyCode.SPACE))) {
            picked = selectedChoice;
        }
        // Hovering a card highlights it; clicking one picks it.
        Point2D mouse = input.consumeMouseMove();
        if (mouse != null) {
            int hovered = CardLayout.cardAt(mouse.getX(), mouse.getY(), count);
            if (hovered >= 0) {
                selectedChoice = hovered;
            }
        }
        Point2D click = input.consumeClick();
        if (picked < 0 && click != null) {
            picked = CardLayout.cardAt(click.getX(), click.getY(), count);
        }
        if (picked >= 0) {
            Upgrade upgrade = upgradeChoices.get(picked);
            upgradeManager.apply(upgrade, stats);
            nextStage("+ " + upgrade.getName());
        }
    }

    // Demo shortcut (F3 then N): clear the stage without scoring, to show later stages quickly.
    private void skipStage() {
        logger.warn("Debug: stage {} skipped", stage);
        bricks.removeIf(Brick::isBreakable);
        onStageCleared();
    }

    // Unbreakable bricks don't have to be cleared.
    public boolean isStageCleared() {
        return bricks.stream().noneMatch(Brick::isBreakable);
    }

    private void startNewGame() {
        stats = new PlayerStats();
        scoreManager.reset();
        stage = 1;
        paddle = new Paddle();
        loadStage("");
        logger.info("New game started: lives={}, stages={}", stats.getLives(), stages.size());
        setState(GameState.PLAYING);
    }

    private void nextStage(String upgradeText) {
        stage++;
        paddle.recenter();
        loadStage(upgradeText);
        setState(GameState.PLAYING);
    }

    private void loadStage(String subtitle) {
        bricks = BrickLayout.fromRows(stages.get(stage - 1));
        paddle.applyStats(stats);
        scoreManager.setMultiplier(stats.getScoreMultiplier());
        resetBalls();
        laser.reset();
        powerUps.clear();
        effects.clear();
        showBanner("STAGE " + stage, subtitle);
        logger.info("Stage {} loaded with {} bricks", stage, bricks.size());
    }

    // Back to a single ball resting on the paddle.
    private void resetBalls() {
        balls.clear();
        balls.add(new Ball(stats.getBallSpeed()));
        balls.get(0).followPaddle(paddle);
    }

    private void showBanner(String title, String subtitle) {
        bannerTitle = title;
        bannerSubtitle = subtitle;
        bannerTime = BANNER_SECONDS;
    }

    private void setState(GameState next) {
        logger.debug("State {} -> {}", state, next);
        state = next;
    }

    public GameState getState() {
        return state;
    }

    public int getScore() {
        return scoreManager.getScore();
    }

    public int getCombo() {
        return scoreManager.getCombo();
    }

    public int getLives() {
        return stats.getLives();
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

    public String getBanner() {
        return bannerTitle;
    }

    public String getBannerSubtitle() {
        return bannerSubtitle;
    }

    public double getBannerTime() {
        return bannerTime;
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

    public Effects getEffects() {
        return effects;
    }

    public List<PowerUp> getPowerUps() {
        return Collections.unmodifiableList(powerUps);
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
