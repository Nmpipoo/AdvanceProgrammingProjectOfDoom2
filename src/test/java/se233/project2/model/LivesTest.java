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
        assertEquals(GameWorld.State.PLAYING, world.getState());
        assertFalse(world.getBall().isLaunched());
    }

    @Test
    void losingTheLastLifeEndsTheGame() {
        for (int i = 0; i < 3; i++) {
            dropBall();
        }
        assertEquals(0, world.getLives());
        assertEquals(GameWorld.State.GAME_OVER, world.getState());
    }

    @Test
    void playingAgainAfterGameOverRestoresTheLives() {
        for (int i = 0; i < 3; i++) {
            dropBall();
        }
        tap(KeyCode.ENTER);
        assertEquals(GameWorld.State.PLAYING, world.getState());
        assertEquals(3, world.getLives());
        assertEquals(0, world.getScore());
    }

    @Test
    void livesCarryOverToTheNextStage() {
        dropBall();
        world.getBricks().forEach(brick -> brick.hit());
        world.update(GameLoop.STEP, input);
        input.click(GameWorld.cardLeft(0, 3) + 10, GameWorld.CARD_TOP + 10);
        world.update(GameLoop.STEP, input);
        assertEquals(2, world.getStage());
        assertEquals(2, world.getLives());
    }
}
