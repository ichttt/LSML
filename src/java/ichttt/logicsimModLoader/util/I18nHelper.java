package ichttt.logicsimModLoader.util;

import com.google.common.base.Preconditions;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.loader.Loader;
import logicsim.I18N;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * Helper for LogicSim's {@link I18N} class to support custom files.
 * @since 0.1.4
 */
public class I18nHelper {
    @Nullable
    private ResourceBundle DEFAULT, FALLBACK;

    /**
     * Creates a I18n helper for your mod.
     * You should only create one instance of this per mod.
     * @since 0.1.4
     * @param baseName The name of your resource files without "_[Language Key]"
     */
    public I18nHelper(String baseName) {
        this(baseName, Preconditions.checkNotNull(Loader.getInstance().getModClassLoader()));
    }

    public I18nHelper(String baseName, ClassLoader classLoader) {
        try {
            FALLBACK = ResourceBundle.getBundle(baseName, Locale.ENGLISH, classLoader);
        } catch (MissingResourceException e) {
            LSMLLog.log(String.format("Error loading default resources for %s, strings may not be translated if selected.", baseName), Level.SEVERE, e);
            FALLBACK = null;
        }
        try {
            DEFAULT = ResourceBundle.getBundle(baseName, Locale.forLanguageTag(I18N.getLanguageKey()), classLoader);
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
        } catch (MissingResourceException | NullPointerException e) {
            e.printStackTrace();
            try {
                return FALLBACK.getString(key);
            } catch (MissingResourceException | NullPointerException e1) {
                return key;
            }
        }
    }
}
