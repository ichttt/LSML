package ichttt.logicsimModLoader.config.entry;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since 0.0.1
 */
public class StringConfigEntry extends ConfigEntryBase {
    @Nonnull
    public String value;

    /**
     * @since 0.0.1
     */
    public StringConfigEntry(String key, String value, List<String> commentLines) {
        super(key, commentLines, "Every String");
        this.value = value;
    }

    /**
     * @since 0.0.1
     */
    public StringConfigEntry(String key, String value, String comment) {
        super(key, comment, "Every String");
        this.value = value;
    }

    @Nonnull
    @Override
    public List<String> generateLines() {
        List<String> lines = super.generateLines();
        lines.add(String.format("\"%s\" = \"%s\"", key, value));
        return lines;
    }

}
