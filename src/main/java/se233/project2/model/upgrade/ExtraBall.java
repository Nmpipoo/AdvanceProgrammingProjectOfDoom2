package se233.project2.model.upgrade;

import se233.project2.model.PlayerStats;

public class ExtraBall implements Upgrade {

    public static final int MAX_EXTRA = 2;

    @Override
    public String getName() {
        return "Extra Ball";
    }

    @Override
    public String getDescription() {
        return "Launch one more ball each time";
    }

    @Override
    public boolean canApply(PlayerStats stats) {
        return stats.getExtraBalls() < MAX_EXTRA;
    }

    @Override
    public void apply(PlayerStats stats) {
        stats.setExtraBalls(stats.getExtraBalls() + 1);
    }
}
