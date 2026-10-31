package se233.project2.controller;

import javafx.animation.AnimationTimer;
import se233.project2.model.GameWorld;
import se233.project2.view.GameView;

// Redraws the screen on the JavaFX thread every frame from the current world state.
public class DrawingLoop extends AnimationTimer {

    private final GameWorld world;
    private final GameView view;
    private final GameLoop gameLoop;
    private long secondStart;
    private int frames;
    private int framesPerSecond;

    public DrawingLoop(GameWorld world, GameView view, GameLoop gameLoop) {
        this.world = world;
        this.view = view;
        this.gameLoop = gameLoop;
    }

    @Override
    public void handle(long now) {
        frames++;
        if (now - secondStart >= 1_000_000_000L) {
            framesPerSecond = frames;
            frames = 0;
            secondStart = now;
        }
        synchronized (world) {
            view.render(world, framesPerSecond, gameLoop.getUpdatesPerSecond());
        }
    }
}
