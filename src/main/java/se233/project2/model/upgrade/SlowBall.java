package se233.project2.model.upgrade;

import se233.project2.model.PlayerStats;

public class SlowBall implements Upgrade {

    public static final double FACTOR = 0.85;
    public static final double MIN_SPEED = 240;

    @Override
    public String getName() {
        return "Slow Ball";
    }

    @Override
    public String getDescription() {
        return "Ball 15% slower, easier to catch";
    }

    @Override
    public boolean canApply(PlayerStats stats) {
        return stats.getBallSpeed() > MIN_SPEED;
    }

    @Override
    public void apply(PlayerStats stats) {
        stats.setBallSpeed(Math.max(stats.getBallSpeed() * FACTOR, MIN_SPEED));
    }
}
