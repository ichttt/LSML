package ichttt.logicsimModLoader.exceptions;

import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.api.Mod;

import java.util.MissingResourceException;

/**
 * Throw this if you detect a missing dependency
 * @since 0.0.2
 */
public class MissingDependencyException extends RuntimeException{
    public MissingDependencyException(Mod yourMod, String requiredModID, VersionBase requiredVersion) {
        super(String.format("Mod %s requires the mod %s at version %s or later.", yourMod.modName(), requiredModID, requiredVersion.getVersionString()));
    }
}
