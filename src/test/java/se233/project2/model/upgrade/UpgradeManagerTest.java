package se233.project2.model.upgrade;

import org.junit.jupiter.api.Test;
import se233.project2.model.PlayerStats;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpgradeManagerTest {

    @Test
    void offersThreeDifferentUpgrades() {
        UpgradeManager manager = new UpgradeManager(new Random(42));
        List<Upgrade> choices = manager.rollChoices(new PlayerStats());
        assertEquals(UpgradeManager.CHOICES, choices.size());
        assertEquals(UpgradeManager.CHOICES, new HashSet<>(choices).size());
    }

    @Test
    void choicesAreRandomAcrossRolls() {
        UpgradeManager manager = new UpgradeManager(new Random(7));
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            manager.rollChoices(new PlayerStats()).forEach(u -> seen.add(u.getName()));
        }
        assertEquals(manager.getPool().size(), seen.size());
    }

    @Test
    void maxedOutUpgradesAreNotOffered() {
        UpgradeManager manager = new UpgradeManager(new Random(3));
        PlayerStats stats = new PlayerStats();
        stats.setPaddleWidth(WidePaddle.MAX_WIDTH);
        stats.setExtraBalls(ExtraBall.MAX_EXTRA);
        for (int i = 0; i < 30; i++) {
            for (Upgrade upgrade : manager.rollChoices(stats)) {
                assertFalse(upgrade instanceof WidePaddle);
                assertFalse(upgrade instanceof ExtraBall);
            }
        }
    }

    @Test
    void applyingRecordsTheUpgrade() {
        UpgradeManager manager = new UpgradeManager(new Random(1));
        PlayerStats stats = new PlayerStats();
        manager.apply(new ScoreBoost(), stats);
        manager.apply(new ScoreBoost(), stats);
        assertEquals(List.of("Score Boost", "Score Boost"), stats.getUpgradesTaken());
        assertTrue(stats.getScoreMultiplier() > 1.5);
    }
}
