package se233.project2.model;

// Keeps the score. Breaking bricks one after another without the ball touching
// the paddle builds a combo, and each step of combo adds 10% to a brick's points
// (up to +100%). The multiplier is there for the score upgrade in Phase 5.
public class ScoreManager {

    public static final double COMBO_STEP = 0.1;
    public static final int MAX_COMBO_BONUS_STEPS = 10;

    private int score;
    private int combo;
    private double multiplier = 1.0;

    // Returns the points actually awarded.
    public int brickDestroyed(int basePoints) {
        combo++;
        double comboBonus = 1 + COMBO_STEP * Math.min(combo - 1, MAX_COMBO_BONUS_STEPS);
        int awarded = (int) Math.round(basePoints * comboBonus * multiplier);
        score += awarded;
        return awarded;
    }

    public void resetCombo() {
        combo = 0;
    }

    public void reset() {
        score = 0;
        combo = 0;
        multiplier = 1.0;
    }

    public int getScore() {
        return score;
    }

    public int getCombo() {
        return combo;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }
}
