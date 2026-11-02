package se233.project2.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

// The upgrades offered after a stage is cleared. Each changes PlayerStats for the rest of the run.
// There is no cap: with 4 stages the player can pick at most 3 upgrades.
public enum Upgrade {
    WIDE_PADDLE("Wide Paddle", "Paddle 25% wider", s -> s.paddleWidth *= 1.25),
    QUICK_PADDLE("Quick Paddle", "Paddle moves 20% faster", s -> s.paddleSpeed *= 1.2),
    SCORE_BOOST("Score Boost", "+50% points from bricks", s -> s.scoreMultiplier += 0.5);

    private static final Logger logger = LogManager.getLogger(Upgrade.class);

    private final String title;
    private final String description;
    private final Consumer<PlayerStats> effect;

    Upgrade(String title, String description, Consumer<PlayerStats> effect) {
        this.title = title;
        this.description = description;
        this.effect = effect;
    }

    // All upgrades, in a random order for the cards.
    public static List<Upgrade> roll(Random random) {
        List<Upgrade> choices = new ArrayList<>(List.of(values()));
        Collections.shuffle(choices, random);
        logger.debug("Offering upgrades {}", choices);
        return List.copyOf(choices);
    }

    public void apply(PlayerStats stats) {
        effect.accept(stats);
        stats.upgradesTaken.add(title);
        logger.info("Player chose upgrade: {}", title);
    }

    public String getName() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
