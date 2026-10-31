package se233.project2.model;

// Anything in the play area with a position and an axis-aligned box.
public abstract class GameObject {

    protected double x;
    protected double y;
    protected double width;
    protected double height;

    protected GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean intersects(GameObject other) {
        return x < other.x + other.width && x + width > other.x
                && y < other.y + other.height && y + height > other.y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getCenterX() {
        return x + width / 2;
    }

    public double getCenterY() {
        return y + height / 2;
    }
}
