package se233.project2.model.upgrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se233.project2.model.PlayerStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Rolls the random upgrade choices shown after a stage and applies the one picked.
public class UpgradeManager {

    public static final int CHOICES = 3;

    private static final Logger logger = LogManager.getLogger(UpgradeManager.class);

    private final List<Upgrade> pool = List.of(
            new WidePaddle(),
            new QuickPaddle(),
            new SlowBall(),
            new FastBall(),
            new ExtraBall(),
            new ExtraLife(),
            new PiercingBall(),
            new ScoreBoost());
    private final Random random;

    public UpgradeManager(Random random) {
        this.random = random;
    }

    // Up to CHOICES different upgrades, skipping any that are already maxed out.
    public List<Upgrade> rollChoices(PlayerStats stats) {
        List<Upgrade> available = new ArrayList<>(pool.stream().filter(u -> u.canApply(stats)).toList());
        Collections.shuffle(available, random);
        List<Upgrade> choices = available.subList(0, Math.min(CHOICES, available.size()));
        logger.debug("Offering upgrades {}", choices.stream().map(Upgrade::getName).toList());
        return List.copyOf(choices);
    }

    public void apply(Upgrade upgrade, PlayerStats stats) {
        upgrade.apply(stats);
        stats.recordUpgrade(upgrade.getName());
        logger.info("Player chose upgrade: {}", upgrade.getName());
    }

    public List<Upgrade> getPool() {
        return pool;
    }
}
