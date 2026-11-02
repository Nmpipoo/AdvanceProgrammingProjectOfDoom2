package se233.project2.view;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import se233.project2.exception.GameException;
import se233.project2.model.GameWorld;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

// Loads the sprite sheets in resources/se233/project2/sprites and builds the animations from them.
public class Sprites {

    // A sequence of frames shown one after another, each for frameSeconds. A looping animation
    // repeats; a one-shot one stays on its last frame. Frames are sprite sheet rectangles,
    // or anything else that changes over time (e.g. the ball's squash scales).
    public static class Animation<T> {
        private final List<T> frames;
        private final double frameSeconds;
        private final boolean loop;

        public Animation(List<T> frames, double frameSeconds, boolean loop) {
            if (frames.isEmpty()) {
                throw new IllegalArgumentException("An animation needs at least one frame");
            }
            this.frames = List.copyOf(frames);
            this.frameSeconds = frameSeconds;
            this.loop = loop;
        }

        public T frameAt(double time) {
            int index = (int) Math.floor(Math.max(0, time) / frameSeconds);
            return frames.get(loop ? index % frames.size() : Math.min(index, frames.size() - 1));
        }
    }

    private static final String DIR = "/se233/project2/sprites/";

    // Background sheet: 224 px wide fields, 8 px apart. Skip the metal border and
    // the shadow it casts on the top and left of the pattern.
    private static final int FIELD_STRIDE = 232;
    private static final int FIELD_COUNT = 5;
    private static final int FIELD_INSET = 16;
    private static final int FIELD_PATTERN_WIDTH = 200;

    private final Image paddleSheet;
    private final Image enemySheet;
    private final Image backgroundSheet;

    // Paddle sheet: 32x8 paddle frames stacked in columns, 6 frames each.
    private final Animation<Rectangle2D> paddleMove = paddleColumn(32);
    private final Animation<Rectangle2D> explosion;
    // Ball squash on bounce, as {width scale, height scale} along the bounce direction.
    private final Animation<double[]> ballBounce = new Animation<>(List.of(
            new double[]{1.4, 0.65},
            new double[]{1.2, 0.8},
            new double[]{0.9, 1.1},
            new double[]{1.0, 1.0}), 0.04, false);

    private final Rectangle2D ball = new Rectangle2D(0, 40, 5, 4);

    // Throws GameException if a sheet is missing or unreadable.
    public Sprites() {
        // Paddle sheet background is pure green in several shades.
        paddleSheet = load("paddle.png", rgb -> (rgb & 0xFF00FF) == 0 && rgb != 0);
        enemySheet = load("enemies.png", rgb -> rgb == 0xABA000 || rgb == 0x5B5500);
        backgroundSheet = load("backgrounds.png", rgb -> false);

        List<Rectangle2D> blast = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            blast.add(new Rectangle2D(i * 16, 128, 16, 16));
        }
        explosion = new Animation<>(blast, GameWorld.EXPLOSION_SECONDS / blast.size(), false);
    }

    // The sheets have solid background colours instead of transparency,
    // so pixels matching `background` are made transparent on load.
    private static Image load(String name, IntPredicate background) {
        Image source;
        try (InputStream in = Sprites.class.getResourceAsStream(DIR + name)) {
            if (in == null) {
                throw new GameException("Missing sprite sheet " + DIR + name);
            }
            source = new Image(in);
        } catch (IOException e) {
            throw new GameException("Could not read sprite sheet " + DIR + name, e);
        }
        if (source.isError()) {
            throw new GameException("Could not decode sprite sheet " + DIR + name, source.getException());
        }
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage keyed = new WritableImage(width, height);
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = keyed.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                writer.setArgb(x, y, background.test(argb & 0xFFFFFF) ? 0 : argb);
            }
        }
        return keyed;
    }

    private static Animation<Rectangle2D> paddleColumn(int x) {
        List<Rectangle2D> frames = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            frames.add(new Rectangle2D(x, i * 8, 32, 8));
        }
        return new Animation<>(frames, 0.07, true);
    }

    // The inside of the playfield for a stage, cycling through the five field designs.
    public Rectangle2D background(int stage, double aspectWidth, double aspectHeight) {
        int field = Math.floorMod(stage - 1, FIELD_COUNT);
        double height = FIELD_PATTERN_WIDTH * aspectHeight / aspectWidth;
        return new Rectangle2D(field * FIELD_STRIDE + FIELD_INSET, FIELD_INSET, FIELD_PATTERN_WIDTH, height);
    }

    public Image getPaddleSheet() {
        return paddleSheet;
    }

    public Image getEnemySheet() {
        return enemySheet;
    }

    public Image getBackgroundSheet() {
        return backgroundSheet;
    }

    public Animation<Rectangle2D> getPaddleMove() {
        return paddleMove;
    }

    public Animation<Rectangle2D> getExplosion() {
        return explosion;
    }

    public Animation<double[]> getBallBounce() {
        return ballBounce;
    }

    public Rectangle2D getBall() {
        return ball;
    }
}
