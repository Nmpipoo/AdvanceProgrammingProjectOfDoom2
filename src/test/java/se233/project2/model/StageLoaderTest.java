package se233.project2.model;

import org.junit.jupiter.api.Test;
import se233.project2.exception.GameException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageLoaderTest {

    @Test
    void loadsEveryStageFileInOrder() {
        List<String[]> stages = StageLoader.loadAll();
        assertTrue(stages.size() >= 4);
    }

    @Test
    void skipsCommentsAndTrailingSpaces() {
        String[] rows = StageLoader.parse("test", List.of("# title", "NN  ", "# note", ".S"));
        assertArrayEquals(new String[]{"NN", ".S"}, rows);
    }

    @Test
    void unknownLetterNamesTheStageFile() {
        GameException e = assertThrows(GameException.class,
                () -> StageLoader.parse("stage9", List.of("NNQ")));
        assertEquals("stage9: Unknown brick 'Q' at row 1, column 3", e.getMessage());
    }

    @Test
    void stageWithOnlyUnbreakableBricksIsRejected() {
        GameException e = assertThrows(GameException.class,
                () -> StageLoader.parse("stage9", List.of("UUU", "...")));
        assertEquals("stage9 has no breakable bricks", e.getMessage());
    }

    @Test
    void emptyStageIsRejected() {
        assertThrows(GameException.class, () -> StageLoader.parse("stage9", List.of("# nothing here")));
    }
}
