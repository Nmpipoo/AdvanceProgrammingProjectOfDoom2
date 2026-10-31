package se233.project2.exception;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The single place where GameExceptions are handled: log at ERROR and tell the player.
public final class ExceptionHandler {

    private static final Logger logger = LogManager.getLogger(ExceptionHandler.class);

    private ExceptionHandler() {
    }

    public static void handle(GameException e) {
        logger.error(e.getMessage(), e);
        showAlert(e.getMessage());
    }

    // For errors the game can't run without (e.g. a broken stage file at start-up):
    // show the message, then close once the player dismisses it.
    public static void handleFatal(GameException e) {
        logger.fatal(e.getMessage(), e);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Game can't start");
        alert.setHeaderText(null);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
        Platform.exit();
    }

    // Anything that escapes a thread uncaught is wrapped so it still ends up in handle().
    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            GameException e = throwable instanceof GameException ge
                    ? ge
                    : new GameException("Unexpected error in " + thread.getName(), throwable);
            handle(e);
        });
    }

    private static void showAlert(String message) {
        Runnable show = () -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Game error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        };
        try {
            if (Platform.isFxApplicationThread()) {
                show.run();
            } else {
                Platform.runLater(show);
            }
        } catch (IllegalStateException toolkitNotRunning) {
            // No JavaFX toolkit (e.g. in unit tests); the log entry is enough.
        }
    }
}
