package se233.project2.model.brick;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrickTest {

    @Test
    void normalBrickBreaksInOneHit() {
        Brick brick = new NormalBrick(0, 0);
        assertTrue(brick.hit());
        assertTrue(brick.isDestroyed());
    }

    @Test
    void strongBrickTakesThreeHits() {
        Brick brick = new StrongBrick(0, 0);
        assertFalse(brick.hit());
        assertFalse(brick.hit());
        assertEquals(1, brick.getHitPoints());
        assertTrue(brick.hit());
        assertTrue(brick.isDestroyed());
    }

    @Test
    void destroyedBrickIgnoresFurtherHits() {
        Brick brick = new NormalBrick(0, 0);
        brick.hit();
        assertFalse(brick.hit());
        assertEquals(0, brick.getHitPoints());
    }

    @Test
    void unbreakableBrickNeverBreaks() {
        Brick brick = new UnbreakableBrick(0, 0);
        for (int i = 0; i < 10; i++) {
            assertFalse(brick.hit());
        }
        assertFalse(brick.isDestroyed());
        assertFalse(brick.isBreakable());
    }

    @Test
    void hitBrickFlashesBriefly() {
        Brick brick = new StrongBrick(0, 0);
        assertFalse(brick.isFlashing());
        brick.hit();
        assertTrue(brick.isFlashing());
        brick.tick(Brick.FLASH_SECONDS);
        assertFalse(brick.isFlashing());
    }

    @Test
    void unbreakableBrickStillFlashesWhenHit() {
        Brick brick = new UnbreakableBrick(0, 0);
        brick.hit();
        assertTrue(brick.isFlashing());
    }

    @Test
    void tougherBricksAreWorthMore() {
        assertTrue(new StrongBrick(0, 0).getPoints() > new NormalBrick(0, 0).getPoints());
        assertTrue(new BonusBrick(0, 0).getPoints() > new StrongBrick(0, 0).getPoints());
        assertEquals(0, new UnbreakableBrick(0, 0).getPoints());
    }
}
