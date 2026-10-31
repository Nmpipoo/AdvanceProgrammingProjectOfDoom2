package se233.project2.view;

import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import se233.project2.model.Ball;
import se233.project2.model.Effects;
import se233.project2.model.GameState;
import se233.project2.model.GameWorld;
import se233.project2.model.Laser;
import se233.project2.model.LaserBolt;
import se233.project2.model.Paddle;
import se233.project2.model.PowerUp;
import se233.project2.model.brick.Brick;
import se233.project2.model.brick.BrickType;
import se233.project2.model.upgrade.Upgrade;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static se233.project2.model.GameWorld.HEIGHT;
import static se233.project2.model.GameWorld.HUD_HEIGHT;
import static se233.project2.model.GameWorld.WIDTH;

// Draws everything onto a single Canvas.
public class GameView extends Pane {

    private static final Color HUD_COLOR = Color.web("#11131c");
    private static final Color TEXT_COLOR = Color.web("#e8ecf4");
    private static final Color ACCENT = Color.web("#ffb347");
    private static final Color MUTED = Color.web("#8a93a8");
    private static final Color BACKGROUND = Color.web("#161a2b");
    private static final Color BACKGROUND_DIM = Color.color(0.05, 0.06, 0.1, 0.6);
    private static final Color PIERCE_GLOW = Color.web("#ffb347", 0.55);
    private static final Color LASER_CAPSULE = Color.web("#ff5a4f");
    // Sprites are pixel art, drawn at 3x their size on the sheet.
    private static final double SCALE = 3;

    // Upgrade choice cards.
    private static final double CARD_WIDTH = 220;
    private static final double CARD_HEIGHT = 150;
    private static final double CARD_GAP = 30;
    private static final double CARD_TOP = 240;

    private final Canvas canvas = new Canvas(WIDTH, HEIGHT);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final Sprites sprites;

    // Throws GameException if a sprite sheet is missing or unreadable.
    public GameView() {
        sprites = new Sprites();
        getChildren().add(canvas);
        setPrefSize(WIDTH, HEIGHT);
        // Keep pixel art crisp when scaled up.
        gc.setImageSmoothing(false);
    }

    public void render(GameWorld world, int fps, int ups) {
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        switch (world.getState()) {
            case MENU -> drawMenu();
            case PLAYING -> {
                drawPlayfield(world);
                drawBanner(world);
                drawHud(world);
            }
            case PAUSED -> {
                drawPlayfield(world);
                drawHud(world);
                drawOverlay("PAUSED", "P to resume    Q to quit to menu");
            }
            case CHOOSE_UPGRADE -> {
                drawHud(world);
                drawUpgradeChoice(world);
            }
            case GAME_OVER -> {
                drawPlayfield(world);
                drawHud(world);
                drawOverlay("GAME OVER", "Score " + world.getScore() + "    ENTER to retry    ESC for menu");
            }
            case VICTORY -> {
                drawHud(world);
                drawOverlay("YOU WIN!", "Score " + world.getScore() + "    ENTER to play again    ESC for menu");
            }
        }
        if (world.isDebugVisible()) {
            drawDebug(fps, ups);
        }
    }

