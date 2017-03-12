package ichttt.logicsimModLoader.config.entry;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since 0.0.1
 */
public class IntConfigEntry extends ConfigEntryBase {
    public int value;

    /**
     * @since 0.0.1
     */
    public IntConfigEntry(boolean allowNegativeValues, String key, int value, List<String> commentLines) {
        super(key, commentLines, (allowNegativeValues ? (Integer.toString(Integer.MIN_VALUE)) : 0) + " up to " + Integer.toString(Integer.MAX_VALUE));
        this.value = value;
    }

    /**
     * @since 0.0.1
     */
    public IntConfigEntry(boolean allowNegativeValues, String key, int value, String comment) {
        super(key, comment, (allowNegativeValues ? (Integer.toString(Integer.MIN_VALUE)) : 0) + " up to " + Integer.toString(Integer.MAX_VALUE));
        this.value = value;
    }

    @Nonnull
    @Override
    public List<String> generateLines() {
        List<String> lines = super.generateLines();
        lines.add(String.format("\"%s\" = %s", key, value));
        return lines;
    }
}
