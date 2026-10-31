package se233.project2.exception;

// The one exception type the game throws. Every failure is wrapped in this and
// sent to ExceptionHandler, so all error handling lives in one place.
public class GameException extends RuntimeException {

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
