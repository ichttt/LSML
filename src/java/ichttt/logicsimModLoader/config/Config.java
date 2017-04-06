package ichttt.logicsimModLoader.config;


import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.config.entry.ConfigEntryBase;
import ichttt.logicsimModLoader.config.entry.IConfigEntryParser;
import ichttt.logicsimModLoader.exceptions.MalformedConfigException;
import ichttt.logicsimModLoader.exceptions.ModException;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A basic config all mods can utilize.
 * It is best to call this during init, so other mods may add custom {@link ConfigEntryBase} during registration.
 * @since 0.0.1
 */
public class Config extends ConfigElement {
    private final File configFile;
    private final List<ConfigCategory> categoryList = new ArrayList<>();
    private final String modName;
    private static final List<IConfigEntryParser> entryParsers = new ArrayList<>();
    private static boolean registrationAllowed = true;

    public Config(String modName, File configFile) {
        super(0);
        this.configFile = configFile;
        this.modName = modName;
        if (configFile.isDirectory())
            throw new IllegalArgumentException(String.format("File %s is a dictionary, should be config file!", configFile));
    }

    public Config(ModContainer mod) {
        this(mod.mod.modName(), mod.getSuggestedConfigFile());
    }

    @Override
    @Nonnull
    public List<String> generateLines() {
        String CONFIG_FOR_MOD = "Config for mod ";
        List<String> lines = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 30+CONFIG_FOR_MOD.length()+modName.length(); i++) {
            builder.append("-");
        }
        String s = builder.toString();
        lines.add(s);
        lines.add("---------------" + CONFIG_FOR_MOD + modName + "---------------");
        lines.add(s);
        lines.add("");
        for (ConfigCategory category : categoryList) {
            lines.addAll(category.getOffsetLines());
            lines.add("");
            lines.add("");
        }
        //remove the last line, it is noise
        lines.remove(lines.size()-1);
        return lines;
    }

    public void addCategory(ConfigCategory category) {
        categoryList.add(category);
    }

    /**
     * Loads the current fields from the config.
     * If the config file does not exist, this will do nothing!
     * @throws MalformedConfigException If the config could not be read.
     * @since 0.0.1
     */
    public void load() throws MalformedConfigException {
        BufferedReader br = null;
        try {
            if (!configFile.exists() || !configFile.isFile()) {
                return;
            }
            br = new BufferedReader(new FileReader(configFile));

            ConfigCategory currentCategory = null;
            List<String> commentLines = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                br.readLine();
            }
            IConfigEntryParser foundParser = null;
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.isEmpty() || line.replaceAll(" ", "").isEmpty()) {
                    continue;
                }
                if (line.contains("----------")) {
                    if (!line.contains("--------------------")) {
                        line = line.replaceAll(" ", "");
                        line = line.replaceAll("-", "");
                        boolean set = false;
                        for (ConfigCategory category : categoryList) {
                            if (category.name.equals(line)) {
                                currentCategory = category;
                                set = true;
                                break;
                            }
                        }
                        if (!set) {
                            currentCategory = new ConfigCategory(line);
                            categoryList.add(currentCategory);
                        }
                    }
                    continue;
                }

                if (foundParser != null) {
                    if (currentCategory == null)
                        throw new MalformedConfigException("Current Category is not set but values are present!");
                    foundParser.parseActualLine(line, commentLines, currentCategory);
                    commentLines.clear();
                    foundParser = null;
                    continue;
                }

                for (IConfigEntryParser parser : entryParsers) {
                    if (parser.parseCommentLine(line)) {
                        if (foundParser != null) LSMLLog.warning("Two ConfigEntryParsers claimed to be able to parse the next field. This can cause stability issues!");
                        foundParser = parser;
                    }
                }

                if (foundParser != null) continue;

                if (line.contains("*")) {
                    String[] split2 = line.split("\\* "); //Escape the *
                    if (split2.length != 2) throw new MalformedConfigException("Comment", line);
                    commentLines.add(split2[1]);
                    continue;
                }
                if (line.replaceAll(" ", "").equals("|")) continue;

                throw new MalformedConfigException("Could not find an association for the following line:\n" + line);
            }
        }
        catch (IOException e) {
            LSMLLog.error("Error while loading config file %s!\nThis is a critical error, stopping now!", configFile.toPath());
            throw new RuntimeException("Could not load config file!");
        }
        finally {
            LSMLUtil.closeSilent(br);
        }
    }

    /**
     * Saves your config to disk, if you have done any changes
     * @return true if the save was successful
     * @since 0.0.1
     */
    public boolean save() {
        BufferedWriter bf = null;
        try {
            if (!configFile.exists())
                if (!configFile.createNewFile())
                    throw new IOException();
            bf = new BufferedWriter(new FileWriter(configFile));
            List<String> lines = getOffsetLines();
            for (String s : lines) {
                bf.write(s);
                bf.newLine();
            }
        }
        catch (IOException e) {
            LSMLLog.error("Error while saving config file %s!", configFile.toPath());
            return false;
        }
        finally {
            LSMLUtil.closeSilent(bf);
        }
        return true;
    }

    /**
     * INTERNAL USE ONLY
     * @since 0.0.2
     */
    public static void closeRegistrationWindow() {
        if (!registrationAllowed) {
            LSMLLog.fine("Should close registration window but already closed!");
            return;
        }
        if (LSMLUtil.isCalledFromModCode()) {
            LSMLLog.error("A mod tried closing the registration window. THIS IS NOT ALLOWED!");
            return;
        }
        registrationAllowed = false;
    }

    /**
     * Register your own custom {@link IConfigEntryParser}.
     * Only register one time per class
     * <b>This should be done during LSMLRegistrationEvent</b>.
     * @param parser Your custom parser
     * @since 0.0.1
     */
    public static void registerCustomEntryParser(IConfigEntryParser parser) {
        if (!registrationAllowed) {
            Mod mod = LSMLUtil.getActiveModFromCurrentThread();
            if (mod != null)
                LSMLLog.warning("A config parser from mod %s (modid %s) is registered late!", mod.modName(), mod.modid());
            else
                LSMLLog.warning("A config parser is registered late!");
        }
        final boolean[] duplicateParser = new boolean[1];
        entryParsers.forEach(parser1 -> {
            if (parser1.getClass().equals(parser.getClass()))
                duplicateParser[0] = true;
        });
        if (!entryParsers.contains(parser) &&  !duplicateParser[0]) {
            entryParsers.add(parser);
        }
        else
            LSMLLog.warning("Tried registering the same ConfigEntryParsers (%s) twice!", parser.getClass().getName());
    }
}
