package se233.project2.controller;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Key events arrive on the JavaFX thread but are read by the game loop thread,
// so both sets are concurrent.
public class InputHandler {

    private final Set<KeyCode> held = ConcurrentHashMap.newKeySet();
    private final Set<KeyCode> pressed = ConcurrentHashMap.newKeySet();

    public void attach(Scene scene) {
        scene.setOnKeyPressed(e -> {
            // held.add is false on key auto-repeat, so a long press counts as one press.
            if (held.add(e.getCode())) {
                pressed.add(e.getCode());
            }
        });
        scene.setOnKeyReleased(e -> held.remove(e.getCode()));
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

    // Called after each update so a press nobody used (e.g. P on the menu)
    // doesn't fire later on another screen.
    public void clearPresses() {
        pressed.clear();
    }

    // Key presses and releases for tests, without a Scene.
    public void press(KeyCode key) {
        if (held.add(key)) {
            pressed.add(key);
        }
    }

    public void release(KeyCode key) {
        held.remove(key);
    }
}
