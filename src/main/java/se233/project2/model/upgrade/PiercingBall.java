package se233.project2.model.upgrade;

import se233.project2.model.PlayerStats;

public class PiercingBall implements Upgrade {

    public static final int PIERCE_PER_PICK = 2;
    public static final int MAX_PIERCE = 6;

    @Override
    public String getName() {
        return "Piercing Ball";
    }

    @Override
    public String getDescription() {
        return "After each paddle hit, smash through 2 bricks";
    }

    @Override
    public boolean canApply(PlayerStats stats) {
        return stats.getPierce() < MAX_PIERCE;
    }

    @Override
    public void apply(PlayerStats stats) {
        stats.setPierce(Math.min(stats.getPierce() + PIERCE_PER_PICK, MAX_PIERCE));
    }
}
