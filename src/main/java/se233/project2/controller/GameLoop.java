package se233.project2.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se233.project2.exception.ExceptionHandler;
import se233.project2.exception.GameException;
import se233.project2.model.GameWorld;

// Runs game logic on its own thread at a fixed 60 updates per second,
// independent of how fast the screen is drawn.
public class GameLoop implements Runnable {

    public static final double STEP = 1.0 / 60.0;
    private static final double MAX_LAG = 0.25;

    private static final Logger logger = LogManager.getLogger(GameLoop.class);

    private final GameWorld world;
    private final InputHandler input;
    private volatile boolean running;
    private volatile int updatesPerSecond;
    private Thread thread;

    public GameLoop(GameWorld world, InputHandler input) {
        this.world = world;
        this.input = input;
    }

    public void start() {
        running = true;
        thread = new Thread(this, "game-loop");
        thread.setDaemon(true);
        thread.start();
        logger.info("Game loop started");
    }

    public void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        long previous = System.nanoTime();
        long secondStart = previous;
        double lag = 0;
        int updates = 0;

        while (running) {
            long now = System.nanoTime();
            // Cap the backlog so a long stall doesn't fast-forward the game.
            lag = Math.min(lag + (now - previous) / 1e9, MAX_LAG);
            previous = now;

            try {
                while (lag >= STEP) {
                    synchronized (world) {
                        world.update(STEP, input);
                    }
                    lag -= STEP;
                    updates++;
                }
            } catch (GameException e) {
                ExceptionHandler.handle(e);
                running = false;
                break;
            }

            if (now - secondStart >= 1_000_000_000L) {
                updatesPerSecond = updates;
                updates = 0;
                secondStart = now;
            }

            try {
                Thread.sleep(Math.max(1, (long) ((STEP - lag) * 1000)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        logger.info("Game loop stopped");
    }

    public int getUpdatesPerSecond() {
        return updatesPerSecond;
    }
}
