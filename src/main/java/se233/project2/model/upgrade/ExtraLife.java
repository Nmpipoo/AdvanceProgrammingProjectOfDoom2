package se233.project2.model.upgrade;

import se233.project2.model.PlayerStats;

public class ExtraLife implements Upgrade {

    public static final int MAX_LIVES = 6;

    @Override
    public String getName() {
        return "Extra Life";
    }

    @Override
    public String getDescription() {
        return "Gain one life";
    }

    @Override
    public boolean canApply(PlayerStats stats) {
        return stats.getLives() < MAX_LIVES;
    }

    @Override
    public void apply(PlayerStats stats) {
        stats.addLife();
    }
}
