package ichttt.logicsimModLoader.config.entry;

import ichttt.logicsimModLoader.config.ConfigElement;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Extend this if you want to define custom config fields
 * <br><b>Be sure to also register a {@link IConfigEntryParser} when extending this </b>
 * @since 0.0.1
 * @param <T> The type for {@link #setValue(Object)} and {@link #getValue()}, available since v0.1.1
 */
public abstract class ConfigEntryBase<T> extends ConfigElement {
    public final String key, postComment;
    private final List<String> comment;
    public static final String POST_COMMENT = "Allowed values: ";

    /**
     * @since 0.0.1
     */
    public ConfigEntryBase(String key, List<String> commentLines, String allowedValues) {
        super(1);
        this.key = key;
        this.comment = commentLines;
        this.postComment = POST_COMMENT + allowedValues;
    }

    /**
     * @since 0.0.1
     */
    public ConfigEntryBase(String key, String comment, String allowedValues) {
        super(1);
        this.comment = new ArrayList<>(1);
        this.comment.add(comment);
        this.key = key;
        this.postComment = POST_COMMENT + allowedValues;
    }

    @Nonnull
    @Override
    public List<String> generateLines() {
        List<String> lines = new ArrayList<>(comment.size() + 1);
        for (String line : comment) {
            lines.add("* " + line);
        }
        lines.add("* " + postComment);
        return lines;
    }

    /**
     * Sets the value. If this fails due to a {@link UnsupportedOperationException}, a new ConfigEntry might be created,
     * deleting any reference that was made.
     * ConfigEntry authors: Override this if you target 0.1.1+!
     * @param data the value
     * @since 0.1.1
     * @throws UnsupportedOperationException if the configEntry does not support swapping the value this way
     */
    public void setValue(T data) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Could not set ConfigEntry value!");
    }

    /**
     * Sets the value. If this fails due to a {@link UnsupportedOperationException}, a new ConfigEntry might be created,
     * deleting any reference that was made.
     * ConfigEntry authors: Override this if you target 0.1.1+!
     * @since 0.1.1
     * @return The value
     * @throws UnsupportedOperationException if the configEntry does not support swapping the value this way
     */
    @Nonnull
    public T getValue() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Could not load ConfigEntry value!");
    }
}
