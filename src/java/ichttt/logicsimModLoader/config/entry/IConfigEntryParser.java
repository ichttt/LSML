package ichttt.logicsimModLoader.config.entry;

import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.config.ConfigCategory;
import ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent;
import ichttt.logicsimModLoader.exceptions.MalformedConfigException;

import java.util.List;

/**
 * This must be created for each class that overrides {@link ConfigEntryBase}.
 * <br> <b>Be sure to only add this once per Class, not per instance</b>.
 * Register this in {@link Config#registerCustomEntryParser(IConfigEntryParser)} or {@link LSMLRegistrationEvent#registerConfigParser(IConfigEntryParser)}
 * @since 0.0.1
 */
public interface IConfigEntryParser {

    /**
     * Parses all lines during {@link Config#load()}. If you identify your {@link ConfigEntryBase#postComment} here,
     * return true and {@link #parseActualLine(String, List, ConfigCategory)} will be fired.
     * @return true, if {@link #parseActualLine(String, List, ConfigCategory)} should be fired next
     * @since 0.0.1
     */
    boolean parseCommentLine(String line);

    /**
     * Parses the line containing the name of the field and the current value.
     * @param line The current line
     * @param comments The previous read comments (without postComment)
     * @param currentCategory The current category
     * @since 0.0.1
     */
    void parseActualLine(String line, List<String> comments, ConfigCategory currentCategory) throws MalformedConfigException;
}