    private void drawMenu() {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(ACCENT);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 64));
        gc.fillText("GAME", WIDTH / 2, HEIGHT / 2 - 80);
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Consolas", 22));
        gc.fillText("Press ENTER to start", WIDTH / 2, HEIGHT / 2 + 10);
        gc.setFill(MUTED);
        gc.setFont(Font.font("Consolas", 16));
        gc.fillText("A/D or arrow keys: move   SPACE: launch   P: pause", WIDTH / 2, HEIGHT / 2 + 60);
        gc.fillText("Catch the red L capsule, then hold SPACE to fire lasers", WIDTH / 2, HEIGHT / 2 + 85);
    }

    private void drawPlayfield(GameWorld world) {
        // Stage background from the sprite sheet, dimmed so the bricks stand out.
        Rectangle2D field = sprites.background(world.getStage(), WIDTH, HEIGHT - HUD_HEIGHT);
        gc.drawImage(sprites.getBackgroundSheet(), field.getMinX(), field.getMinY(), field.getWidth(), field.getHeight(),
                0, HUD_HEIGHT, WIDTH, HEIGHT - HUD_HEIGHT);
        gc.setFill(BACKGROUND_DIM);
        gc.fillRect(0, HUD_HEIGHT, WIDTH, HEIGHT - HUD_HEIGHT);

        for (Brick brick : world.getBricks()) {
            if (!brick.isDestroyed()) {
                drawBrick(brick);
            }
        }

        for (PowerUp powerUp : world.getPowerUps()) {
            drawPowerUp(powerUp);
        }

        Image paddleSheet = sprites.getPaddleSheet();
        Rectangle2D bolt = sprites.getLaserBolt();
        for (LaserBolt laserBolt : world.getLaser().getBolts()) {
            drawCentered(paddleSheet, bolt, laserBolt.getCenterX(), laserBolt.getCenterY(), SCALE, SCALE);
        }

        drawPaddle(world);

        for (Ball ball : world.getBalls()) {
            drawBall(ball);
        }

        drawEffects(world.getEffects());

        if (!world.getBall().isLaunched() && world.getState() == GameState.PLAYING) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFill(MUTED);
            gc.setFont(Font.font("Consolas", 16));
            gc.fillText("SPACE to launch", WIDTH / 2, Paddle.Y - 60);
        }
    }

    private void drawBrick(Brick brick) {
        double x = brick.getX();
        double y = brick.getY();
        double w = brick.getWidth();
        double h = brick.getHeight();
        if (brick.isFlashing()) {
            gc.setFill(Color.WHITE);
            gc.fillRect(x, y, w, h);
            return;
        }
        // Damaged bricks get darker with each hit they take.
        double health = (double) brick.getHitPoints() / brick.getMaxHitPoints();
        Color color = brickColor(brick.getType());
        if (brick.isBreakable()) {
            color = color.deriveColor(0, 1, 0.45 + 0.55 * health, 1);
        }
        gc.setFill(color);
        gc.fillRect(x, y, w, h);
        // Light top edge and dark bottom edge give a simple raised look.
        gc.setFill(color.brighter());
        gc.fillRect(x, y, w, 3);
        gc.setFill(color.darker());
        gc.fillRect(x, y + h - 3, w, 3);
    }

    private void drawPowerUp(PowerUp powerUp) {
        gc.setFill(LASER_CAPSULE);
        gc.fillRoundRect(powerUp.getX(), powerUp.getY(), powerUp.getWidth(), powerUp.getHeight(), 14, 14);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        gc.fillText("L", powerUp.getCenterX(), powerUp.getCenterY());
    }

    // The paddle sprite is 32x8: its 8 px end caps keep their shape and the middle stretches,
    // so any paddle width (after Wide Paddle upgrades) still looks right.
    private void drawPaddle(GameWorld world) {
        Paddle paddle = world.getPaddle();
        Animation<Rectangle2D> move = world.getLaser().isActive() ? sprites.getLaserPaddleMove() : sprites.getPaddleMove();
        Rectangle2D frame = move.frameAt(paddle.getMoveTime());
        Image sheet = sprites.getPaddleSheet();
        double cap = 8 * SCALE;
        double height = frame.getHeight() * SCALE;
        double x = paddle.getX();
        double y = paddle.getY() - 2;
        double width = paddle.getWidth();
        gc.drawImage(sheet, frame.getMinX(), frame.getMinY(), 8, 8, x, y, cap, height);
        gc.drawImage(sheet, frame.getMinX() + 8, frame.getMinY(), 16, 8, x + cap, y, width - 2 * cap, height);
        gc.drawImage(sheet, frame.getMinX() + 24, frame.getMinY(), 8, 8, x + width - cap, y, cap, height);
    }

    // The ball squashes for a moment after each bounce, flattened along the direction it hit.
    private void drawBall(Ball ball) {
        double[] squash = sprites.getBallBounce().frameAt(ball.getSinceBounce());
        double scaleX = ball.isBouncedVertically() ? squash[0] : squash[1];
        double scaleY = ball.isBouncedVertically() ? squash[1] : squash[0];
        if (ball.getPierceLeft() > 0) {
            // A ball that can still smash through bricks glows orange.
            gc.setFill(PIERCE_GLOW);
            gc.fillOval(ball.getX() - 4, ball.getY() - 4, ball.getWidth() + 8, ball.getHeight() + 8);
        }
        drawCentered(sprites.getPaddleSheet(), sprites.getBall(), ball.getCenterX(), ball.getCenterY(),
                SCALE * scaleX, SCALE * scaleY);
    }

    private void drawEffects(Effects effects) {
        Image enemySheet = sprites.getEnemySheet();
        for (Effects.Explosion explosion : effects.getExplosions()) {
            drawCentered(enemySheet, sprites.getExplosion().frameAt(explosion.age()),
                    explosion.centerX(), explosion.centerY(), SCALE, SCALE);
        }
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
        // White with a dark outline so it reads on top of the explosion.
        gc.setLineWidth(3);
        for (Effects.FloatingText text : effects.getTexts()) {
            double alpha = 1 - text.age() / Effects.TEXT_SECONDS;
            double y = text.centerY() - 22;
            gc.setStroke(Color.color(0, 0, 0, 0.8 * alpha));
            gc.strokeText(text.text(), text.centerX(), y);
            gc.setFill(TEXT_COLOR.deriveColor(0, 1, 1, alpha));
            gc.fillText(text.text(), text.centerX(), y);
        }
    }

    private void drawCentered(Image sheet, Rectangle2D frame, double centerX, double centerY, double scaleX, double scaleY) {
        double w = frame.getWidth() * scaleX;
        double h = frame.getHeight() * scaleY;
        gc.drawImage(sheet, frame.getMinX(), frame.getMinY(), frame.getWidth(), frame.getHeight(),
                centerX - w / 2, centerY - h / 2, w, h);
    }

    private static Color brickColor(BrickType type) {
        return switch (type) {
            case NORMAL -> Color.web("#4fc3f7");
            case STRONG -> Color.web("#9575cd");
            case BONUS -> Color.web("#ffd54f");
            case UNBREAKABLE -> Color.web("#5c6478");
        };
    }

    private void drawHud(GameWorld world) {
        gc.setFill(HUD_COLOR);
        gc.fillRect(0, 0, WIDTH, HUD_HEIGHT);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        gc.setFill(TEXT_COLOR);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("SCORE " + world.getScore(), 16, HUD_HEIGHT / 2);
        if (world.getCombo() >= 2) {
            gc.setFill(ACCENT);
            gc.fillText("COMBO x" + world.getCombo(), 200, HUD_HEIGHT / 2);
            gc.setFill(TEXT_COLOR);
        }
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("STAGE " + world.getStage() + "/" + world.getStageCount(), WIDTH / 2, HUD_HEIGHT / 2);
        Laser laser = world.getLaser();
        if (laser.isActive()) {
            gc.setFill(LASER_CAPSULE);
            gc.fillText("LASER " + (int) Math.ceil(laser.getTimeLeft()) + "s", 590, HUD_HEIGHT / 2);
        }
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(ACCENT);
        gc.fillText("LIVES " + world.getLives(), WIDTH - 16, HUD_HEIGHT / 2);
    }

    // Short message in the middle of the screen ("STAGE 2", "LIFE LOST") that fades out.
    private void drawBanner(GameWorld world) {
        if (world.getBannerTime() <= 0) {
            return;
        }
        double alpha = Math.min(1, world.getBannerTime() / 0.5);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(TEXT_COLOR.deriveColor(0, 1, 1, alpha));
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 40));
        gc.fillText(world.getBanner(), WIDTH / 2, HEIGHT / 2 + 40);
        if (!world.getBannerSubtitle().isEmpty()) {
            gc.setFill(ACCENT.deriveColor(0, 1, 1, alpha));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 22));
            gc.fillText(world.getBannerSubtitle(), WIDTH / 2, HEIGHT / 2 + 80);
        }
    }

    private void drawUpgradeChoice(GameWorld world) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 44));
        gc.fillText("STAGE " + world.getStage() + " CLEAR", WIDTH / 2, 130);
        gc.setFill(MUTED);
        gc.setFont(Font.font("Consolas", 20));
        gc.fillText("Choose an upgrade", WIDTH / 2, 185);

        List<Upgrade> choices = world.getUpgradeChoices();
        double left = (WIDTH - choices.size() * CARD_WIDTH - (choices.size() - 1) * CARD_GAP) / 2;
        for (int i = 0; i < choices.size(); i++) {
            double x = left + i * (CARD_WIDTH + CARD_GAP);
            boolean selected = i == world.getSelectedChoice();
            gc.setFill(HUD_COLOR);
            gc.fillRect(x, CARD_TOP, CARD_WIDTH, CARD_HEIGHT);
            gc.setStroke(selected ? ACCENT : MUTED);
            gc.setLineWidth(selected ? 3 : 1);
            gc.strokeRect(x, CARD_TOP, CARD_WIDTH, CARD_HEIGHT);

            double centerX = x + CARD_WIDTH / 2;
            gc.setFill(selected ? ACCENT : MUTED);
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 16));
            gc.fillText("[" + (i + 1) + "]", centerX, CARD_TOP + 24);
            gc.setFill(TEXT_COLOR);
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
            gc.fillText(choices.get(i).getName(), centerX, CARD_TOP + 58);
            gc.setFill(MUTED);
            gc.setFont(Font.font("Consolas", 15));
            List<String> lines = wrap(choices.get(i).getDescription(), 22);
            for (int line = 0; line < lines.size(); line++) {
                gc.fillText(lines.get(line), centerX, CARD_TOP + 95 + line * 20);
            }
        }

        gc.setFill(MUTED);
        gc.setFont(Font.font("Consolas", 16));
        gc.fillText("Press 1-" + choices.size() + ", or LEFT/RIGHT then ENTER", WIDTH / 2, CARD_TOP + CARD_HEIGHT + 45);
        List<String> taken = world.getStats().getUpgradesTaken();
        if (!taken.isEmpty()) {
            gc.fillText("Your upgrades: " + summarize(taken), WIDTH / 2, CARD_TOP + CARD_HEIGHT + 85);
        }
    }

    // "Wide Paddle x2, Score Boost" from the list of picks.
    private static String summarize(List<String> upgrades) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String name : upgrades) {
            counts.merge(name, 1L, Long::sum);
        }
        StringBuilder text = new StringBuilder();
        counts.forEach((name, count) -> {
            if (!text.isEmpty()) {
                text.append(", ");
            }
            text.append(name).append(count > 1 ? " x" + count : "");
        });
        return text.toString();
    }

    private static List<String> wrap(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (!line.isEmpty() && line.length() + 1 + word.length() > maxChars) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (!line.isEmpty()) {
                line.append(' ');
            }
            line.append(word);
        }
        lines.add(line.toString());
        return lines;
    }

    private void drawOverlay(String title, String hint) {
        gc.setFill(Color.color(0, 0, 0, 0.55));
        gc.fillRect(0, HUD_HEIGHT, WIDTH, HEIGHT - HUD_HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 48));
        gc.fillText(title, WIDTH / 2, HEIGHT / 2 - 20);
        gc.setFill(MUTED);
        gc.setFont(Font.font("Consolas", 18));
        gc.fillText(hint, WIDTH / 2, HEIGHT / 2 + 30);
    }

    private void drawDebug(int fps, int ups) {
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.BOTTOM);
        gc.setFill(MUTED);
        gc.setFont(Font.font("Consolas", 12));
        gc.fillText("N: skip stage    " + fps + " FPS / " + ups + " UPS", WIDTH - 8, HEIGHT - 6);
    }
}
