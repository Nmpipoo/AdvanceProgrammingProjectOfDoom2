package se233.project2.model;

import se233.project2.model.brick.Brick;

import java.util.ArrayList;
import java.util.List;

// Moves the ball for one update and resolves what it bumps into:
// bricks, the walls and the paddle. Bricks that get hit are damaged here.
public class Physics {

    public record Result(List<Brick> bricksHit, boolean paddleHit) {
    }

    public Result step(Ball ball, Paddle paddle, List<Brick> bricks, double dt) {
        List<Brick> hits = new ArrayList<>();

        ball.moveX(dt);
        resolveBricks(ball, bricks, hits, true);
        ball.moveY(dt);
        resolveBricks(ball, bricks, hits, false);

        ball.bounceOffWalls(0, GameWorld.HUD_HEIGHT, GameWorld.WIDTH);

        // Only catch the ball while it's falling and hasn't already dropped past the paddle's face.
        boolean paddleHit = ball.getVy() > 0 && ball.intersects(paddle)
                && ball.getCenterY() < paddle.getY() + paddle.getHeight();
        if (paddleHit) {
            ball.bounceOffPaddle(paddle);
        }
        return new Result(hits, paddleHit);
    }

    // A piercing ball can smash a brick while touching another, so keep going
    // until it's clear of bricks or has bounced off one.
    private static void resolveBricks(Ball ball, List<Brick> bricks, List<Brick> hits, boolean sideways) {
        Brick brick;
        while ((brick = closestOverlap(ball, bricks)) != null) {
            if (smash(ball, brick, hits)) {
                continue;
            }
            if (sideways) {
                ball.bounceOffSide(brick);
            } else {
                ball.bounceOffTopOrBottom(brick);
            }
            damage(brick, hits);
            return;
        }
    }

    // When the ball lands on a seam between bricks it overlaps two; only the one
    // nearest its center counts, so a single bounce never breaks two bricks.
    private static Brick closestOverlap(Ball ball, List<Brick> bricks) {
        Brick closest = null;
        double best = Double.MAX_VALUE;
        for (Brick brick : bricks) {
            if (brick.isDestroyed() || !ball.intersects(brick)) {
                continue;
            }
            double distance = Math.hypot(ball.getCenterX() - brick.getCenterX(),
                    ball.getCenterY() - brick.getCenterY());
            if (distance < best) {
                best = distance;
                closest = brick;
            }
        }
        return closest;
    }

    // A piercing ball breaks a breakable brick outright and keeps flying without bouncing.
    private static boolean smash(Ball ball, Brick brick, List<Brick> hits) {
        if (!brick.isBreakable() || !ball.usePierce()) {
            return false;
        }
        brick.destroy();
        if (!hits.contains(brick)) {
            hits.add(brick);
        }
        return true;
    }

    // A corner hit can touch the same brick on both axes; it still only takes one hit.
    private static void damage(Brick brick, List<Brick> hits) {
        if (!hits.contains(brick)) {
            brick.hit();
            hits.add(brick);
        }
    }
}
