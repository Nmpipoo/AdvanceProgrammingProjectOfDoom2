package se233.project2.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se233.project2.exception.GameException;
import se233.project2.model.brick.Brick;
import se233.project2.model.brick.BrickLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Reads stage layouts from resources: stages/stage1.txt, stage2.txt, ... until one is missing.
// Each line is a row of bricks in BrickLayout's letters; lines starting with # are comments.
public final class StageLoader {

    private static final String STAGE_PATH = "/se233/project2/stages/stage%d.txt";
    private static final Logger logger = LogManager.getLogger(StageLoader.class);

    private StageLoader() {
    }

    public static List<String[]> loadAll() {
        List<String[]> stages = new ArrayList<>();
        for (int number = 1; ; number++) {
            String path = String.format(STAGE_PATH, number);
            try (InputStream in = StageLoader.class.getResourceAsStream(path)) {
                if (in == null) {
                    break;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                stages.add(parse("stage" + number, reader.lines().toList()));
            } catch (IOException e) {
                throw new GameException("Could not read " + path, e);
            }
        }
        if (stages.isEmpty()) {
            throw new GameException("No stage files found under " + String.format(STAGE_PATH, 1));
        }
        logger.info("Loaded {} stages", stages.size());
        return stages;
    }

    // Checks the layout now, so a broken stage file is reported at start-up
    // rather than halfway through a game.
    public static String[] parse(String name, List<String> lines) {
        String[] rows = lines.stream()
                .map(String::stripTrailing)
                .filter(line -> !line.startsWith("#"))
                .toArray(String[]::new);
        List<Brick> bricks;
        try {
            bricks = BrickLayout.fromRows(rows);
        } catch (GameException e) {
            throw new GameException(name + ": " + e.getMessage(), e);
        }
        if (bricks.stream().noneMatch(Brick::isBreakable)) {
            throw new GameException(name + " has no breakable bricks");
        }
        return rows;
    }
}
