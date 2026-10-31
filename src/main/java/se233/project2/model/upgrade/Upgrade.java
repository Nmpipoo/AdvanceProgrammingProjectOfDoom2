package se233.project2.model.upgrade;

import se233.project2.model.PlayerStats;

// One choice offered after a stage is cleared. It changes PlayerStats for the rest of the run.
public interface Upgrade {

    String getName();

    String getDescription();

    // False once the upgrade has hit its cap, so it stops being offered.
    default boolean canApply(PlayerStats stats) {
        return true;
    }

    void apply(PlayerStats stats);
}
