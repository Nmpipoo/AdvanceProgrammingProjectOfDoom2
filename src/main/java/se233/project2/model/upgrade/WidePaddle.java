package se233.project2.model.upgrade;

import se233.project2.model.PlayerStats;

public class WidePaddle implements Upgrade {

    public static final double FACTOR = 1.25;
    public static final double MAX_WIDTH = 200;

    @Override
    public String getName() {
        return "Wide Paddle";
    }

    @Override
    public String getDescription() {
        return "Paddle 25% wider";
    }

    @Override
    public boolean canApply(PlayerStats stats) {
        return stats.getPaddleWidth() < MAX_WIDTH;
    }

    @Override
    public void apply(PlayerStats stats) {
        stats.setPaddleWidth(Math.min(stats.getPaddleWidth() * FACTOR, MAX_WIDTH));
    }
}
