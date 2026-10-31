package se233.project2.model.upgrade;

import se233.project2.model.PlayerStats;

// A risk-for-reward pick: harder to keep the ball alive, but every brick is worth more.
public class FastBall implements Upgrade {

    public static final double FACTOR = 1.15;
    public static final double MAX_SPEED = 560;
    public static final double SCORE_BONUS = 0.25;

    @Override
    public String getName() {
        return "Fast Ball";
    }

    @Override
    public String getDescription() {
        return "Ball 15% faster, +25% points";
    }

    @Override
    public boolean canApply(PlayerStats stats) {
        return stats.getBallSpeed() < MAX_SPEED;
    }

    @Override
    public void apply(PlayerStats stats) {
        stats.setBallSpeed(Math.min(stats.getBallSpeed() * FACTOR, MAX_SPEED));
        stats.setScoreMultiplier(stats.getScoreMultiplier() + SCORE_BONUS);
    }
}
