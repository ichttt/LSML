package ichttt.logicsimModLoader.test.unittest;

import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.config.ConfigCategory;
import ichttt.logicsimModLoader.config.entry.BooleanConfigEntry;
import ichttt.logicsimModLoader.config.entry.IConfigEntryParser;
import ichttt.logicsimModLoader.config.entry.IntConfigEntry;
import ichttt.logicsimModLoader.config.entry.StringConfigEntry;
import ichttt.logicsimModLoader.exceptions.MalformedConfigException;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by Tobias on 05.03.2017.
 */
public class ConfigTest {
    private static final File configFile = new File(new File("").getAbsolutePath() + "/UNITTEST.cfg");
    private static final File configFile2 = new File(new File("").getAbsolutePath() + "/UNITTEST2.cfg");
    private static Config config = new Config("UNITTEST", configFile);

    private void addFirstCat() {
        ConfigCategory general = new ConfigCategory("General");
        general.addEntry(new StringConfigEntry("TestString", "I'm a test!", "This is just a test value"));
        general.addEntry(new IntConfigEntry(false, "TestInt", 2, "Just a test int"));
        general.addEntry(new BooleanConfigEntry("TestBool", true, "JUST A PRANK BRO"));
        config.addCategory(general);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test(timeout = 5000L)
    public void test() throws Exception{
        if (configFile.exists())
            configFile.delete();
        if (configFile2.exists())
            configFile2.delete();
        LogicSimModLoader.coreInit();
        addFirstCat();
        config.save();
        Files.copy(configFile.toPath(), configFile2.toPath());
        config.load();
        config.save();
        Assert.assertTrue(contentEquals(configFile, configFile2));
        config.load();
        config.load(); //Load again
        config.save();
        Assert.assertTrue(contentEquals(configFile, configFile2));
        //Cleanup
        configFile.delete();
        configFile2.delete();
    }

    @Test()
    public void testDuplicateParsers() throws Exception {
        Field f =  Config.class.getDeclaredField("entryParsers");
        f.setAccessible(true);
        //noinspection unchecked
        List<IConfigEntryParser> parsers = (List<IConfigEntryParser>) f.get(null);
        parsers.clear();
        Config.registerCustomEntryParser(new DuplicateParser());
        Config.registerCustomEntryParser(new DuplicateParser());
        Config.registerCustomEntryParser(new LegitParser());
        Assert.assertTrue(parsers.size() == 2);
    }

    private static class DuplicateParser implements IConfigEntryParser {
        @Override
        public boolean parseCommentLine(@Nonnull String line) {return false;}
        @Override
        public void parseActualLine(@Nonnull String line, @Nonnull List<String> comments, @Nonnull ConfigCategory currentCategory) throws MalformedConfigException {}
    }

    private static class LegitParser implements IConfigEntryParser {
        @Override
        public boolean parseCommentLine(@Nonnull String line) {return false;}
        @Override
        public void parseActualLine(@Nonnull String line, @Nonnull List<String> comments, @Nonnull ConfigCategory currentCategory) throws MalformedConfigException {}
    }

    //Taken from Apache IOCommons, simplified
    private static boolean contentEquals(File input1, File input2) throws IOException {
        BufferedReader input11 = new BufferedReader(new FileReader(input1));
        BufferedReader input21 = new BufferedReader(new FileReader(input2));

        int ch2;
        for(int ch = input11.read(); -1 != ch; ch = input11.read()) {
            ch2 = input21.read();
            if(ch != ch2) {
                return false;
            }
        }
        ch2 = input21.read();
        return ch2 == -1;
    }
}
