package se233.project2.model;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameLoop;
import se233.project2.controller.InputHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameWorldTest {

    private GameWorld world;
    private InputHandler input;

    @BeforeEach
    void setUp() {
        world = new GameWorld();
        input = new InputHandler();
    }

    private void tap(KeyCode key) {
        input.press(key);
        world.update(GameLoop.STEP, input);
        input.release(key);
    }

    @Test
    void startsOnMenu() {
        assertEquals(GameState.MENU, world.getState());
    }

    @Test
    void enterStartsNewGameWithStartingLives() {
        tap(KeyCode.ENTER);
        assertEquals(GameState.PLAYING, world.getState());
        assertEquals(GameWorld.STARTING_LIVES, world.getLives());
        assertEquals(0, world.getScore());
        assertEquals(1, world.getStage());
    }

    @Test
    void pauseTogglesAndQuitReturnsToMenu() {
        tap(KeyCode.ENTER);
        tap(KeyCode.P);
        assertEquals(GameState.PAUSED, world.getState());
        tap(KeyCode.P);
        assertEquals(GameState.PLAYING, world.getState());
        tap(KeyCode.ESCAPE);
        tap(KeyCode.Q);
        assertEquals(GameState.MENU, world.getState());
    }

    @Test
    void newGameBuildsBricksAndRestsBallOnPaddle() {
        tap(KeyCode.ENTER);
        assertFalse(world.getBricks().isEmpty());
        world.update(GameLoop.STEP, input);
        assertFalse(world.getBall().isLaunched());
        assertEquals(world.getPaddle().getCenterX(), world.getBall().getCenterX(), 1e-9);
    }

    @Test
    void holdingRightMovesPaddleAndCarriesBall() {
        tap(KeyCode.ENTER);
        double startX = world.getPaddle().getX();
        input.press(KeyCode.RIGHT);
        for (int i = 0; i < 10; i++) {
            world.update(GameLoop.STEP, input);
        }
        input.release(KeyCode.RIGHT);
        assertTrue(world.getPaddle().getX() > startX);
        assertEquals(world.getPaddle().getCenterX(), world.getBall().getCenterX(), 1e-9);
    }

    @Test
    void spaceLaunchesBallUpwards() {
        tap(KeyCode.ENTER);
        world.update(GameLoop.STEP, input);
        double startY = world.getBall().getY();
        tap(KeyCode.SPACE);
        world.update(GameLoop.STEP, input);
        assertTrue(world.getBall().isLaunched());
        assertTrue(world.getBall().getY() < startY);
    }

    @Test
    void breakingBricksAddsTheirPointsToScore() {
        tap(KeyCode.ENTER);
        int bricksAtStart = world.getBricks().size();
        tap(KeyCode.SPACE);
        for (int i = 0; i < 300 && world.getScore() == 0; i++) {
            world.update(GameLoop.STEP, input);
        }
        assertTrue(world.getScore() > 0);
        assertTrue(world.getBricks().size() < bricksAtStart);
    }

    @Test
    void unusedPressDoesNotCarryOverToNextScreen() {
        tap(KeyCode.P); // nothing to pause on the menu
        tap(KeyCode.ENTER);
        world.update(GameLoop.STEP, input);
        assertEquals(GameState.PLAYING, world.getState());
    }

    @Test
    void holdingKeyCountsAsOnePress() {
        input.press(KeyCode.ENTER);
        input.press(KeyCode.ENTER); // auto-repeat
        world.update(GameLoop.STEP, input);
        assertEquals(GameState.PLAYING, world.getState());
        // The repeat must not leave a second press queued.
        input.press(KeyCode.P);
        world.update(GameLoop.STEP, input);
        assertEquals(GameState.PAUSED, world.getState());
    }
}
