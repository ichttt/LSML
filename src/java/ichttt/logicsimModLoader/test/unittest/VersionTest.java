package ichttt.logicsimModLoader.test.unittest;

import ichttt.logicsimModLoader.VersionBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Tobias on 16.04.2017.
 */
public class VersionTest {

    @Test
    public void test() {
        VersionBase v1 = new VersionBase(0, 0, 2);
        VersionBase v2 = new VersionBase(0, 1, 0);
        VersionBase v3 = new VersionBase(0, 0, 3);
        VersionBase v4 = new VersionBase(1, 0, 1);
        List<VersionBase> list = new ArrayList<>();
        list.add(v1);
        list.add(v2);
        list.add(v3);
        list.add(v4);
        Collections.sort(list);
        Assert.assertEquals(v1, list.get(0));
        Assert.assertEquals(v3, list.get(1));
        Assert.assertEquals(v2, list.get(2));
        Assert.assertEquals(v4, list.get(3));
        Assert.assertTrue(v4.isMinimum(v3));
        Assert.assertFalse(v3.isMinimum(v2));
        Assert.assertTrue(v2.isMinimum(v2));
        VersionBase v5 = new VersionBase("0.0.2");
        Assert.assertEquals(v5, v1);
    }
}
