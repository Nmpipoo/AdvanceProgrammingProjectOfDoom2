package se233.project2.model;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameLoop;
import se233.project2.controller.InputHandler;
import se233.project2.model.brick.Brick;
import se233.project2.model.brick.StrongBrick;
import se233.project2.model.upgrade.ExtraBall;
import se233.project2.model.upgrade.ExtraLife;
import se233.project2.model.upgrade.FastBall;
import se233.project2.model.upgrade.PiercingBall;
import se233.project2.model.upgrade.QuickPaddle;
import se233.project2.model.upgrade.ScoreBoost;
import se233.project2.model.upgrade.SlowBall;
import se233.project2.model.upgrade.Upgrade;
import se233.project2.model.upgrade.UpgradeManager;
import se233.project2.model.upgrade.WidePaddle;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Rubric: unit tests for upgrade behavior.
class UpgradesTest {

    private final PlayerStats stats = new PlayerStats();

    @Test
    void clearingAStageOffersThreeDifferentUpgradesAndThePickCarriesIntoTheNextStage() {
        GameWorld world = new GameWorld(List.of(new String[]{"N"}, new String[]{"N"}));
        InputHandler input = new InputHandler();
        input.press(KeyCode.ENTER);
        world.update(GameLoop.STEP, input);
        input.release(KeyCode.ENTER);

        world.getBricks().forEach(brick -> brick.hit());
        world.update(GameLoop.STEP, input);
        assertEquals(GameState.CHOOSE_UPGRADE, world.getState());
        List<Upgrade> choices = world.getUpgradeChoices();
        assertEquals(3, choices.stream().map(Upgrade::getName).distinct().count());

        input.press(KeyCode.DIGIT2);
        world.update(GameLoop.STEP, input);
        assertEquals(2, world.getStage());
        assertEquals(List.of(choices.get(1).getName()), world.getStats().getUpgradesTaken());
        assertEquals(world.getStats().getPaddleWidth(), world.getPaddle().getWidth(), 1e-9);
        assertEquals(world.getStats().getBallSpeed(), world.getBall().getSpeed(), 1e-9);
    }

    // A started game with its first stage just cleared, waiting for an upgrade pick.
    private GameWorld worldAtUpgradeChoice(InputHandler input) {
        GameWorld world = new GameWorld(List.of(new String[]{"N"}, new String[]{"N"}));
        input.press(KeyCode.ENTER);
        world.update(GameLoop.STEP, input);
        input.release(KeyCode.ENTER);
        world.getBricks().forEach(brick -> brick.hit());
        world.update(GameLoop.STEP, input);
        return world;
    }

    private static double cardCenterX(int index) {
        return CardLayout.left(index, 3) + CardLayout.WIDTH / 2;
    }

    @Test
    void hoveringACardHighlightsItAndClickingPicksIt() {
        InputHandler input = new InputHandler();
        GameWorld world = worldAtUpgradeChoice(input);
        double cardY = CardLayout.TOP + CardLayout.HEIGHT / 2;

        input.moveMouse(cardCenterX(2), cardY);
        world.update(GameLoop.STEP, input);
        assertEquals(2, world.getSelectedChoice());
        assertEquals(GameState.CHOOSE_UPGRADE, world.getState());

        Upgrade second = world.getUpgradeChoices().get(1);
        input.click(cardCenterX(1), cardY);
        world.update(GameLoop.STEP, input);
        assertEquals(2, world.getStage());
        assertEquals(List.of(second.getName()), world.getStats().getUpgradesTaken());
    }

    @Test
    void clickingOutsideTheCardsPicksNothing() {
        InputHandler input = new InputHandler();
        GameWorld world = worldAtUpgradeChoice(input);
        input.click(cardCenterX(0), CardLayout.TOP - 20);
        world.update(GameLoop.STEP, input);
        input.click(CardLayout.left(1, 3) - CardLayout.GAP / 2, CardLayout.TOP + 10); // between two cards
        world.update(GameLoop.STEP, input);
        assertEquals(GameState.CHOOSE_UPGRADE, world.getState());
        assertEquals(0, world.getSelectedChoice());
    }

    @Test
    void paddleUpgradesMakeThePaddleWiderAndFaster() {
        new WidePaddle().apply(stats);
        new QuickPaddle().apply(stats);
        assertEquals(Paddle.DEFAULT_WIDTH * WidePaddle.FACTOR, stats.getPaddleWidth(), 1e-9);
        assertEquals(Paddle.DEFAULT_SPEED * QuickPaddle.FACTOR, stats.getPaddleSpeed(), 1e-9);
    }

    @Test
    void ballSpeedUpgradesChangeTheBallSpeed() {
        new SlowBall().apply(stats);
        assertEquals(Ball.DEFAULT_SPEED * SlowBall.FACTOR, stats.getBallSpeed(), 1e-9);
        PlayerStats fast = new PlayerStats();
        new FastBall().apply(fast);
        assertEquals(Ball.DEFAULT_SPEED * FastBall.FACTOR, fast.getBallSpeed(), 1e-9);
        assertEquals(1 + FastBall.SCORE_BONUS, fast.getScoreMultiplier(), 1e-9);
    }

    @Test
    void extraBallExtraLifePiercingAndScoreBoostApplyTheirEffects() {
        new ExtraBall().apply(stats);
        new ExtraLife().apply(stats);
        new PiercingBall().apply(stats);
        new ScoreBoost().apply(stats);
        assertEquals(1, stats.getExtraBalls());
        assertEquals(PlayerStats.STARTING_LIVES + 1, stats.getLives());
        assertEquals(PiercingBall.PIERCE_PER_PICK, stats.getPierce());
        assertEquals(1 + ScoreBoost.BONUS, stats.getScoreMultiplier(), 1e-9);
    }

    @Test
    void piercingBallSmashesThroughAStrongBrickWithoutBouncing() {
        Paddle paddle = new Paddle();
        Ball ball = new Ball();
        Brick brick = new StrongBrick(300, 200);
        ball.launch();
        ball.setPierceLeft(1);
        ball.x = brick.getCenterX() - Ball.RADIUS - 2;
        ball.y = brick.getY() + brick.getHeight() + 5;
        Physics physics = new Physics();
        for (int i = 0; i < 600 && !brick.isDestroyed(); i++) {
            physics.step(ball, paddle, List.of(brick), GameLoop.STEP);
        }
        assertTrue(brick.isDestroyed(), "a 3-hit brick broke in one pass");
        assertTrue(ball.getVy() < 0, "the ball kept flying up");
        assertEquals(0, ball.getPierceLeft());
    }

    @Test
    void everyUpgradeStopsAtItsCap() {
        for (Upgrade upgrade : new UpgradeManager(new Random(1)).getPool()) {
            PlayerStats fresh = new PlayerStats();
            for (int i = 0; i < 50 && upgrade.canApply(fresh); i++) {
                upgrade.apply(fresh);
            }
            assertFalse(upgrade.canApply(fresh), upgrade.getName() + " never reached its cap");
        }
    }

    @Test
    void maxedOutUpgradesAreNoLongerOffered() {
        UpgradeManager manager = new UpgradeManager(new Random(3));
        stats.setPaddleWidth(WidePaddle.MAX_WIDTH);
        stats.setExtraBalls(ExtraBall.MAX_EXTRA);
        for (int i = 0; i < 30; i++) {
            for (Upgrade upgrade : manager.rollChoices(stats)) {
                assertFalse(upgrade instanceof WidePaddle);
                assertFalse(upgrade instanceof ExtraBall);
            }
        }
    }
}
