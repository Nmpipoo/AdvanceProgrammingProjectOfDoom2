package se233.project2.view;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import se233.project2.model.Effects;

import java.util.ArrayList;
import java.util.List;

// Where each sprite lives on the sheets in resources/se233/project2/sprites,
// and the animations built from them.
public class Sprites {

    private static final String DIR = "/se233/project2/sprites/";

    // Paddle sheet: 32x8 paddle frames stacked in columns, 6 frames each.
    private static final int PADDLE_FRAMES = 6;
    private static final int NORMAL_PADDLE_X = 32;
    private static final int LASER_PADDLE_X = 144;

    // Background sheet: 224 px wide fields, 8 px apart. Skip the metal border and
    // the shadow it casts on the top and left of the pattern.
    private static final int FIELD_STRIDE = 232;
    private static final int FIELD_COUNT = 5;
    private static final int FIELD_INSET = 16;
    private static final int FIELD_PATTERN_WIDTH = 200;

    private final Image paddleSheet;
    private final Image enemySheet;
    private final Image backgroundSheet;

    private final Animation<Rectangle2D> paddleMove;
    private final Animation<Rectangle2D> laserPaddleMove;
    private final Animation<Rectangle2D> explosion;
    // Ball squash on bounce, as {width scale, height scale} along the bounce direction.
    private final Animation<double[]> ballBounce;

    private final Rectangle2D ball = new Rectangle2D(0, 40, 5, 4);
    private final Rectangle2D laserBolt = new Rectangle2D(165, 48, 3, 8);

    public Sprites() {
        // Paddle sheet background is pure green in several shades.
        paddleSheet = SpriteSheet.load(DIR + "paddle.png", rgb -> (rgb & 0xFF00FF) == 0 && rgb != 0).getImage();
        enemySheet = SpriteSheet.load(DIR + "enemies.png", rgb -> rgb == 0xABA000 || rgb == 0x5B5500).getImage();
        backgroundSheet = SpriteSheet.load(DIR + "backgrounds.png", rgb -> false).getImage();

        paddleMove = column(NORMAL_PADDLE_X, 0.07);
        laserPaddleMove = column(LASER_PADDLE_X, 0.07);

        List<Rectangle2D> blast = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            blast.add(new Rectangle2D(i * 16, 128, 16, 16));
        }
        explosion = new Animation<>(blast, Effects.EXPLOSION_SECONDS / blast.size(), false);

        ballBounce = new Animation<>(List.of(
                new double[]{1.4, 0.65},
                new double[]{1.2, 0.8},
                new double[]{0.9, 1.1},
                new double[]{1.0, 1.0}), 0.04, false);
    }

    private static Animation<Rectangle2D> column(int x, double frameSeconds) {
        List<Rectangle2D> frames = new ArrayList<>();
        for (int i = 0; i < PADDLE_FRAMES; i++) {
            frames.add(new Rectangle2D(x, i * 8, 32, 8));
        }
        return new Animation<>(frames, frameSeconds, true);
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

    public Animation<Rectangle2D> getLaserPaddleMove() {
        return laserPaddleMove;
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

    public Rectangle2D getLaserBolt() {
        return laserBolt;
    }
}
