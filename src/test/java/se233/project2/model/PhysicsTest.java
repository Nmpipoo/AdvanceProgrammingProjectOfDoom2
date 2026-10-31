package se233.project2.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameLoop;
import se233.project2.model.brick.Brick;
import se233.project2.model.brick.BrickLayout;
import se233.project2.model.brick.NormalBrick;
import se233.project2.model.brick.StrongBrick;
import se233.project2.model.brick.UnbreakableBrick;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicsTest {

    private Physics physics;
    private Paddle paddle;
    private Ball ball;

    @BeforeEach
    void setUp() {
        physics = new Physics();
        paddle = new Paddle();
        ball = new Ball();
    }

    // Launches the ball, then runs updates until something is hit or the time runs out.
    private Physics.Result runUntilHit(List<Brick> bricks) {
        for (int i = 0; i < 600; i++) {
            Physics.Result result = physics.step(ball, paddle, bricks, GameLoop.STEP);
            if (!result.bricksHit().isEmpty() || result.paddleHit()) {
                return result;
            }
        }
        throw new AssertionError("Nothing was hit");
    }

    private void placeBallBelow(Brick brick) {
        ball.followPaddle(paddle);
        ball.launch();
        // Put it just under the brick, lined up so it travels into its bottom face.
        double startY = brick.getY() + brick.getHeight() + 5;
        moveBallTo(brick.getCenterX() - Ball.RADIUS - 2, startY);
    }

    private void moveBallTo(double x, double y) {
        ball.x = x;
        ball.y = y;
    }

    @Test
    void ballHittingBrickFromBelowBouncesDownAndDestroysIt() {
        List<Brick> bricks = new ArrayList<>(List.of(new NormalBrick(300, 200)));
        placeBallBelow(bricks.get(0));
        Physics.Result result = runUntilHit(bricks);
        assertEquals(1, result.bricksHit().size());
        assertTrue(bricks.get(0).isDestroyed());
        assertTrue(ball.getVy() > 0);
        assertEquals(bricks.get(0).getY() + bricks.get(0).getHeight(), ball.getY(), 1e-9);
    }

    @Test
    void ballHittingSideOfBrickReversesSideways() {
        Brick brick = new StrongBrick(400, 300);
        List<Brick> bricks = new ArrayList<>(List.of(brick));
        ball.launch(); // moving up and to the right
        // Start level with the brick, just left of it, so it reaches the left face first.
        moveBallTo(brick.getX() - ball.getWidth() - 1, brick.getY() + 4);
        double vyBefore = ball.getVy();
        runUntilHit(bricks);
        assertTrue(ball.getVx() < 0);
        assertEquals(vyBefore, ball.getVy(), 1e-9);
        assertEquals(2, brick.getHitPoints());
    }

    @Test
    void seamBetweenTwoBricksOnlyBreaksOne() {
        List<Brick> bricks = BrickLayout.fromRows("NN");
        Brick left = bricks.get(0);
        ball.launch();
        // Aim the ball's center at the gap between the two bricks.
        moveBallTo(left.getX() + left.getWidth() + BrickLayout.GAP / 2 - Ball.RADIUS,
                left.getY() + left.getHeight() + 2);
        Physics.Result result = runUntilHit(bricks);
        assertEquals(1, result.bricksHit().size());
        assertEquals(1, bricks.stream().filter(Brick::isDestroyed).count());
    }

    @Test
    void piercingBallSmashesThroughAStrongBrickWithoutBouncing() {
        Brick brick = new StrongBrick(300, 200);
        List<Brick> bricks = new ArrayList<>(List.of(brick));
        placeBallBelow(brick);
        ball.setPierceLeft(1);
        runUntilHit(bricks);
        assertTrue(brick.isDestroyed());
        assertTrue(ball.getVy() < 0, "ball should keep flying up");
        assertEquals(0, ball.getPierceLeft());
    }

    @Test
    void piercingBallBouncesNormallyOnceChargesRunOut() {
        Brick first = new NormalBrick(300, 200);
        Brick second = new NormalBrick(300, 200 - Brick.HEIGHT - BrickLayout.GAP);
        List<Brick> bricks = new ArrayList<>(List.of(first, second));
        placeBallBelow(first);
        ball.setPierceLeft(1);
        runUntilHit(bricks);
        assertTrue(first.isDestroyed());
        assertTrue(ball.getVy() < 0);
        runUntilHit(bricks);
        assertTrue(second.isDestroyed());
        assertTrue(ball.getVy() > 0, "out of charges, so it bounces off the second brick");
    }

    @Test
    void piercingBallStillBouncesOffUnbreakableBricks() {
        Brick wall = new UnbreakableBrick(300, 200);
        List<Brick> bricks = new ArrayList<>(List.of(wall));
        placeBallBelow(wall);
        ball.setPierceLeft(3);
        runUntilHit(bricks);
        assertFalse(wall.isDestroyed());
        assertTrue(ball.getVy() > 0);
        assertEquals(3, ball.getPierceLeft());
    }

    @Test
    void unbreakableBrickBouncesBallWithoutBreaking() {
        Brick wall = new UnbreakableBrick(300, 200);
        List<Brick> bricks = new ArrayList<>(List.of(wall));
        placeBallBelow(wall);
        runUntilHit(bricks);
        assertFalse(wall.isDestroyed());
        assertTrue(ball.getVy() > 0);
    }

    @Test
    void fallingBallBouncesUpOffThePaddle() {
        ball.launch();
        ball.bounceOffTopOrBottom(new NormalBrick(0, -100)); // now moving down
        moveBallTo(paddle.getCenterX() - Ball.RADIUS, paddle.getY() - ball.getHeight() - 2);
        Physics.Result result = runUntilHit(List.of());
        assertTrue(result.paddleHit());
        assertTrue(ball.getVy() < 0);
        assertEquals(paddle.getY() - ball.getHeight(), ball.getY(), 1e-9);
    }

    @Test
    void ballLandingOnPaddleCenterGoesStraightUp() {
        moveBallTo(paddle.getCenterX() - Ball.RADIUS, paddle.getY() - ball.getHeight());
        ball.bounceOffPaddle(paddle);
        assertEquals(0, ball.getVx(), 1e-9);
        assertEquals(-ball.getSpeed(), ball.getVy(), 1e-9);
    }

    @Test
    void risingBallPassesUpThroughPaddleWithoutBouncing() {
        ball.launch(); // moving up
        moveBallTo(paddle.getCenterX() - Ball.RADIUS, paddle.getY() + 2);
        Physics.Result result = physics.step(ball, paddle, List.of(), GameLoop.STEP);
        assertFalse(result.paddleHit());
    }

    @Test
    void ballLandingNearPaddleEdgeLeavesAtAnAngle() {
        moveBallTo(paddle.getX() + paddle.getWidth() - Ball.RADIUS, paddle.getY() - ball.getHeight());
        ball.bounceOffPaddle(paddle);
        double angle = Math.atan2(ball.getVx(), -ball.getVy());
        assertEquals(Ball.MAX_BOUNCE_ANGLE, angle, 1e-9);
        assertEquals(ball.getSpeed(), Math.hypot(ball.getVx(), ball.getVy()), 1e-9);
    }

    @Test
    void bounceAngleNeverExceedsTheMaximum() {
        moveBallTo(paddle.getX() - 50, paddle.getY() - ball.getHeight());
        ball.bounceOffPaddle(paddle);
        double angle = Math.atan2(ball.getVx(), -ball.getVy());
        assertEquals(-Ball.MAX_BOUNCE_ANGLE, angle, 1e-9);
    }
}
