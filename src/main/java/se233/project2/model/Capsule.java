package se233.project2.model;

// The Big Ball power-up. It drops from a broken brick; catching it with the paddle makes the ball bigger.
public class Capsule extends GameObject {

    public static final double FALL_SPEED = 160;

    public Capsule(double centerX, double centerY) {
        super(centerX - 18, centerY - 8, 36, 16);
    }

    public void fall(double dt) {
        y += FALL_SPEED * dt;
    }
}
