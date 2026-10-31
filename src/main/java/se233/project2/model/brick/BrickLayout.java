package se233.project2.model.brick;

import se233.project2.exception.GameException;
import se233.project2.model.GameWorld;

import java.util.ArrayList;
import java.util.List;

// Builds bricks from rows of letters, one letter per column:
// N normal, S strong, B bonus, U unbreakable, '.' or space for an empty slot.
public final class BrickLayout {

    public static final int COLUMNS = 12;
    public static final double GAP = 4;
    public static final double LEFT = (GameWorld.WIDTH - (COLUMNS * (Brick.WIDTH + GAP) - GAP)) / 2;
    public static final double TOP = GameWorld.HUD_HEIGHT + 50;

    private BrickLayout() {
    }

    public static List<Brick> fromRows(String... rows) {
        List<Brick> bricks = new ArrayList<>();
        for (int row = 0; row < rows.length; row++) {
            String line = rows[row];
            if (line.length() > COLUMNS) {
                throw new GameException("Row " + (row + 1) + " has " + line.length()
                        + " bricks; the most is " + COLUMNS);
            }
            for (int col = 0; col < line.length(); col++) {
                double x = LEFT + col * (Brick.WIDTH + GAP);
                double y = TOP + row * (Brick.HEIGHT + GAP);
                char symbol = line.charAt(col);
                switch (symbol) {
                    case '.', ' ' -> {
                    }
                    case 'N' -> bricks.add(new NormalBrick(x, y));
                    case 'S' -> bricks.add(new StrongBrick(x, y));
                    case 'B' -> bricks.add(new BonusBrick(x, y));
                    case 'U' -> bricks.add(new UnbreakableBrick(x, y));
                    default -> throw new GameException("Unknown brick '" + symbol + "' at row "
                            + (row + 1) + ", column " + (col + 1));
                }
            }
        }
        return bricks;
    }
}
