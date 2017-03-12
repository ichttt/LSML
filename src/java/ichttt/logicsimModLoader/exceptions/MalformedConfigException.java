package ichttt.logicsimModLoader.exceptions;

/**
 * @since 0.0.1
 */
public class MalformedConfigException extends RuntimeException {
    public MalformedConfigException(String type, String line) {
        super(String.format("Error processing %s.\nAffected Line: %s", type, line));
    }

    public MalformedConfigException(String s) {
        super(s);
    }
}
