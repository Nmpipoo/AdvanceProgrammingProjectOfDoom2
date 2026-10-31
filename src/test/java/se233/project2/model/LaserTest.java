package se233.project2.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameLoop;
import se233.project2.model.brick.Brick;
import se233.project2.model.brick.NormalBrick;
import se233.project2.model.brick.StrongBrick;
import se233.project2.model.brick.UnbreakableBrick;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaserTest {

    private Laser laser;
    private Paddle paddle;

    @BeforeEach
    void setUp() {
        laser = new Laser();
        paddle = new Paddle();
    }

    // Fires one pair of bolts, then lets them fly until one hits something.
    private List<Brick> fireUntilHit(List<Brick> bricks) {
        for (int i = 0; i < 120; i++) {
            List<Brick> hits = laser.update(GameLoop.STEP, paddle, i == 0, bricks);
            if (!hits.isEmpty()) {
                return hits;
            }
        }
        throw new AssertionError("No bolt hit anything");
    }

    @Test
    void doesNotFireUntilActivated() {
        laser.update(GameLoop.STEP, paddle, true, List.of());
        assertFalse(laser.isActive());
        assertTrue(laser.getBolts().isEmpty());
    }

    @Test
    void firesAPairOfBoltsFromBothEndsOfThePaddle() {
        laser.activate();
        laser.update(GameLoop.STEP, paddle, true, List.of());
        List<LaserBolt> bolts = laser.getBolts();
        assertEquals(2, bolts.size());
        assertTrue(bolts.get(0).getCenterX() < paddle.getCenterX());
        assertTrue(bolts.get(1).getCenterX() > paddle.getCenterX());
        assertTrue(bolts.get(0).getY() < paddle.getY());
    }

    @Test
    void waitsForTheCooldownBetweenShots() {
        laser.activate();
        laser.update(GameLoop.STEP, paddle, true, List.of());
        laser.update(GameLoop.STEP, paddle, true, List.of());
        assertEquals(2, laser.getBolts().size());
        for (double t = 0; t < Laser.COOLDOWN; t += GameLoop.STEP) {
            laser.update(GameLoop.STEP, paddle, true, List.of());
        }
        assertEquals(4, laser.getBolts().size());
    }

    @Test
    void onlyFiresWhileTheButtonIsHeld() {
        laser.activate();
        laser.update(GameLoop.STEP, paddle, false, List.of());
        assertTrue(laser.getBolts().isEmpty());
    }

    @Test
    void boltDamagesTheFirstBrickItHitsAndDisappears() {
        laser.activate();
        // A strong brick right above the left cannon.
        Brick brick = new StrongBrick(paddle.getX() + Laser.CANNON_INSET - Brick.WIDTH / 2, 200);
        List<Brick> bricks = new ArrayList<>(List.of(brick));
        List<Brick> hits = fireUntilHit(bricks);
        assertEquals(List.of(brick), hits);
        assertEquals(2, brick.getHitPoints());
        assertEquals(1, laser.getBolts().size(), "the right-hand bolt is still flying");
    }

    @Test
    void unbreakableBricksBlockBolts() {
        laser.activate();
        Brick wall = new UnbreakableBrick(paddle.getX() + Laser.CANNON_INSET - Brick.WIDTH / 2, 200);
        fireUntilHit(new ArrayList<>(List.of(wall)));
        assertFalse(wall.isDestroyed());
    }

    @Test
    void boltsLeaveAtTheTopOfThePlayArea() {
        laser.activate();
        laser.update(GameLoop.STEP, paddle, true, List.of());
        for (int i = 0; i < 120; i++) {
            laser.update(GameLoop.STEP, paddle, false, List.of());
        }
        assertTrue(laser.getBolts().isEmpty());
    }

    @Test
    void wearsOffAfterItsDuration() {
        laser.activate();
        for (double t = 0; t < Laser.DURATION + 0.1; t += GameLoop.STEP) {
            laser.update(GameLoop.STEP, paddle, false, List.of());
        }
        assertFalse(laser.isActive());
        laser.update(GameLoop.STEP, paddle, true, List.of(new NormalBrick(0, 100)));
        assertTrue(laser.getBolts().isEmpty());
    }

    @Test
    void resetTurnsItOffAndClearsBolts() {
        laser.activate();
        laser.update(GameLoop.STEP, paddle, true, List.of());
        laser.reset();
        assertFalse(laser.isActive());
        assertTrue(laser.getBolts().isEmpty());
    }
}
