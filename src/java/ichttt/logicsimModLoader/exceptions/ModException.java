package ichttt.logicsimModLoader.exceptions;

import ichttt.logicsimModLoader.api.Mod;

/**
 * @since 0.0.1
 */
public class ModException extends RuntimeException {
    public ModException(Mod mod, String message, Throwable cause) {
        super("Exception while processing mod " + mod + "\n" + message, cause);
    }

    public ModException(Mod mod, String message) {
        super("Exception while processing mod " + mod + "\n" + message);
    }
}
