package se233.project2.model.upgrade;

import se233.project2.model.PlayerStats;

public class QuickPaddle implements Upgrade {

    public static final double FACTOR = 1.2;
    public static final double MAX_SPEED = 900;

    @Override
    public String getName() {
        return "Quick Paddle";
    }

    @Override
    public String getDescription() {
        return "Paddle moves 20% faster";
    }

    @Override
    public boolean canApply(PlayerStats stats) {
        return stats.getPaddleSpeed() < MAX_SPEED;
    }

    @Override
    public void apply(PlayerStats stats) {
        stats.setPaddleSpeed(Math.min(stats.getPaddleSpeed() * FACTOR, MAX_SPEED));
    }
}
