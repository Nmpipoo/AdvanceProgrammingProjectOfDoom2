package se233.project2.controller;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

// Key and mouse events arrive on the JavaFX thread but are read by the game loop thread,
// so everything here is concurrent.
public class InputHandler {

    private final Set<KeyCode> held = ConcurrentHashMap.newKeySet();
    private final Set<KeyCode> pressed = ConcurrentHashMap.newKeySet();
    // Where the mouse moved to and where it was clicked since the last update, or null.
    private final AtomicReference<Point2D> moved = new AtomicReference<>();
    private final AtomicReference<Point2D> clicked = new AtomicReference<>();

    public void attach(Scene scene) {
        scene.setOnKeyPressed(e -> {
            // held.add is false on key auto-repeat, so a long press counts as one press.
            if (held.add(e.getCode())) {
                pressed.add(e.getCode());
            }
        });
        scene.setOnKeyReleased(e -> held.remove(e.getCode()));
        scene.setOnMouseMoved(e -> moveMouse(e.getX(), e.getY()));
        scene.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                click(e.getX(), e.getY());
            }
        });
        scene.getWindow().focusedProperty().addListener((obs, was, focused) -> {
            if (!focused) {
                held.clear();
            }
        });
    }

    // True while the key is down; for continuous actions like moving the paddle.
    public boolean isHeld(KeyCode key) {
        return held.contains(key);
    }

    // True once per press; for one-shot actions like launching or pausing.
    public boolean consumePress(KeyCode key) {
        return pressed.remove(key);
    }

    // Where the mouse moved to since the last update, or null if it hasn't moved.
    // Only a real move counts, so a mouse resting on a card doesn't fight the arrow keys.
    public Point2D consumeMouseMove() {
        return moved.getAndSet(null);
    }

    // Where the left button was clicked since the last update, or null.
    public Point2D consumeClick() {
        return clicked.getAndSet(null);
    }

    // Called after each update so a press or click nobody used (e.g. P on the menu)
    // doesn't fire later on another screen.
    public void clearPresses() {
        pressed.clear();
        moved.set(null);
        clicked.set(null);
    }

    // Key presses, releases and mouse events for tests, without a Scene.
    public void press(KeyCode key) {
        if (held.add(key)) {
            pressed.add(key);
        }
    }

    public void release(KeyCode key) {
        held.remove(key);
    }

    public void moveMouse(double x, double y) {
        moved.set(new Point2D(x, y));
    }

    public void click(double x, double y) {
        moveMouse(x, y);
        clicked.set(new Point2D(x, y));
    }
}
