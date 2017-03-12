package ichttt.logicsimModLoader.config.entry;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since 0.0.1
 */
public class BooleanConfigEntry extends ConfigEntryBase {
    public boolean value;
    /**
     * @since 0.0.1
     */
    public BooleanConfigEntry(String key, boolean value, List<String> commentLines) {
        super(key, commentLines, "\"true\" or \"false\"");
        this.value = value;
    }

    /**
     * @since 0.0.1
     */
    public BooleanConfigEntry(String key, boolean value, String comment) {
        super(key, comment, "\"true\" or \"false\"");
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