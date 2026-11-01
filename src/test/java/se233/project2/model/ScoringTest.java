package se233.project2.model;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import se233.project2.controller.GameLoop;
import se233.project2.controller.InputHandler;
import se233.project2.model.brick.BonusBrick;
import se233.project2.model.brick.NormalBrick;
import se233.project2.model.brick.StrongBrick;
import se233.project2.model.brick.UnbreakableBrick;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Rubric: unit tests for scoring.
class ScoringTest {

    private final ScoreManager score = new ScoreManager();

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
        assertEquals(50, new NormalBrick(0, 0).getPoints());
        assertEquals(150, new StrongBrick(0, 0).getPoints());
        assertEquals(300, new BonusBrick(0, 0).getPoints());
        assertEquals(0, new UnbreakableBrick(0, 0).getPoints());
    }

    @Test
    void eachBrickInAComboIsWorthTenPercentMore() {
        assertEquals(100, score.brickDestroyed(100));
        assertEquals(110, score.brickDestroyed(100));
        assertEquals(120, score.brickDestroyed(100));
        assertEquals(330, score.getScore());
        assertEquals(3, score.getCombo());
    }

    @Test
    void comboBonusIsCappedAtDoublePoints() {
        for (int i = 0; i < 20; i++) {
            score.brickDestroyed(100);
        }
        assertEquals(200, score.brickDestroyed(100));
    }

    @Test
    void hittingThePaddleResetsTheCombo() {
        score.brickDestroyed(100);
        score.brickDestroyed(100);
        score.resetCombo();
        assertEquals(100, score.brickDestroyed(100));
        assertEquals(310, score.getScore());
    }

    @Test
    void scoreMultiplierScalesThePoints() {
        score.setMultiplier(1.5);
        assertEquals(75, score.brickDestroyed(50));
    }
}
