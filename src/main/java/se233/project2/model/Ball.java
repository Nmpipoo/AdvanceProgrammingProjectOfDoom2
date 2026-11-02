package se233.project2.model;

import java.util.ArrayList;
import java.util.List;

// The ball rests on the paddle until launched, then moves at a constant speed
// and bounces off walls, bricks and the paddle.
public class Ball extends GameObject {

    public static final double RADIUS = 7;
    // The Big Ball power-up makes the ball BIG_SCALE times as wide for BIG_SECONDS.
    public static final double BIG_SCALE = 2;
    public static final double BIG_SECONDS = 10;
    public static final double DEFAULT_SPEED = 360;
    // Launch direction, measured from straight up towards the right.
    public static final double LAUNCH_ANGLE = Math.toRadians(30);
    public static final double MAX_BOUNCE_ANGLE = Math.toRadians(60);

    // What one step hit: the bricks it damaged and whether it bounced off the paddle.
    public record Hits(List<Brick> bricks, boolean paddle) {
    }

    private double vx;
    private double vy;
    private final double speed = DEFAULT_SPEED;
    private boolean launched;
    // Time since the last bounce; drives the ball's squash animation.
    private double sinceBounce = Double.MAX_VALUE;
    private boolean bouncedVertically;
    private double bigTimeLeft;

    public Ball() {
        super(0, 0, RADIUS * 2, RADIUS * 2);
    }

    public void followPaddle(Paddle paddle) {
        x = paddle.getCenterX() - width / 2;
        y = paddle.getY() - height - 1;
    }

    // Catching the Big Ball power-up: grow around the center, or restart the timer if already big.
    public void grow() {
        bigTimeLeft = BIG_SECONDS;
        resize(RADIUS * 2 * BIG_SCALE);
    }

    private void resize(double size) {
        x = getCenterX() - size / 2;
        y = getCenterY() - size / 2;
        width = size;
        height = size;
    }

    public void launch() {
        launch(LAUNCH_ANGLE);
    }

    // angle is measured from straight up; positive goes right.
    public void launch(double angle) {
        launched = true;
        vx = speed * Math.sin(angle);
        vy = -speed * Math.cos(angle);
    }

    // Moves one step and handles every collision. Moving one axis at a time
    // tells which side of a brick the ball hit.
    public Hits step(Paddle paddle, List<Brick> bricks, double dt) {
        List<Brick> hits = new ArrayList<>();
        x += vx * dt;
        hitBricks(bricks, hits, true);
        y += vy * dt;
        hitBricks(bricks, hits, false);
        bounceOffWalls();

        // Only catch the ball while it's falling and hasn't already dropped past the paddle's face.
        boolean paddleHit = vy > 0 && intersects(paddle) && getCenterY() < paddle.getY() + paddle.getHeight();
        if (paddleHit) {
            bounceOffPaddle(paddle);
        }
        return new Hits(hits, paddleHit);
    }

    // Bounces off the overlapped brick nearest the ball's center (so a seam between two bricks only breaks one).
    private void hitBricks(List<Brick> bricks, List<Brick> hits, boolean sideways) {
        Brick brick = closestOverlap(bricks);
        if (brick == null) {
            return;
        }
        if (sideways) {
            x = vx > 0 ? brick.getX() - width : brick.getX() + brick.getWidth();
            vx = -vx;
        } else {
            y = vy > 0 ? brick.getY() - height : brick.getY() + brick.getHeight();
            vy = -vy;
        }
        markBounce(!sideways);
        // A corner hit can touch the same brick on both axes; it still only takes one hit.
        if (!hits.contains(brick)) {
            brick.hit();
            hits.add(brick);
        }
    }

    private Brick closestOverlap(List<Brick> bricks) {
        Brick closest = null;
        double best = Double.MAX_VALUE;
        for (Brick brick : bricks) {
            if (!brick.isDestroyed() && intersects(brick)) {
                double distance = Math.hypot(getCenterX() - brick.getCenterX(), getCenterY() - brick.getCenterY());
                if (distance < best) {
                    best = distance;
                    closest = brick;
                }
            }
        }
        return closest;
    }

    private void bounceOffWalls() {
        if (x < 0 || x + width > GameWorld.WIDTH) {
            x = Math.clamp(x, 0, GameWorld.WIDTH - width);
            vx = x == 0 ? Math.abs(vx) : -Math.abs(vx);
            markBounce(false);
        }
        if (y < GameWorld.HUD_HEIGHT) {
            y = GameWorld.HUD_HEIGHT;
            vy = Math.abs(vy);
            markBounce(true);
        }
    }

    // The further from the paddle's center the ball lands, the steeper it leaves:
    // straight up in the middle, up to MAX_BOUNCE_ANGLE at the edges.
    public void bounceOffPaddle(Paddle paddle) {
        double offset = (getCenterX() - paddle.getCenterX()) / (paddle.getWidth() / 2);
        launch(Math.clamp(offset, -1, 1) * MAX_BOUNCE_ANGLE);
        y = paddle.getY() - height;
        markBounce(true);
    }

    private void markBounce(boolean vertical) {
        sinceBounce = 0;
        bouncedVertically = vertical;
    }

    public void tick(double dt) {
        sinceBounce += dt;
        if (bigTimeLeft > 0) {
            bigTimeLeft = Math.max(0, bigTimeLeft - dt);
            if (bigTimeLeft == 0) {
                resize(RADIUS * 2);
            }
        }
    }

    public boolean isBig() {
        return bigTimeLeft > 0;
    }

    public double getBigTimeLeft() {
        return bigTimeLeft;
    }

    public double getSinceBounce() {
        return sinceBounce;
    }

    // True if the last bounce was off a floor or ceiling (squash flat), false for a wall (squash thin).
    public boolean isBouncedVertically() {
        return bouncedVertically;
    }

    public boolean isLaunched() {
        return launched;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getSpeed() {
        return speed;
    }
}
