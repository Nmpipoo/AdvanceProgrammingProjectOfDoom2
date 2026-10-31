package se233.project2.model;

// One shot from the paddle's laser cannons. Flies straight up.
public class LaserBolt extends GameObject {

    public static final double WIDTH = 6;
    public static final double HEIGHT = 18;
    public static final double SPEED = 720;

    public LaserBolt(double centerX, double top) {
        super(centerX - WIDTH / 2, top, WIDTH, HEIGHT);
    }

    public void fly(double dt) {
        y -= SPEED * dt;
    }
}
