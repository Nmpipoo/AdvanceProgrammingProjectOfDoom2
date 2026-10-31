package se233.project2.model.brick;

// A wall the ball bounces off. It never breaks and isn't needed to clear a stage.
public class UnbreakableBrick extends Brick {

    public UnbreakableBrick(double x, double y) {
        super(x, y, 1, 0);
    }

    @Override
    public boolean isBreakable() {
        return false;
    }

    @Override
    public BrickType getType() {
        return BrickType.UNBREAKABLE;
    }
}
