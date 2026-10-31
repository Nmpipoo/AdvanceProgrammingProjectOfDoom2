package se233.project2.model;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameLoop;
import se233.project2.controller.InputHandler;
import se233.project2.model.brick.Brick;
import se233.project2.model.brick.BrickType;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerUpAndEffectsTest {

    private GameWorld world;
    private InputHandler input;

    @BeforeEach
    void setUp() {
        // A bonus brick in column 6 sits right above the paddle's starting spot,
        // and the normal bricks keep the stage from being cleared.
        world = new GameWorld(List.<String[]>of(new String[]{".....B", "N..........N"}), new Random(1));
        input = new InputHandler();
        input.press(KeyCode.ENTER);
        world.update(GameLoop.STEP, input);
        input.release(KeyCode.ENTER);
    }

    // Launches the ball and sends it into the bonus brick from below.
    private void breakBonusBrick() {
        Brick bonus = world.getBricks().stream().filter(b -> b.getType() == BrickType.BONUS).findFirst().orElseThrow();
        input.press(KeyCode.SPACE);
        world.update(GameLoop.STEP, input);
        input.release(KeyCode.SPACE);
        Ball ball = world.getBall();
        ball.x = bonus.getCenterX() - Ball.RADIUS;
        ball.y = bonus.getY() + bonus.getHeight() + 1;
        world.update(GameLoop.STEP, input);
        // Park the ball on the paddle so it can't be lost while the capsule falls.
        ball.reset();
    }

    private void runSeconds(double seconds) {
        for (double t = 0; t < seconds; t += GameLoop.STEP) {
            world.update(GameLoop.STEP, input);
        }
    }

    @Test
    void breakingABrickShowsAnExplosionAndItsPoints() {
        breakBonusBrick();
        Effects effects = world.getEffects();
        assertEquals(1, effects.getExplosions().size());
        assertEquals("+300", effects.getTexts().get(0).text());
    }

    @Test
    void bonusBrickDropsALaserCapsule() {
        breakBonusBrick();
        assertEquals(1, world.getPowerUps().size());
    }

    @Test
    void catchingTheCapsuleTurnsOnTheLaser() {
        breakBonusBrick();
        runSeconds(4);
        assertTrue(world.getPowerUps().isEmpty());
        assertTrue(world.getLaser().isActive());
    }

    @Test
    void missedCapsuleFallsOffTheScreen() {
        breakBonusBrick();
        input.press(KeyCode.LEFT);
        runSeconds(4);
        input.release(KeyCode.LEFT);
        assertTrue(world.getPowerUps().isEmpty());
        assertFalse(world.getLaser().isActive());
    }

    @Test
    void losingALifeTurnsTheLaserOff() {
        breakBonusBrick();
        runSeconds(4);
        input.press(KeyCode.SPACE);
        world.update(GameLoop.STEP, input);
        input.release(KeyCode.SPACE);
        world.getBall().y = GameWorld.HEIGHT + 10;
        world.update(GameLoop.STEP, input);
        assertFalse(world.getLaser().isActive());
    }

    @Test
    void effectsExpire() {
        Effects effects = new Effects();
        effects.explode(10, 10);
        effects.floatText("+50", 10, 100);
        effects.update(Effects.EXPLOSION_SECONDS / 2);
        assertEquals(1, effects.getExplosions().size());
        assertTrue(effects.getTexts().get(0).centerY() < 100, "score text floats upwards");
        effects.update(Effects.TEXT_SECONDS);
        assertTrue(effects.getExplosions().isEmpty());
        assertTrue(effects.getTexts().isEmpty());
    }
}
