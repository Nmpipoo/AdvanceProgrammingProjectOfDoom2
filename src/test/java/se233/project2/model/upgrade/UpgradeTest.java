package se233.project2.model.upgrade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se233.project2.model.Ball;
import se233.project2.model.Paddle;
import se233.project2.model.PlayerStats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpgradeTest {

    private PlayerStats stats;

    @BeforeEach
    void setUp() {
        stats = new PlayerStats();
    }

    private static void applyUntilMaxed(Upgrade upgrade, PlayerStats stats) {
        for (int i = 0; i < 50 && upgrade.canApply(stats); i++) {
            upgrade.apply(stats);
        }
        assertFalse(upgrade.canApply(stats), upgrade.getName() + " never reached its cap");
    }

    @Test
    void widePaddleGrowsThePaddleUpToTheCap() {
        Upgrade upgrade = new WidePaddle();
        upgrade.apply(stats);
        assertEquals(Paddle.DEFAULT_WIDTH * WidePaddle.FACTOR, stats.getPaddleWidth(), 1e-9);
        applyUntilMaxed(upgrade, stats);
        assertEquals(WidePaddle.MAX_WIDTH, stats.getPaddleWidth(), 1e-9);
    }

    @Test
    void quickPaddleSpeedsUpThePaddleUpToTheCap() {
        Upgrade upgrade = new QuickPaddle();
        upgrade.apply(stats);
        assertEquals(Paddle.DEFAULT_SPEED * QuickPaddle.FACTOR, stats.getPaddleSpeed(), 1e-9);
        applyUntilMaxed(upgrade, stats);
        assertEquals(QuickPaddle.MAX_SPEED, stats.getPaddleSpeed(), 1e-9);
    }

    @Test
    void slowBallSlowsTheBallDownToTheFloor() {
        Upgrade upgrade = new SlowBall();
        upgrade.apply(stats);
        assertEquals(Ball.DEFAULT_SPEED * SlowBall.FACTOR, stats.getBallSpeed(), 1e-9);
        applyUntilMaxed(upgrade, stats);
        assertEquals(SlowBall.MIN_SPEED, stats.getBallSpeed(), 1e-9);
    }

    @Test
    void fastBallTradesSpeedForPoints() {
        new FastBall().apply(stats);
        assertEquals(Ball.DEFAULT_SPEED * FastBall.FACTOR, stats.getBallSpeed(), 1e-9);
        assertEquals(1 + FastBall.SCORE_BONUS, stats.getScoreMultiplier(), 1e-9);
        applyUntilMaxed(new FastBall(), stats);
        assertEquals(FastBall.MAX_SPEED, stats.getBallSpeed(), 1e-9);
    }

    @Test
    void extraBallAddsBallsUpToTheCap() {
        Upgrade upgrade = new ExtraBall();
        upgrade.apply(stats);
        assertEquals(1, stats.getExtraBalls());
        applyUntilMaxed(upgrade, stats);
        assertEquals(ExtraBall.MAX_EXTRA, stats.getExtraBalls());
    }

    @Test
    void extraLifeAddsALifeUpToTheCap() {
        Upgrade upgrade = new ExtraLife();
        upgrade.apply(stats);
        assertEquals(PlayerStats.STARTING_LIVES + 1, stats.getLives());
        applyUntilMaxed(upgrade, stats);
        assertEquals(ExtraLife.MAX_LIVES, stats.getLives());
    }

    @Test
    void piercingBallAddsChargesUpToTheCap() {
        Upgrade upgrade = new PiercingBall();
        upgrade.apply(stats);
        assertEquals(PiercingBall.PIERCE_PER_PICK, stats.getPierce());
        applyUntilMaxed(upgrade, stats);
        assertEquals(PiercingBall.MAX_PIERCE, stats.getPierce());
    }

    @Test
    void scoreBoostRaisesTheMultiplierUpToTheCap() {
        Upgrade upgrade = new ScoreBoost();
        upgrade.apply(stats);
        assertEquals(1 + ScoreBoost.BONUS, stats.getScoreMultiplier(), 1e-9);
        applyUntilMaxed(upgrade, stats);
        assertEquals(ScoreBoost.MAX_MULTIPLIER, stats.getScoreMultiplier(), 1e-9);
    }

    @Test
    void everyUpgradeHasANameAndDescription() {
        for (Upgrade upgrade : new UpgradeManager(new java.util.Random(1)).getPool()) {
            assertFalse(upgrade.getName().isBlank());
            assertFalse(upgrade.getDescription().isBlank());
            assertTrue(upgrade.canApply(new PlayerStats()), upgrade.getName() + " should be available at the start");
        }
    }
}
