package se233.project2.model;

import se233.project2.model.brick.Brick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// The dual laser special attack. Once activated it lasts DURATION seconds; while active,
// holding fire shoots a pair of bolts from both ends of the paddle every COOLDOWN seconds.
public class Laser {

    public static final double DURATION = 10;
    public static final double COOLDOWN = 0.3;
    // How far in from each end of the paddle the cannons sit.
    public static final double CANNON_INSET = 12;

    private final List<LaserBolt> bolts = new ArrayList<>();
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
            bolts.add(new LaserBolt(paddle.getX() + CANNON_INSET, paddle.getY() - LaserBolt.HEIGHT));
            bolts.add(new LaserBolt(paddle.getX() + paddle.getWidth() - CANNON_INSET, paddle.getY() - LaserBolt.HEIGHT));
            cooldown = COOLDOWN;
        }

        List<Brick> hits = new ArrayList<>();
        bolts.removeIf(bolt -> {
            bolt.fly(dt);
            if (bolt.getY() + bolt.getHeight() < GameWorld.HUD_HEIGHT) {
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

    public List<LaserBolt> getBolts() {
        return Collections.unmodifiableList(bolts);
    }
}
