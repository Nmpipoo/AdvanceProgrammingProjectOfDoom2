package se233.project2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Short-lived visual feedback: explosions where bricks break and floating "+50" score text.
// Kept in the model so the game loop times them; GameView only draws them.
public class Effects {

    public static final double EXPLOSION_SECONDS = 0.36;
    public static final double TEXT_SECONDS = 0.8;
    public static final double TEXT_RISE = 40;

    public record Explosion(double centerX, double centerY, double age) {
    }

    public record FloatingText(String text, double centerX, double centerY, double age) {
    }

    private final List<Explosion> explosions = new ArrayList<>();
    private final List<FloatingText> texts = new ArrayList<>();

    public void explode(double centerX, double centerY) {
        explosions.add(new Explosion(centerX, centerY, 0));
    }

    public void floatText(String text, double centerX, double centerY) {
        texts.add(new FloatingText(text, centerX, centerY, 0));
    }

    public void update(double dt) {
        explosions.replaceAll(e -> new Explosion(e.centerX(), e.centerY(), e.age() + dt));
        explosions.removeIf(e -> e.age() >= EXPLOSION_SECONDS);
        texts.replaceAll(t -> new FloatingText(t.text(), t.centerX(), t.centerY() - TEXT_RISE / TEXT_SECONDS * dt, t.age() + dt));
        texts.removeIf(t -> t.age() >= TEXT_SECONDS);
    }

    public void clear() {
        explosions.clear();
        texts.clear();
    }

    public List<Explosion> getExplosions() {
        return Collections.unmodifiableList(explosions);
    }

    public List<FloatingText> getTexts() {
        return Collections.unmodifiableList(texts);
    }
}
