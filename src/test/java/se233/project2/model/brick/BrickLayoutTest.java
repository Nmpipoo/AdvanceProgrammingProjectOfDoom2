package se233.project2.model.brick;

import org.junit.jupiter.api.Test;
import se233.project2.exception.GameException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrickLayoutTest {

    @Test
    void buildsOneBrickPerLetterAndSkipsGaps() {
        List<Brick> bricks = BrickLayout.fromRows("N.S", "B U");
        assertEquals(4, bricks.size());
        assertInstanceOf(NormalBrick.class, bricks.get(0));
        assertInstanceOf(StrongBrick.class, bricks.get(1));
        assertInstanceOf(BonusBrick.class, bricks.get(2));
        assertInstanceOf(UnbreakableBrick.class, bricks.get(3));
    }

    @Test
    void placesBricksOnTheGrid() {
        List<Brick> bricks = BrickLayout.fromRows("N.N", ".N.");
        assertEquals(BrickLayout.LEFT, bricks.get(0).getX(), 1e-9);
        assertEquals(BrickLayout.TOP, bricks.get(0).getY(), 1e-9);
        assertEquals(BrickLayout.LEFT + 2 * (Brick.WIDTH + BrickLayout.GAP), bricks.get(1).getX(), 1e-9);
        assertEquals(BrickLayout.TOP + Brick.HEIGHT + BrickLayout.GAP, bricks.get(2).getY(), 1e-9);
    }

    @Test
    void fullRowFitsInsideThePlayArea() {
        List<Brick> bricks = BrickLayout.fromRows("NNNNNNNNNNNN");
        Brick last = bricks.get(bricks.size() - 1);
        assertEquals(BrickLayout.LEFT, se233.project2.model.GameWorld.WIDTH - (last.getX() + last.getWidth()), 1e-9);
    }

    @Test
    void unknownLetterThrowsGameException() {
        GameException e = assertThrows(GameException.class, () -> BrickLayout.fromRows("NNX"));
        assertEquals("Unknown brick 'X' at row 1, column 3", e.getMessage());
    }

    @Test
    void tooLongRowThrowsGameException() {
        assertThrows(GameException.class, () -> BrickLayout.fromRows("NNNNNNNNNNNNN"));
    }
}
