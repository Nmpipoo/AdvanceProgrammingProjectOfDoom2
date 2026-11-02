package se233.project2;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se233.project2.controller.GameLoop;
import se233.project2.controller.InputHandler;
import se233.project2.exception.ExceptionHandler;
import se233.project2.exception.GameException;
import se233.project2.model.GameWorld;
import se233.project2.view.GameView;

public class App extends Application {

    private static final Logger logger = LogManager.getLogger(App.class);

    private GameLoop gameLoop;
    private AnimationTimer drawingLoop;

    @Override
    public void start(Stage stage) {
        ExceptionHandler.install();
        GameWorld world;
        GameView view;
        try {
            world = new GameWorld();
            view = new GameView();
        } catch (GameException e) {
            ExceptionHandler.handleFatal(e);
            return;
        }
        InputHandler input = new InputHandler();

        Scene scene = new Scene(view, GameWorld.WIDTH, GameWorld.HEIGHT);
        stage.setTitle("Brick Rogue");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        input.attach(scene);

        // Game logic runs on its own thread; drawing happens on the JavaFX thread every frame.
        gameLoop = new GameLoop(world, input);
        drawingLoop = new AnimationTimer() {
            private long secondStart;
            private int frames;
            private int framesPerSecond;

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
        };
        gameLoop.start();
        drawingLoop.start();
        logger.info("Brick Rogue window opened");
    }

    @Override
    public void stop() {
        if (drawingLoop != null) {
            drawingLoop.stop();
        }
        if (gameLoop != null) {
            gameLoop.stop();
        }
        logger.info("Brick Rogue closed");
    }
}
