package se233.project2.model;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameLoop;
import se233.project2.controller.InputHandler;
import se233.project2.exception.GameException;
import se233.project2.model.brick.Brick;
import se233.project2.model.upgrade.ExtraLife;
import se233.project2.model.upgrade.Upgrade;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivesAndStagesTest {

    private GameWorld world;
    private InputHandler input;

    @BeforeEach
    void setUp() {
        // Two tiny stages so tests can clear them by hand.
        world = new GameWorld(List.of(new String[]{"N"}, new String[]{"NU"}));
        input = new InputHandler();
        tap(KeyCode.ENTER);
    }

    private void tap(KeyCode key) {
        input.press(key);
        world.update(GameLoop.STEP, input);
        input.release(key);
    }

    private void dropBall() {
        tap(KeyCode.SPACE);
        world.getBall().y = GameWorld.HEIGHT + 10;
        world.update(GameLoop.STEP, input);
    }

    private void breakAllBricks() {
        for (Brick brick : world.getBricks()) {
            while (brick.isBreakable() && !brick.isDestroyed()) {
                brick.hit();
            }
        }
        world.update(GameLoop.STEP, input);
    }

    @Test
    void losingTheBallCostsALifeAndPutsItBackOnThePaddle() {
        dropBall();
        assertEquals(GameWorld.STARTING_LIVES - 1, world.getLives());
        assertEquals(GameState.PLAYING, world.getState());
        assertFalse(world.getBall().isLaunched());
        assertEquals("LIFE LOST", world.getBanner());
        assertTrue(world.getBannerTime() > 0);
    }

    @Test
    void losingTheLastLifeEndsTheGame() {
        for (int i = 0; i < GameWorld.STARTING_LIVES; i++) {
            dropBall();
        }
        assertEquals(0, world.getLives());
        assertEquals(GameState.GAME_OVER, world.getState());
    }

    @Test
    void enterAfterGameOverStartsAFreshGame() {
        for (int i = 0; i < GameWorld.STARTING_LIVES; i++) {
            dropBall();
        }
        tap(KeyCode.ENTER);
        assertEquals(GameState.PLAYING, world.getState());
        assertEquals(GameWorld.STARTING_LIVES, world.getLives());
        assertEquals(1, world.getStage());
        assertEquals(0, world.getScore());
    }

    @Test
    void escapeAfterGameOverGoesToMenu() {
        for (int i = 0; i < GameWorld.STARTING_LIVES; i++) {
            dropBall();
        }
        tap(KeyCode.ESCAPE);
        assertEquals(GameState.MENU, world.getState());
    }

    // Presses the number key of the first offered upgrade that isn't Extra Life.
    private Upgrade pickUpgradeOtherThanExtraLife() {
        List<Upgrade> choices = world.getUpgradeChoices();
        for (int i = 0; i < choices.size(); i++) {
            if (!(choices.get(i) instanceof ExtraLife)) {
                tap(KeyCode.valueOf("DIGIT" + (i + 1)));
                return choices.get(i);
            }
        }
        throw new AssertionError("Only Extra Life was offered");
    }

    @Test
    void clearingAStageOffersThreeDifferentUpgrades() {
        breakAllBricks();
        assertEquals(GameState.CHOOSE_UPGRADE, world.getState());
        assertEquals(1, world.getStage());
        List<Upgrade> choices = world.getUpgradeChoices();
        assertEquals(3, choices.size());
        assertEquals(3, choices.stream().map(Upgrade::getName).distinct().count());
    }

    @Test
    void enterTakesTheHighlightedUpgradeAndLoadsTheNextStage() {
        breakAllBricks();
        tap(KeyCode.RIGHT);
        assertEquals(1, world.getSelectedChoice());
        Upgrade highlighted = world.getUpgradeChoices().get(1);
        tap(KeyCode.ENTER);
        assertEquals(GameState.PLAYING, world.getState());
        assertEquals(2, world.getStage());
        assertEquals(2, world.getBricks().size());
        assertEquals("STAGE 2", world.getBanner());
        assertEquals("+ " + highlighted.getName(), world.getBannerSubtitle());
        assertEquals(List.of(highlighted.getName()), world.getStats().getUpgradesTaken());
        assertFalse(world.getBall().isLaunched());
    }

    @Test
    void numberKeyPicksThatUpgrade() {
        breakAllBricks();
        Upgrade third = world.getUpgradeChoices().get(2);
        tap(KeyCode.DIGIT3);
        assertEquals(List.of(third.getName()), world.getStats().getUpgradesTaken());
    }

    @Test
    void leftFromTheFirstCardWrapsToTheLast() {
        breakAllBricks();
        tap(KeyCode.LEFT);
        assertEquals(2, world.getSelectedChoice());
    }

    @Test
    void otherKeysDontPickAnUpgrade() {
        breakAllBricks();
        tap(KeyCode.DIGIT9);
        tap(KeyCode.P);
        assertEquals(GameState.CHOOSE_UPGRADE, world.getState());
    }

    @Test
    void nextStageUsesTheUpgradedStats() {
        breakAllBricks();
        tap(KeyCode.ENTER);
        PlayerStats stats = world.getStats();
        assertEquals(stats.getPaddleWidth(), world.getPaddle().getWidth(), 1e-9);
        assertEquals(stats.getPaddleSpeed(), world.getPaddle().getSpeed(), 1e-9);
        assertEquals(stats.getBallSpeed(), world.getBall().getSpeed(), 1e-9);
    }

    @Test
    void livesCarryOverToTheNextStage() {
        dropBall();
        breakAllBricks();
        pickUpgradeOtherThanExtraLife();
        assertEquals(GameWorld.STARTING_LIVES - 1, world.getLives());
    }

    @Test
    void extraBallsLaunchTogetherAndOnlyTheLastOneLostCostsALife() {
        world.getStats().setExtraBalls(2);
        tap(KeyCode.SPACE);
        assertEquals(3, world.getBalls().size());
        assertTrue(world.getBalls().stream().allMatch(Ball::isLaunched));

        world.getBalls().get(0).y = GameWorld.HEIGHT + 10;
        world.getBalls().get(1).y = GameWorld.HEIGHT + 10;
        world.update(GameLoop.STEP, input);
        assertEquals(1, world.getBalls().size());
        assertEquals(GameWorld.STARTING_LIVES, world.getLives());

        world.getBalls().get(0).y = GameWorld.HEIGHT + 10;
        world.update(GameLoop.STEP, input);
        assertEquals(GameWorld.STARTING_LIVES - 1, world.getLives());
        assertEquals(1, world.getBalls().size());
        assertFalse(world.getBall().isLaunched());
    }

    @Test
    void debugSkipOnlyWorksInDebugMode() {
        tap(KeyCode.N);
        assertEquals(GameState.PLAYING, world.getState());
        tap(KeyCode.F3);
        tap(KeyCode.N);
        assertEquals(GameState.CHOOSE_UPGRADE, world.getState());
        assertEquals(0, world.getScore());
    }

    @Test
    void piercingChargesAreGivenOnLaunch() {
        world.getStats().setPierce(4);
        tap(KeyCode.SPACE);
        assertEquals(4, world.getBall().getPierceLeft());
    }

    @Test
    void unbreakableBricksDontHaveToBeCleared() {
        breakAllBricks();
        tap(KeyCode.ENTER);
        breakAllBricks(); // leaves the U brick standing
        assertEquals(1, world.getBricks().size());
        assertEquals(GameState.VICTORY, world.getState());
    }

    @Test
    void clearingTheLastStageWinsTheGame() {
        breakAllBricks();
        tap(KeyCode.ENTER);
        breakAllBricks();
        assertEquals(GameState.VICTORY, world.getState());
    }

    @Test
    void worldWithoutStagesIsRejected() {
        assertThrows(GameException.class, () -> new GameWorld(List.of()));
    }
}
