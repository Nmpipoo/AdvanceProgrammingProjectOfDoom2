package se233.project2.model;

import se233.project2.exception.GameException;

import java.util.ArrayList;
import java.util.List;

// A brick loses one hit point per hit and is destroyed at zero.
// Its type decides how tough it is, what it is worth and whether it can break at all.
public class Brick extends GameObject {

    public static final double WIDTH = 60;
    public static final double HEIGHT = 22;
    // How long a brick flashes white after being hit.
    public static final double FLASH_SECONDS = 0.12;

    // Stage grid: 12 columns, centered, starting below the HUD.
    public static final int COLUMNS = 12;
    public static final double GAP = 4;
    private static final double GRID_LEFT = (GameWorld.WIDTH - (COLUMNS * (WIDTH + GAP) - GAP)) / 2;
    private static final double GRID_TOP = GameWorld.HUD_HEIGHT + 50;

    // Each type's letter in a stage file, hit points and points.
    public enum Type {
        NORMAL('N', 1, 50),
        STRONG('S', 3, 150),
        BONUS('B', 1, 300),
        UNBREAKABLE('U', 0, 0);

        private final char symbol;
        private final int hitPoints;
        private final int points;

        Type(char symbol, int hitPoints, int points) {
            this.symbol = symbol;
            this.hitPoints = hitPoints;
            this.points = points;
        }
    }

    private final Type type;
    private int hitPoints;
    private double flashTime;

    public Brick(Type type, double x, double y) {
        super(x, y, WIDTH, HEIGHT);
        this.type = type;
        this.hitPoints = type.hitPoints;
    }

    // Builds bricks from rows of letters, one per column (N, S, B, U; '.' or space is empty).
    public static List<Brick> fromRows(String... rows) {
        List<Brick> bricks = new ArrayList<>();
        for (int row = 0; row < rows.length; row++) {
            String line = rows[row];
            if (line.length() > COLUMNS) {
                throw new GameException("Row " + (row + 1) + " has " + line.length() + " bricks; the most is " + COLUMNS);
            }
            for (int col = 0; col < line.length(); col++) {
                char symbol = line.charAt(col);
                if (symbol == '.' || symbol == ' ') {
                    continue;
                }
                Type type = null;
                for (Type candidate : Type.values()) {
                    if (candidate.symbol == symbol) {
                        type = candidate;
                    }
                }
                if (type == null) {
                    throw new GameException("Unknown brick '" + symbol + "' at row " + (row + 1) + ", column " + (col + 1));
                }
                bricks.add(new Brick(type, GRID_LEFT + col * (WIDTH + GAP), GRID_TOP + row * (HEIGHT + GAP)));
            }
        }
        return bricks;
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

    public boolean isDestroyed() {
        return isBreakable() && hitPoints <= 0;
    }

    public boolean isBreakable() {
        return type != Type.UNBREAKABLE;
    }

    public void tick(double dt) {
        flashTime = Math.max(0, flashTime - dt);
    }

    public boolean isFlashing() {
        return flashTime > 0;
    }

    public Type getType() {
        return type;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public int getMaxHitPoints() {
        return type.hitPoints;
    }

    public int getPoints() {
        return type.points;
    }
}
