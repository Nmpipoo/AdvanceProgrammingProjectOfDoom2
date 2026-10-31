package se233.project2.model.upgrade;

import se233.project2.model.PlayerStats;

public class ScoreBoost implements Upgrade {

    public static final double BONUS = 0.5;
    public static final double MAX_MULTIPLIER = 3.0;

    @Override
    public String getName() {
        return "Score Boost";
    }

    @Override
    public String getDescription() {
        return "+50% points from bricks";
    }

    @Override
    public boolean canApply(PlayerStats stats) {
        return stats.getScoreMultiplier() < MAX_MULTIPLIER;
    }

    @Override
    public void apply(PlayerStats stats) {
        stats.setScoreMultiplier(Math.min(stats.getScoreMultiplier() + BONUS, MAX_MULTIPLIER));
    }
}
