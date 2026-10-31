package se233.project2.model.brick;

import se233.project2.model.GameObject;

// A brick loses one hit point per hit and is destroyed at zero.
// Subclasses decide how tough it is, what it is worth and whether it can break at all.
public abstract class Brick extends GameObject {

    public static final double WIDTH = 60;
    public static final double HEIGHT = 22;

    // How long a brick flashes white after being hit.
    public static final double FLASH_SECONDS = 0.12;

    private final int maxHitPoints;
    private final int points;
    private int hitPoints;
    private double flashTime;

    protected Brick(double x, double y, int hitPoints, int points) {
        super(x, y, WIDTH, HEIGHT);
        this.maxHitPoints = hitPoints;
        this.hitPoints = hitPoints;
        this.points = points;
    }

    // Returns true if this hit destroyed the brick.
    public boolean hit() {
        flashTime = FLASH_SECONDS;
        if (!isBreakable() || isDestroyed()) {
            return false;
        }
        hitPoints--;
        return isDestroyed();
    }

    // Breaks the brick outright, whatever its hit points (used by the piercing ball).
    public boolean destroy() {
        if (!isBreakable() || isDestroyed()) {
            return false;
        }
        hitPoints = 0;
        return true;
    }

    public boolean isDestroyed() {
        return isBreakable() && hitPoints <= 0;
    }

    public boolean isBreakable() {
        return true;
    }

    public void tick(double dt) {
        flashTime = Math.max(0, flashTime - dt);
    }

    public boolean isFlashing() {
        return flashTime > 0;
    }

    public abstract BrickType getType();

    public int getHitPoints() {
        return hitPoints;
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public int getPoints() {
        return points;
    }
}
