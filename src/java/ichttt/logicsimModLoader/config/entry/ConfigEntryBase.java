package ichttt.logicsimModLoader.config.entry;

import ichttt.logicsimModLoader.config.ConfigElement;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Extend this if you want to define custom config fields
 * <br><b>Be sure to also register a {@link IConfigEntryParser} when extending this </b>
 * @since 0.0.1
 */
public abstract class ConfigEntryBase extends ConfigElement {
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
        List<String> lines = comment.stream().
                map(aComment -> "* " + aComment).
                collect(Collectors.toList());
        lines.add("* " + postComment);
        return lines;
    }
}
