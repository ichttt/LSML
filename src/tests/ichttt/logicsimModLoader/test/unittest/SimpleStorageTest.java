package ichttt.logicsimModLoader.test.unittest;

import ichttt.logicsimModLoader.util.SimpleStorage;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link SimpleStorage}
 */
public class SimpleStorageTest {
    public static SimpleStorage setup() {
        SimpleStorage simpleStorage = new SimpleStorage();
        simpleStorage.putEntry("test1", "false");
        simpleStorage.putEntry("test2", "slick");
        return simpleStorage;
    }

    @Test
    public void testSaveAndLoad() {
       SimpleStorage simpleStorage = setup();
       simpleStorage.loadLines(simpleStorage.saveLines());
       Assert.assertEquals("false", simpleStorage.getValue("test1"));
       Assert.assertEquals("slick", simpleStorage.getValue("test2"));
       Assert.assertNull(simpleStorage.getValue("test3"));
    }
}
