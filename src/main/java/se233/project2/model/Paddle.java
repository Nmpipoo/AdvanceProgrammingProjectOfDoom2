package se233.project2.model;

// The player's paddle. It only moves sideways and stays inside the play area.
public class Paddle extends GameObject {

    public static final double DEFAULT_WIDTH = 100;
    public static final double HEIGHT = 14;
    public static final double DEFAULT_SPEED = 480;
    public static final double Y = GameWorld.HEIGHT - 50;

    private double speed = DEFAULT_SPEED;
    // How long the paddle has been moving; drives its move animation. 0 while standing still.
    private double moveTime;

    public Paddle() {
        super((GameWorld.WIDTH - DEFAULT_WIDTH) / 2, Y, DEFAULT_WIDTH, HEIGHT);
    }

    // direction is -1 (left), 0 (stay) or 1 (right).
    public void move(int direction, double dt) {
        double before = x;
        x = Math.clamp(x + direction * speed * dt, 0, GameWorld.WIDTH - width);
        moveTime = x != before ? moveTime + dt : 0;
    }

    public boolean isMoving() {
        return moveTime > 0;
    }

    public double getMoveTime() {
        return moveTime;
    }

    // Upgrades change size and speed; resizing keeps the paddle centered where it was.
    public void applyStats(PlayerStats stats) {
        double center = getCenterX();
        width = stats.paddleWidth;
        speed = stats.paddleSpeed;
        x = Math.clamp(center - width / 2, 0, GameWorld.WIDTH - width);
    }

    public void recenter() {
        x = (GameWorld.WIDTH - width) / 2;
    }

    public double getSpeed() {
        return speed;
    }
}
