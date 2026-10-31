package se233.project2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Everything about the player that carries over from stage to stage during one run.
// Upgrades change these numbers; GameWorld applies them to the paddle, balls and score.
public class PlayerStats {

    public static final int STARTING_LIVES = 3;

    private int lives = STARTING_LIVES;
    private double paddleWidth = Paddle.DEFAULT_WIDTH;
    private double paddleSpeed = Paddle.DEFAULT_SPEED;
    private double ballSpeed = Ball.DEFAULT_SPEED;
    private int extraBalls;
    private int pierce;
    private double scoreMultiplier = 1.0;
    private final List<String> upgradesTaken = new ArrayList<>();

    public void loseLife() {
        lives--;
    }

    public void addLife() {
        lives++;
    }

    public void recordUpgrade(String name) {
        upgradesTaken.add(name);
    }

    public int getLives() {
        return lives;
    }

    public double getPaddleWidth() {
        return paddleWidth;
    }

    public void setPaddleWidth(double paddleWidth) {
        this.paddleWidth = paddleWidth;
    }

    public double getPaddleSpeed() {
        return paddleSpeed;
    }

    public void setPaddleSpeed(double paddleSpeed) {
        this.paddleSpeed = paddleSpeed;
    }

    public double getBallSpeed() {
        return ballSpeed;
    }

    public void setBallSpeed(double ballSpeed) {
        this.ballSpeed = ballSpeed;
    }

    public int getExtraBalls() {
        return extraBalls;
    }

    public void setExtraBalls(int extraBalls) {
        this.extraBalls = extraBalls;
    }

    // Bricks the ball smashes straight through after each paddle hit.
    public int getPierce() {
        return pierce;
    }

    public void setPierce(int pierce) {
        this.pierce = pierce;
    }

    public double getScoreMultiplier() {
        return scoreMultiplier;
    }

    public void setScoreMultiplier(double scoreMultiplier) {
        this.scoreMultiplier = scoreMultiplier;
    }

    public List<String> getUpgradesTaken() {
        return Collections.unmodifiableList(upgradesTaken);
    }
}
