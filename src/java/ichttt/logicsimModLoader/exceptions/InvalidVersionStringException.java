package ichttt.logicsimModLoader.exceptions;

/**
 * @since 0.0.1
 */
public class InvalidVersionStringException extends RuntimeException {
    public InvalidVersionStringException (String message) {
        super("Found invalid Version String: " + message);
    }
}
