package se233.project2.model;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameLoop;
import se233.project2.controller.InputHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Rubric: unit tests for scoring.
class ScoringTest {

    private final PlayerStats stats = new PlayerStats();

    @Test
    void breakingBricksInTheGameRaisesTheScore() {
        GameWorld world = new GameWorld();
        InputHandler input = new InputHandler();
        input.press(KeyCode.ENTER);
        world.update(GameLoop.STEP, input);
        input.release(KeyCode.ENTER);
        int bricksAtStart = world.getBricks().size();
        input.press(KeyCode.SPACE);
        for (int i = 0; i < 300 && world.getScore() == 0; i++) {
            world.update(GameLoop.STEP, input);
        }
        assertTrue(world.getScore() > 0);
        assertTrue(world.getBricks().size() < bricksAtStart);
    }

    @Test
    void tougherBricksAreWorthMore() {
        assertEquals(50, new Brick(Brick.Type.NORMAL, 0, 0).getPoints());
        assertEquals(150, new Brick(Brick.Type.STRONG, 0, 0).getPoints());
        assertEquals(300, new Brick(Brick.Type.BONUS, 0, 0).getPoints());
        assertEquals(0, new Brick(Brick.Type.UNBREAKABLE, 0, 0).getPoints());
    }

    @Test
    void eachBrickInAComboIsWorthTenPercentMore() {
        assertEquals(100, stats.addBrickPoints(100));
        assertEquals(110, stats.addBrickPoints(100));
        assertEquals(120, stats.addBrickPoints(100));
        assertEquals(330, stats.getScore());
        assertEquals(3, stats.getCombo());
    }

    @Test
    void comboBonusIsCappedAtDoublePoints() {
        for (int i = 0; i < 20; i++) {
            stats.addBrickPoints(100);
        }
        assertEquals(200, stats.addBrickPoints(100));
    }

    @Test
    void hittingThePaddleResetsTheCombo() {
        stats.addBrickPoints(100);
        stats.addBrickPoints(100);
        stats.resetCombo();
        assertEquals(100, stats.addBrickPoints(100));
        assertEquals(310, stats.getScore());
    }

    @Test
    void scoreMultiplierScalesThePoints() {
        stats.scoreMultiplier = 1.5;
        assertEquals(75, stats.addBrickPoints(50));
    }
}
