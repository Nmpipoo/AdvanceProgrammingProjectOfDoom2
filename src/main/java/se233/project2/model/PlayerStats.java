package se233.project2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Everything about the player during one run: lives, score, and the numbers upgrades change.
// Fields are package-private so Upgrade and GameWorld can change them directly.
public class PlayerStats {

    public static final int STARTING_LIVES = 3;
    // Each brick broken in a row without touching the paddle is worth 10% more, up to double.
    public static final double COMBO_STEP = 0.1;
    public static final int MAX_COMBO_STEPS = 10;

    int lives = STARTING_LIVES;
    int score;
    int combo;
    double paddleWidth = Paddle.DEFAULT_WIDTH;
    double paddleSpeed = Paddle.DEFAULT_SPEED;
    double ballSpeed = Ball.DEFAULT_SPEED;
    int extraBalls;
    // Bricks the ball smashes straight through after each paddle hit.
    int pierce;
    double scoreMultiplier = 1.0;
    final List<String> upgradesTaken = new ArrayList<>();

    // Adds a broken brick's points with the combo bonus and multiplier; returns the points awarded.
    public int addBrickPoints(int basePoints) {
        combo++;
        double comboBonus = 1 + COMBO_STEP * Math.min(combo - 1, MAX_COMBO_STEPS);
        int awarded = (int) Math.round(basePoints * comboBonus * scoreMultiplier);
        score += awarded;
        return awarded;
    }

    public void resetCombo() {
        combo = 0;
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public int getCombo() {
        return combo;
    }

    public List<String> getUpgradesTaken() {
        return Collections.unmodifiableList(upgradesTaken);
    }
}
