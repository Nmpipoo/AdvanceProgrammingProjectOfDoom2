package se233.project2.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaddleTest {

    @Test
    void startsCentered() {
        Paddle paddle = new Paddle();
        assertEquals(GameWorld.WIDTH / 2, paddle.getCenterX(), 1e-9);
    }

    @Test
    void movesBySpeedTimesTime() {
        Paddle paddle = new Paddle();
        double startX = paddle.getX();
        paddle.move(1, 0.1);
        assertEquals(startX + Paddle.DEFAULT_SPEED * 0.1, paddle.getX(), 1e-9);
        paddle.move(-1, 0.1);
        assertEquals(startX, paddle.getX(), 1e-9);
    }

    @Test
    void moveAnimationRunsOnlyWhileMoving() {
        Paddle paddle = new Paddle();
        assertFalse(paddle.isMoving());
        paddle.move(1, 0.1);
        paddle.move(1, 0.1);
        assertTrue(paddle.isMoving());
        assertEquals(0.2, paddle.getMoveTime(), 1e-9);
        paddle.move(0, 0.1);
        assertFalse(paddle.isMoving());
    }

    @Test
    void pushingAgainstAWallIsNotMoving() {
        Paddle paddle = new Paddle();
        paddle.move(-1, 10);
        paddle.move(-1, 0.1);
        assertFalse(paddle.isMoving());
    }

    @Test
    void staysInsideTheWalls() {
        Paddle paddle = new Paddle();
        paddle.move(-1, 10);
        assertEquals(0, paddle.getX(), 1e-9);
        paddle.move(1, 10);
        assertEquals(GameWorld.WIDTH - paddle.getWidth(), paddle.getX(), 1e-9);
    }
}
