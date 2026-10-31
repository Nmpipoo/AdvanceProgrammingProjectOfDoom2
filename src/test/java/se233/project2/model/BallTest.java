package se233.project2.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BallTest {

    @Test
    void restsOnPaddleUntilLaunched() {
        Paddle paddle = new Paddle();
        Ball ball = new Ball();
        ball.followPaddle(paddle);
        assertFalse(ball.isLaunched());
        assertEquals(paddle.getCenterX(), ball.getCenterX(), 1e-9);
        assertTrue(ball.getY() + ball.getHeight() <= paddle.getY());
    }

    @Test
    void launchesUpwardsAtFullSpeed() {
        Ball ball = new Ball();
        ball.launch();
        assertTrue(ball.isLaunched());
        assertTrue(ball.getVy() < 0);
        assertEquals(ball.getSpeed(), Math.hypot(ball.getVx(), ball.getVy()), 1e-9);
    }

    @Test
    void bouncesOffSideAndTopWalls() {
        Ball ball = new Ball();
        ball.launch();
        // Moving up and right: push it past the right wall and the ceiling.
        ball.move(100);
        ball.bounceOffWalls(0, GameWorld.HUD_HEIGHT, GameWorld.WIDTH);
        assertTrue(ball.getVx() < 0);
        assertTrue(ball.getVy() > 0);
        assertEquals(GameWorld.WIDTH - ball.getWidth(), ball.getX(), 1e-9);
        assertEquals(GameWorld.HUD_HEIGHT, ball.getY(), 1e-9);
    }

    @Test
    void bouncesUpOffThePaddle() {
        Paddle paddle = new Paddle();
        Ball ball = new Ball();
        ball.launch();
        ball.bounceOffWalls(0, 1000, GameWorld.WIDTH); // force it to move downwards
        ball.bounceOffPaddle(paddle);
        assertTrue(ball.getVy() < 0);
        assertEquals(paddle.getY() - ball.getHeight(), ball.getY(), 1e-9);
    }

    @Test
    void resetStopsTheBall() {
        Ball ball = new Ball();
        ball.launch();
        ball.reset();
        assertFalse(ball.isLaunched());
        assertEquals(0, ball.getVx());
        assertEquals(0, ball.getVy());
    }

    @Test
    void bouncesRestartTheBounceAnimation() {
        Ball ball = new Ball();
        ball.launch();
        ball.tick(1);
        assertTrue(ball.getSinceBounce() >= 1);

        ball.bounceOffPaddle(new Paddle());
        assertEquals(0, ball.getSinceBounce());
        assertTrue(ball.isBouncedVertically());

        ball.tick(0.05);
        assertEquals(0.05, ball.getSinceBounce(), 1e-9);

        ball.bounceOffSide(new Paddle());
        assertFalse(ball.isBouncedVertically());
    }

    @Test
    void travellingWithoutHittingAWallIsNotABounce() {
        Ball ball = new Ball();
        ball.launch();
        ball.tick(1);
        ball.bounceOffWalls(-1000, -1000, 5000);
        assertTrue(ball.getSinceBounce() >= 1);
    }
}
