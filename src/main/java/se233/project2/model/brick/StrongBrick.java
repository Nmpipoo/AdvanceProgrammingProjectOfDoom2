package se233.project2.model.brick;

// Takes three hits and is worth more.
public class StrongBrick extends Brick {

    public StrongBrick(double x, double y) {
        super(x, y, 3, 150);
    }

    @Override
    public BrickType getType() {
        return BrickType.STRONG;
    }
}
