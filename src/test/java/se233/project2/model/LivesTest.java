package se233.project2.model;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameLoop;
import se233.project2.controller.InputHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// Rubric: unit tests for lives.
class LivesTest {

    private GameWorld world;
    private final InputHandler input = new InputHandler();

    @BeforeEach
    void setUp() {
        world = new GameWorld(List.of(new String[]{"N"}, new String[]{"N"}));
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

    @Test
    void gameStartsWithThreeLives() {
        assertEquals(3, world.getLives());
    }

    @Test
    void missingTheBallCostsALifeAndPutsItBackOnThePaddle() {
        dropBall();
        assertEquals(2, world.getLives());
        assertEquals(GameState.PLAYING, world.getState());
        assertFalse(world.getBall().isLaunched());
        assertEquals("LIFE LOST", world.getBanner());
    }

    @Test
    void losingTheLastLifeEndsTheGame() {
        for (int i = 0; i < 3; i++) {
            dropBall();
        }
        assertEquals(0, world.getLives());
        assertEquals(GameState.GAME_OVER, world.getState());
    }

    @Test
    void playingAgainAfterGameOverRestoresTheLives() {
        for (int i = 0; i < 3; i++) {
            dropBall();
        }
        tap(KeyCode.ENTER);
        assertEquals(GameState.PLAYING, world.getState());
        assertEquals(3, world.getLives());
        assertEquals(0, world.getScore());
    }

    @Test
    void withExtraBallsOnlyLosingTheLastBallCostsALife() {
        world.getStats().setExtraBalls(1);
        tap(KeyCode.SPACE);
        assertEquals(2, world.getBalls().size());

        world.getBalls().get(0).y = GameWorld.HEIGHT + 10;
        world.update(GameLoop.STEP, input);
        assertEquals(3, world.getLives());

        world.getBalls().get(0).y = GameWorld.HEIGHT + 10;
        world.update(GameLoop.STEP, input);
        assertEquals(2, world.getLives());
    }

    @Test
    void livesCarryOverToTheNextStage() {
        dropBall();
        world.getBricks().forEach(brick -> brick.hit());
        world.update(GameLoop.STEP, input);
        // Pick an upgrade that isn't Extra Life, so the count stays the same.
        int pick = world.getUpgradeChoices().get(0).getName().equals("Extra Life") ? 2 : 1;
        tap(KeyCode.valueOf("DIGIT" + pick));
        assertEquals(2, world.getStage());
        assertEquals(2, world.getLives());
    }
}
