package ichttt.logicsimModLoader.init;

import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.config.ConfigCategory;
import ichttt.logicsimModLoader.config.entry.BooleanConfigEntry;
import ichttt.logicsimModLoader.config.entry.IConfigEntryParser;
import ichttt.logicsimModLoader.config.entry.IntConfigEntry;
import ichttt.logicsimModLoader.config.entry.StringConfigEntry;
import ichttt.logicsimModLoader.exceptions.MalformedConfigException;
import ichttt.logicsimModLoader.internal.LSMLLog;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic config fields for everyone
 */
public class ConfigInit {
    private static boolean hasInit = false;

    static void init() {
        if (hasInit) {
            LSMLLog.warning("Called init twice!");
            return;
        }
        Config.registerCustomEntryParser(new StringParser());
        Config.registerCustomEntryParser(new IntParser());
        Config.registerCustomEntryParser(new BooleanParser());
        hasInit = true;
    }


    private static class StringParser implements IConfigEntryParser {

        @Override
        public boolean parseCommentLine(@Nonnull String line) {
            return line.contains("* Allowed values: Every String");
        }

        @Override
        public void parseActualLine(@Nonnull String line, @Nonnull List<String> comments, @Nonnull ConfigCategory currentCategory) {
            String[] split = splitHelper(line, "\"");
            if (split.length != 3) throw new MalformedConfigException("String", line);
            currentCategory.addEntry(new StringConfigEntry(split[0], split[2], new ArrayList<String>(comments)));
        }
    }


    private static class IntParser implements IConfigEntryParser {
        boolean allowNegative;

        @Override
        public boolean parseCommentLine(@Nonnull String line) {
            if (line.contains("* Allowed values: 0 up to 2147483647")) {
                allowNegative = false;
                return true;
            }
            if (line.contains("* Allowed values: -2147483647 up to 2147483647")) {
                allowNegative = true;
                return true;
            }
            return false;
        }

        @Override
        public void parseActualLine(@Nonnull String line, @Nonnull List<String> comments, @Nonnull ConfigCategory currentCategory) {
            String[] split = splitHelper(line, "\"");
            if (split.length != 2) throw new MalformedConfigException("Int", line);
            String[] split2 = splitHelper(line, "=");
            if (split2.length != 1) throw new MalformedConfigException("Int", line);
            int i = Integer.parseInt(split2[0].replaceAll(" ", ""));
            currentCategory.addEntry(new IntConfigEntry(allowNegative, split[0], i, new ArrayList<String>(comments)));
        }
    }


    private static class BooleanParser implements IConfigEntryParser {

        @Override
        public boolean parseCommentLine(@Nonnull String line) {
            return line.contains("* Allowed values: \"true\" or \"false\"");
        }

        @Override
        public void parseActualLine(@Nonnull String line, @Nonnull List<String> comments, @Nonnull ConfigCategory currentCategory) {
            String[] split = splitHelper(line, "\"");
            if (split.length != 2) throw new MalformedConfigException("Boolean", line);
            String[] split2 = splitHelper(line, "=".replaceAll(" ", ""));
            if (split2.length != 1) throw new MalformedConfigException("Boolean", line);
            boolean bool = Boolean.parseBoolean(split2[0].replaceAll(" ", ""));
            currentCategory.addEntry(new BooleanConfigEntry(split[0], bool, new ArrayList<String>(comments)));
        }
    }


    private static String[] splitHelper(String line, String regex) {
        String[] rawSplit = line.split(regex);
        String[] split;
        if (rawSplit.length > 1) {
            split = new String[rawSplit.length-1];
            System.arraycopy(rawSplit, 1, split, 0, split.length);
            return split;
        }
        else {
            return rawSplit;
        }
    }
}
