package se233.project2.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

// The upgrades offered after a stage is cleared. Each changes PlayerStats for the rest
// of the run, and stops being offered once it reaches its cap.
public enum Upgrade {
    WIDE_PADDLE("Wide Paddle", "Paddle 25% wider",
            s -> s.paddleWidth < 200, s -> s.paddleWidth = Math.min(s.paddleWidth * 1.25, 200)),
    QUICK_PADDLE("Quick Paddle", "Paddle moves 20% faster",
            s -> s.paddleSpeed < 900, s -> s.paddleSpeed = Math.min(s.paddleSpeed * 1.2, 900)),
    SLOW_BALL("Slow Ball", "Ball 15% slower, easier to catch",
            s -> s.ballSpeed > 240, s -> s.ballSpeed = Math.max(s.ballSpeed * 0.85, 240)),
    // A risk-for-reward pick: harder to keep the ball alive, but every brick is worth more.
    FAST_BALL("Fast Ball", "Ball 15% faster, +25% points",
            s -> s.ballSpeed < 560, s -> {
                s.ballSpeed = Math.min(s.ballSpeed * 1.15, 560);
                s.scoreMultiplier += 0.25;
            }),
    EXTRA_BALL("Extra Ball", "Launch one more ball each time",
            s -> s.extraBalls < 2, s -> s.extraBalls++),
    EXTRA_LIFE("Extra Life", "Gain one life",
            s -> s.lives < 6, s -> s.lives++),
    PIERCING_BALL("Piercing Ball", "After each paddle hit, smash through 2 bricks",
            s -> s.pierce < 6, s -> s.pierce = Math.min(s.pierce + 2, 6)),
    SCORE_BOOST("Score Boost", "+50% points from bricks",
            s -> s.scoreMultiplier < 3, s -> s.scoreMultiplier = Math.min(s.scoreMultiplier + 0.5, 3));

    public static final int CHOICES = 3;
    private static final Logger logger = LogManager.getLogger(Upgrade.class);

    private final String title;
    private final String description;
    private final Predicate<PlayerStats> available;
    private final Consumer<PlayerStats> effect;

    Upgrade(String title, String description, Predicate<PlayerStats> available, Consumer<PlayerStats> effect) {
        this.title = title;
        this.description = description;
        this.available = available;
        this.effect = effect;
    }

    // Up to CHOICES different random upgrades, skipping any that are maxed out.
    public static List<Upgrade> roll(PlayerStats stats, Random random) {
        List<Upgrade> choices = new ArrayList<>();
        for (Upgrade upgrade : values()) {
            if (upgrade.canApply(stats)) {
                choices.add(upgrade);
            }
        }
        Collections.shuffle(choices, random);
        choices = List.copyOf(choices.subList(0, Math.min(CHOICES, choices.size())));
        logger.debug("Offering upgrades {}", choices);
        return choices;
    }

    public boolean canApply(PlayerStats stats) {
        return available.test(stats);
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
