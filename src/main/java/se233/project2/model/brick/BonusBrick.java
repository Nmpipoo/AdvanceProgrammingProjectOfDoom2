package se233.project2.model.brick;

// Breaks in one hit but is worth a lot. Phase 6 makes it drop power-up items.
public class BonusBrick extends Brick {

    public BonusBrick(double x, double y) {
        super(x, y, 1, 300);
    }

    @Override
    public BrickType getType() {
        return BrickType.BONUS;
    }
}
