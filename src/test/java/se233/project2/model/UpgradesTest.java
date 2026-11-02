package se233.project2.model;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameLoop;
import se233.project2.controller.InputHandler;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Rubric: unit tests for upgrade behavior.
class UpgradesTest {

    private final PlayerStats stats = new PlayerStats();

    // A started game with its first stage just cleared, waiting for an upgrade pick.
    private GameWorld worldAtUpgradeChoice(InputHandler input) {
        GameWorld world = new GameWorld(List.of(new String[]{"N"}, new String[]{"N"}));
        input.press(KeyCode.ENTER);
        world.update(GameLoop.STEP, input);
        input.release(KeyCode.ENTER);
        world.getBricks().forEach(Brick::hit);
        world.update(GameLoop.STEP, input);
        return world;
    }

    private static double cardCenterX(int index) {
        return GameWorld.cardLeft(index, 3) + GameWorld.CARD_WIDTH / 2;
    }

    @Test
    void clearingAStageOffersThreeDifferentUpgradesAndThePickCarriesIntoTheNextStage() {
        InputHandler input = new InputHandler();
        GameWorld world = worldAtUpgradeChoice(input);
        assertEquals(GameWorld.State.CHOOSE_UPGRADE, world.getState());
        List<Upgrade> choices = world.getUpgradeChoices();
        assertEquals(3, choices.stream().distinct().count());

        input.click(cardCenterX(1), GameWorld.CARD_TOP + 10);
        world.update(GameLoop.STEP, input);
        assertEquals(2, world.getStage());
        assertEquals(List.of(choices.get(1).getName()), world.getStats().getUpgradesTaken());
        assertEquals(world.getStats().paddleWidth, world.getPaddle().getWidth(), 1e-9);
        assertEquals(world.getStats().ballSpeed, world.getBall().getSpeed(), 1e-9);
    }

    @Test
    void hoveringACardHighlightsItAndClickingPicksIt() {
        InputHandler input = new InputHandler();
        GameWorld world = worldAtUpgradeChoice(input);
        double cardY = GameWorld.CARD_TOP + GameWorld.CARD_HEIGHT / 2;

        input.moveMouse(cardCenterX(2), cardY);
        world.update(GameLoop.STEP, input);
        assertEquals(2, world.getSelectedChoice());
        assertEquals(GameWorld.State.CHOOSE_UPGRADE, world.getState());

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
        input.click(cardCenterX(0), GameWorld.CARD_TOP - 20);
        world.update(GameLoop.STEP, input);
        input.click(GameWorld.cardLeft(1, 3) - GameWorld.CARD_GAP / 2, GameWorld.CARD_TOP + 10); // between two cards
        world.update(GameLoop.STEP, input);
        assertEquals(GameWorld.State.CHOOSE_UPGRADE, world.getState());
        assertEquals(-1, world.getSelectedChoice(), "no card is highlighted");
    }

    @Test
    void paddleUpgradesMakeThePaddleWiderAndFaster() {
        Upgrade.WIDE_PADDLE.apply(stats);
        Upgrade.QUICK_PADDLE.apply(stats);
        assertEquals(Paddle.DEFAULT_WIDTH * 1.25, stats.paddleWidth, 1e-9);
        assertEquals(Paddle.DEFAULT_SPEED * 1.2, stats.paddleSpeed, 1e-9);
        assertEquals(List.of("Wide Paddle", "Quick Paddle"), stats.getUpgradesTaken());
    }

    @Test
    void ballSpeedUpgradesChangeTheBallSpeed() {
        Upgrade.SLOW_BALL.apply(stats);
        assertEquals(Ball.DEFAULT_SPEED * 0.85, stats.ballSpeed, 1e-9);
        PlayerStats fast = new PlayerStats();
        Upgrade.FAST_BALL.apply(fast);
        assertEquals(Ball.DEFAULT_SPEED * 1.15, fast.ballSpeed, 1e-9);
        assertEquals(1.25, fast.scoreMultiplier, 1e-9);
    }

    @Test
    void extraBallExtraLifePiercingAndScoreBoostApplyTheirEffects() {
        Upgrade.EXTRA_BALL.apply(stats);
        Upgrade.EXTRA_LIFE.apply(stats);
        Upgrade.PIERCING_BALL.apply(stats);
        Upgrade.SCORE_BOOST.apply(stats);
        assertEquals(1, stats.extraBalls);
        assertEquals(PlayerStats.STARTING_LIVES + 1, stats.getLives());
        assertEquals(2, stats.pierce);
        assertEquals(1.5, stats.scoreMultiplier, 1e-9);
    }

    @Test
    void piercingBallSmashesThroughAStrongBrickWithoutBouncing() {
        Paddle paddle = new Paddle();
        Ball ball = new Ball();
        Brick brick = new Brick(Brick.Type.STRONG, 300, 200);
        ball.launch();
        ball.setPierceLeft(1);
        ball.x = brick.getCenterX() - Ball.RADIUS - 2;
        ball.y = brick.getY() + brick.getHeight() + 5;
        for (int i = 0; i < 600 && !brick.isDestroyed(); i++) {
            ball.step(paddle, List.of(brick), GameLoop.STEP);
        }
        assertTrue(brick.isDestroyed(), "a 3-hit brick broke in one pass");
        assertTrue(ball.getVy() < 0, "the ball kept flying up");
        assertEquals(0, ball.getPierceLeft());
    }

    @Test
    void everyUpgradeStopsAtItsCap() {
        for (Upgrade upgrade : Upgrade.values()) {
            PlayerStats fresh = new PlayerStats();
            for (int i = 0; i < 50 && upgrade.canApply(fresh); i++) {
                upgrade.apply(fresh);
            }
            assertFalse(upgrade.canApply(fresh), upgrade.getName() + " never reached its cap");
        }
    }

    @Test
    void maxedOutUpgradesAreNoLongerOffered() {
        stats.paddleWidth = 200;
        stats.extraBalls = 2;
        Random random = new Random(3);
        for (int i = 0; i < 30; i++) {
            List<Upgrade> choices = Upgrade.roll(stats, random);
            assertEquals(3, choices.size());
            assertFalse(choices.contains(Upgrade.WIDE_PADDLE));
            assertFalse(choices.contains(Upgrade.EXTRA_BALL));
        }
    }
}
