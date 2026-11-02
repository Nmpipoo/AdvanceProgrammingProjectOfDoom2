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
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import se233.project2.model.Ball;
import se233.project2.model.GameWorld;
import se233.project2.model.Laser;
import se233.project2.model.Paddle;
import se233.project2.model.Brick;
import se233.project2.model.Upgrade;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static se233.project2.model.GameWorld.HEIGHT;
import static se233.project2.model.GameWorld.HUD_HEIGHT;
import static se233.project2.model.GameWorld.WIDTH;

// Draws everything onto a single Canvas.
public class GameView extends Pane {

    // Menus, HUD and messages are plain: black text and 1 px frames on white, in the default font.
    private static final Color PAPER = Color.WHITE;
    private static final Color INK = Color.BLACK;
    private static final String FONT = Font.getDefault().getFamily();
    private static final double BOX_PADDING = 24;
    private static final Color BACKGROUND_DIM = Color.color(0.05, 0.06, 0.1, 0.6);
    private static final Color PIERCE_GLOW = Color.web("#ffb347", 0.55);
    private static final Color LASER_CAPSULE = Color.web("#ff5a4f");
    // Sprites are pixel art, drawn at 3x their size on the sheet.
    private static final double SCALE = 3;

    private final Canvas canvas = new Canvas(WIDTH, HEIGHT);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final Sprites sprites;
    // Used only to measure text so message boxes fit around it.
    private final Text measure = new Text();

    // Throws GameException if a sprite sheet is missing or unreadable.
    public GameView() {
        sprites = new Sprites();
        getChildren().add(canvas);
        setPrefSize(WIDTH, HEIGHT);
        // Keep pixel art crisp when scaled up.
        gc.setImageSmoothing(false);
    }

