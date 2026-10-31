package se233.project2.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreManagerTest {

    private ScoreManager score;

    @BeforeEach
    void setUp() {
        score = new ScoreManager();
    }

    @Test
    void firstBrickIsWorthItsBasePoints() {
        assertEquals(50, score.brickDestroyed(50));
        assertEquals(50, score.getScore());
        assertEquals(1, score.getCombo());
    }

    @Test
    void comboAddsTenPercentPerBrick() {
        score.brickDestroyed(100); // 100
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
    void resetComboStartsTheBonusOver() {
        score.brickDestroyed(100);
        score.brickDestroyed(100);
        score.resetCombo();
        assertEquals(0, score.getCombo());
        assertEquals(100, score.brickDestroyed(100));
        assertEquals(310, score.getScore());
    }

    @Test
    void multiplierScalesAwardedPoints() {
        score.setMultiplier(1.5);
        assertEquals(75, score.brickDestroyed(50));
    }

    @Test
    void resetClearsEverything() {
        score.setMultiplier(2);
        score.brickDestroyed(100);
        score.reset();
        assertEquals(0, score.getScore());
        assertEquals(0, score.getCombo());
        assertEquals(1.0, score.getMultiplier());
    }
}
