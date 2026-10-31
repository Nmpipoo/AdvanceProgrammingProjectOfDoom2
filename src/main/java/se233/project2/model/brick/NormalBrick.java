package se233.project2.model.brick;

public class NormalBrick extends Brick {

    public NormalBrick(double x, double y) {
        super(x, y, 1, 50);
    }

    @Override
    public BrickType getType() {
        return BrickType.NORMAL;
    }
}
