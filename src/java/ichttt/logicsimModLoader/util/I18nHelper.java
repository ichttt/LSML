package ichttt.logicsimModLoader.util;

import ichttt.logicsimModLoader.internal.LSMLLog;
import logicsim.I18N;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helper for LogicSim's {@link I18N} class to support custom files.
 * @since 0.1.4
 */
public class I18nHelper {
    private ResourceBundle DEFAULT;
    private final ResourceBundle FALLBACK;

    /**
     * Creates a I18n helper for your mod.
     * You should only create one instance of this per mod.
     * @since 0.1.4
     * @param baseName The name of your resource files without "_[Language Key]"
     */
    public I18nHelper(String baseName) {
        FALLBACK = ResourceBundle.getBundle(baseName, Locale.ENGLISH);
        try {
            DEFAULT = ResourceBundle.getBundle(baseName + "_" + I18N.getLanguageKey());
        }  catch (MissingResourceException e) {
            LSMLLog.info("Falling back to english local for file " + baseName);
            DEFAULT = FALLBACK;
        }
    }

    /**
     * Tries to translate a String to the selected local. If this fails, it will try to fall back to English.
     * If this fails, too, the raw key will be returned.
     * @param key the key for the desired string
     * @return The translated String
     * @since 0.1.4
     */
    public String translate(String key) {
        try {
            return DEFAULT.getString(key);
        } catch (MissingResourceException e) {
            e.printStackTrace();
            try {
                return FALLBACK.getString(key);
            } catch (MissingResourceException e1) {
                return key;
            }
        }
    }
}
