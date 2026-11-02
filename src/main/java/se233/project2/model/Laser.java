package se233.project2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// The dual laser special attack. Catching a capsule turns it on for DURATION seconds;
// while active, holding fire shoots a bolt from each end of the paddle every COOLDOWN seconds.
public class Laser {

    public static final double DURATION = 10;
    public static final double COOLDOWN = 0.3;
    // How far in from each end of the paddle the cannons sit.
    public static final double CANNON_INSET = 12;

    // One shot from a cannon. Flies straight up.
    public static class Bolt extends GameObject {
        public static final double SPEED = 720;

        Bolt(double centerX, double top) {
            super(centerX - 3, top, 6, 18);
        }
    }

    // The capsule that drops from a broken brick; catching it turns the laser on.
    public static class Capsule extends GameObject {
        public static final double FALL_SPEED = 160;

        public Capsule(double centerX, double centerY) {
            super(centerX - 18, centerY - 8, 36, 16);
        }

        public void fall(double dt) {
            y += FALL_SPEED * dt;
        }
    }

    private final List<Bolt> bolts = new ArrayList<>();
    private double timeLeft;
    private double cooldown;

    public void activate() {
        timeLeft = DURATION;
    }

    public void reset() {
        timeLeft = 0;
        cooldown = 0;
        bolts.clear();
    }

    // Returns the bricks hit by bolts this update.
    public List<Brick> update(double dt, Paddle paddle, boolean firing, List<Brick> bricks) {
        timeLeft = Math.max(0, timeLeft - dt);
        cooldown = Math.max(0, cooldown - dt);
        if (isActive() && firing && cooldown == 0) {
            bolts.add(new Bolt(paddle.getX() + CANNON_INSET, paddle.getY() - 18));
            bolts.add(new Bolt(paddle.getX() + paddle.getWidth() - CANNON_INSET, paddle.getY() - 18));
            cooldown = COOLDOWN;
        }

        List<Brick> hits = new ArrayList<>();
        bolts.removeIf(bolt -> {
            bolt.y -= Bolt.SPEED * dt;
            if (bolt.y + bolt.height < GameWorld.HUD_HEIGHT) {
                return true;
            }
            for (Brick brick : bricks) {
                if (!brick.isDestroyed() && bolt.intersects(brick)) {
                    brick.hit();
                    hits.add(brick);
                    return true;
                }
            }
            return false;
        });
        return hits;
    }

    public boolean isActive() {
        return timeLeft > 0;
    }

    public double getTimeLeft() {
        return timeLeft;
    }

    public List<Bolt> getBolts() {
        return Collections.unmodifiableList(bolts);
    }
}
