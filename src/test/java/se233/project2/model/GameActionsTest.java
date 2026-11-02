package se233.project2.model;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameLoop;
import se233.project2.controller.InputHandler;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Rubric: unit tests for game actions (paddle, ball, bricks, laser, pause, stages).
class GameActionsTest {

    private final InputHandler input = new InputHandler();
    private final Paddle paddle = new Paddle();
    private final Ball ball = new Ball();
    private GameWorld world;

    @BeforeEach
    void setUp() {
        world = new GameWorld(List.of(new String[]{"N"}, new String[]{"NU"}));
        tap(KeyCode.ENTER);
    }

    private void tap(KeyCode key) {
        input.press(key);
        world.update(GameLoop.STEP, input);
        input.release(key);
    }

    // Puts the ball just under the brick, moving up into it, and runs physics until it hits something.
    private void shootBallInto(Brick brick, List<Brick> bricks) {
        ball.launch();
        ball.x = brick.getCenterX() - Ball.RADIUS - 2;
        ball.y = brick.getY() + brick.getHeight() + 5;
        for (int i = 0; i < 600; i++) {
            if (!ball.step(paddle, bricks, GameLoop.STEP).bricks().isEmpty()) {
                return;
            }
        }
        throw new AssertionError("The ball never hit the brick");
    }

    @Test
    void paddleMovesWithItsSpeedAndStaysInsideTheWalls() {
        double startX = paddle.getX();
        paddle.move(1, 0.1);
        assertEquals(startX + Paddle.DEFAULT_SPEED * 0.1, paddle.getX(), 1e-9);
        paddle.move(-1, 10);
        assertEquals(0, paddle.getX(), 1e-9);
        paddle.move(1, 10);
        assertEquals(GameWorld.WIDTH - paddle.getWidth(), paddle.getX(), 1e-9);
    }

    @Test
    void ballRestsOnThePaddleUntilSpaceLaunchesIt() {
        assertFalse(world.getBall().isLaunched());
        assertEquals(world.getPaddle().getCenterX(), world.getBall().getCenterX(), 1e-9);
        double startY = world.getBall().getY();
        tap(KeyCode.SPACE);
        world.update(GameLoop.STEP, input);
        assertTrue(world.getBall().isLaunched());
        assertTrue(world.getBall().getY() < startY);
    }

    @Test
    void ballBouncesOffTheWalls() {
        ball.launch();
        // Moving up and right: push it past the right wall and the ceiling.
        ball.step(paddle, List.of(), 100);
        assertTrue(ball.getVx() < 0);
        assertTrue(ball.getVy() > 0);
    }

    @Test
    void ballBouncesOffThePaddleAtAnAngleSetByWhereItLands() {
        ball.x = paddle.getCenterX() - Ball.RADIUS;
        ball.bounceOffPaddle(paddle);
        assertEquals(0, ball.getVx(), 1e-9, "the center sends it straight up");
        assertTrue(ball.getVy() < 0);

        ball.x = paddle.getX() + paddle.getWidth() - Ball.RADIUS;
        ball.bounceOffPaddle(paddle);
        assertEquals(Ball.MAX_BOUNCE_ANGLE, Math.atan2(ball.getVx(), -ball.getVy()), 1e-9, "the edge sends it sideways");
    }

    @Test
    void ballBreaksANormalBrickAndBouncesBack() {
        Brick brick = new Brick(Brick.Type.NORMAL, 300, 200);
        shootBallInto(brick, new ArrayList<>(List.of(brick)));
        assertTrue(brick.isDestroyed());
        assertTrue(ball.getVy() > 0);
    }

    @Test
    void strongBricksTakeThreeHitsAndUnbreakableBricksNeverBreak() {
        Brick strong = new Brick(Brick.Type.STRONG, 0, 0);
        assertFalse(strong.hit());
        assertFalse(strong.hit());
        assertTrue(strong.hit());
        assertTrue(strong.isDestroyed());

        Brick wall = new Brick(Brick.Type.UNBREAKABLE, 0, 0);
        for (int i = 0; i < 10; i++) {
            wall.hit();
        }
        assertFalse(wall.isDestroyed());
    }

    @Test
    void laserFiresAPairOfBoltsThatDamageBricks() {
        Laser laser = new Laser();
        laser.update(GameLoop.STEP, paddle, true, List.of());
        assertTrue(laser.getBolts().isEmpty(), "no bolts before the power-up is caught");

        laser.activate();
        Brick brick = new Brick(Brick.Type.STRONG, paddle.getX() + Laser.CANNON_INSET - Brick.WIDTH / 2, 200);
        List<Brick> hits = List.of();
        for (int i = 0; i < 120 && hits.isEmpty(); i++) {
            hits = laser.update(GameLoop.STEP, paddle, i == 0, List.of(brick));
            if (i == 0) {
                assertEquals(2, laser.getBolts().size());
            }
        }
        assertEquals(List.of(brick), hits);
        assertEquals(2, brick.getHitPoints());
    }

    @Test
    void laserWearsOffAfterItsDuration() {
        Laser laser = new Laser();
        laser.activate();
        for (double t = 0; t < Laser.DURATION + 0.1; t += GameLoop.STEP) {
            laser.update(GameLoop.STEP, paddle, false, List.of());
        }
        assertFalse(laser.isActive());
        laser.update(GameLoop.STEP, paddle, true, List.of());
        assertTrue(laser.getBolts().isEmpty());
    }

    @Test
    void pauseTogglesAndQuitReturnsToTheMenu() {
        tap(KeyCode.P);
        assertEquals(GameWorld.State.PAUSED, world.getState());
        tap(KeyCode.P);
        assertEquals(GameWorld.State.PLAYING, world.getState());
        tap(KeyCode.P);
        tap(KeyCode.Q);
        assertEquals(GameWorld.State.MENU, world.getState());
    }

    @Test
    void clearingTheLastStageWinsTheGame() {
        breakAllBricks();
        input.click(GameWorld.cardLeft(0, 3) + 10, GameWorld.CARD_TOP + 10); // take an upgrade
        world.update(GameLoop.STEP, input);
        assertEquals(2, world.getStage());
        breakAllBricks();   // the unbreakable brick doesn't need clearing
        assertEquals(GameWorld.State.VICTORY, world.getState());
    }

    private void breakAllBricks() {
        for (Brick brick : world.getBricks()) {
            while (brick.isBreakable() && !brick.isDestroyed()) {
                brick.hit();
            }
        }
        world.update(GameLoop.STEP, input);
    }
}