    public void render(GameWorld world, int fps, int ups) {
        gc.setFill(PAPER);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        switch (world.getState()) {
            case MENU -> drawMenu();
            case PLAYING -> {
                drawPlayfield(world);
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
        gc.setFill(INK);
        gc.setFont(font(64));
        gc.fillText("Arkanoid", WIDTH / 2, HEIGHT / 2 - 80);
        gc.setFont(font(22));
        gc.fillText("Press ENTER to start", WIDTH / 2, HEIGHT / 2 + 10);
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

        for (Laser.Capsule capsule : world.getCapsules()) {
            drawCapsule(capsule);
        }

        Image paddleSheet = sprites.getPaddleSheet();
        Rectangle2D bolt = sprites.getLaserBolt();
        for (Laser.Bolt laserBolt : world.getLaser().getBolts()) {
            drawCentered(paddleSheet, bolt, laserBolt.getCenterX(), laserBolt.getCenterY(), SCALE, SCALE);
        }

        drawPaddle(world);

        for (Ball ball : world.getBalls()) {
            drawBall(ball);
        }

        drawPopups(world.getPopups());
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

    private void drawCapsule(Laser.Capsule capsule) {
        gc.setFill(LASER_CAPSULE);
        gc.fillRoundRect(capsule.getX(), capsule.getY(), capsule.getWidth(), capsule.getHeight(), 14, 14);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font(FONT, FontWeight.BOLD, 14));
        gc.fillText("L", capsule.getCenterX(), capsule.getCenterY());
    }

    // The paddle sprite is 32x8: its 8 px end caps keep their shape and the middle stretches,
    // so any paddle width (after Wide Paddle upgrades) still looks right.
    private void drawPaddle(GameWorld world) {
        Paddle paddle = world.getPaddle();
        Sprites.Animation<Rectangle2D> move = world.getLaser().isActive() ? sprites.getLaserPaddleMove() : sprites.getPaddleMove();
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

    // Explosions where bricks broke, and "+points" / "LASER!" text that rises and fades.
    private void drawPopups(List<GameWorld.Popup> popups) {
        for (GameWorld.Popup popup : popups) {
            if (popup.explosion() && popup.age() < GameWorld.EXPLOSION_SECONDS) {
                drawCentered(sprites.getEnemySheet(), sprites.getExplosion().frameAt(popup.age()),
                        popup.x(), popup.y(), SCALE, SCALE);
            }
        }
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font(FONT, FontWeight.BOLD, 16));
        // White with a dark outline so it reads on top of the explosion.
        gc.setLineWidth(3);
        for (GameWorld.Popup popup : popups) {
            double progress = popup.age() / GameWorld.POPUP_SECONDS;
            double y = popup.y() - 22 - 40 * progress;
            gc.setStroke(Color.color(0, 0, 0, 0.8 * (1 - progress)));
            gc.strokeText(popup.text(), popup.x(), y);
            gc.setFill(Color.color(1, 1, 1, 1 - progress));
            gc.fillText(popup.text(), popup.x(), y);
        }
    }

    private void drawCentered(Image sheet, Rectangle2D frame, double centerX, double centerY, double scaleX, double scaleY) {
        double w = frame.getWidth() * scaleX;
        double h = frame.getHeight() * scaleY;
        gc.drawImage(sheet, frame.getMinX(), frame.getMinY(), frame.getWidth(), frame.getHeight(),
                centerX - w / 2, centerY - h / 2, w, h);
    }

    private static Color brickColor(Brick.Type type) {
        return switch (type) {
            case NORMAL -> Color.web("#4fc3f7");
            case STRONG -> Color.web("#9575cd");
            case BONUS -> Color.web("#ffd54f");
            case UNBREAKABLE -> Color.web("#5c6478");
        };
    }

    private void drawHud(GameWorld world) {
        gc.setFill(PAPER);
        gc.fillRect(0, 0, WIDTH, HUD_HEIGHT);
        gc.setStroke(INK);
        gc.setLineWidth(1);
        gc.strokeLine(0, HUD_HEIGHT - 0.5, WIDTH, HUD_HEIGHT - 0.5);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(font(18));
        gc.setFill(INK);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("SCORE " + world.getScore(), 16, HUD_HEIGHT / 2);
        if (world.getCombo() >= 2) {
            gc.fillText("COMBO x" + world.getCombo(), 200, HUD_HEIGHT / 2);
        }
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("STAGE " + world.getStage() + "/" + world.getStageCount(), WIDTH / 2, HUD_HEIGHT / 2);
        Laser laser = world.getLaser();
        if (laser.isActive()) {
            gc.fillText("LASER " + (int) Math.ceil(laser.getTimeLeft()) + "s", 590, HUD_HEIGHT / 2);
        }
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("LIVES " + world.getLives(), WIDTH - 16, HUD_HEIGHT / 2);
    }

    // Lines of text in a white box with a 1 px frame, centered on (centerX, centerY),
    // so messages stay readable on top of the playfield.
    private void drawMessage(double centerX, double centerY, String[] lines, double[] sizes) {
        double width = 0;
        double height = 0;
        for (int i = 0; i < lines.length; i++) {
            measure.setFont(font(sizes[i]));
            measure.setText(lines[i]);
            width = Math.max(width, measure.getLayoutBounds().getWidth());
            height += sizes[i] * 1.5;
        }
        width += 2 * BOX_PADDING;
        height += BOX_PADDING;
        double x = Math.round(centerX - width / 2) + 0.5;
        double y = Math.round(centerY - height / 2) + 0.5;
        gc.setFill(PAPER);
        gc.fillRect(x, y, width, height);
        gc.setStroke(INK);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, width, height);

        gc.setFill(INK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        double lineY = y + BOX_PADDING / 2;
        for (int i = 0; i < lines.length; i++) {
            gc.setFont(font(sizes[i]));
            gc.fillText(lines[i], centerX, lineY + sizes[i] * 0.75);
            lineY += sizes[i] * 1.5;
        }
    }

    private static Font font(double size) {
        return Font.font(FONT, size);
    }

    private void drawUpgradeChoice(GameWorld world) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(INK);
        gc.setFont(font(44));
        gc.fillText("STAGE " + world.getStage() + " CLEAR", WIDTH / 2, 130);
        gc.setFont(font(20));
        gc.fillText("Choose an upgrade", WIDTH / 2, 185);

        List<Upgrade> choices = world.getUpgradeChoices();
        gc.setStroke(INK);
        for (int i = 0; i < choices.size(); i++) {
            double x = GameWorld.cardLeft(i, choices.size());
            // No colour, so the card under the mouse gets a thicker frame.
            boolean selected = i == world.getSelectedChoice();
            gc.setLineWidth(selected ? 3 : 1);
            gc.strokeRect(x + 0.5, GameWorld.CARD_TOP + 0.5, GameWorld.CARD_WIDTH, GameWorld.CARD_HEIGHT);

            double centerX = x + GameWorld.CARD_WIDTH / 2;
            gc.setFont(font(20));
            gc.fillText(choices.get(i).getName(), centerX, GameWorld.CARD_TOP + 50);
            gc.setFont(font(15));
            List<String> lines = wrap(choices.get(i).getDescription(), 22);
            for (int line = 0; line < lines.size(); line++) {
                gc.fillText(lines.get(line), centerX, GameWorld.CARD_TOP + 90 + line * 20);
            }
        }
        gc.setLineWidth(1);

        gc.setFont(font(16));
        List<String> taken = world.getStats().getUpgradesTaken();
        if (!taken.isEmpty()) {
            gc.fillText("Your upgrades: " + summarize(taken), WIDTH / 2, GameWorld.CARD_TOP + GameWorld.CARD_HEIGHT + 45);
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
        drawMessage(WIDTH / 2, HEIGHT / 2, new String[]{title, hint}, new double[]{48, 18});
    }

    private void drawDebug(int fps, int ups) {
        String text = "N: skip stage    " + fps + " FPS / " + ups + " UPS";
        measure.setFont(font(12));
        measure.setText(text);
        double width = measure.getLayoutBounds().getWidth() + 2 * BOX_PADDING;
        drawMessage(WIDTH - 8 - width / 2, HEIGHT - 24, new String[]{text}, new double[]{12});
    }
}
