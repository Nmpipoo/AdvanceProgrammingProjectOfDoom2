package se233.project2.model;

// A capsule that drops from a broken brick. Catch it with the paddle to get its effect.
public class PowerUp extends GameObject {

    public static final double WIDTH = 36;
    public static final double HEIGHT = 16;
    public static final double FALL_SPEED = 160;

    public PowerUp(double centerX, double centerY) {
        super(centerX - WIDTH / 2, centerY - HEIGHT / 2, WIDTH, HEIGHT);
    }

    public void fall(double dt) {
        y += FALL_SPEED * dt;
    }
}
