package ichttt.logicsimModLoader.test.unittest;

import ichttt.logicsimModLoader.util.VanillaGateType;
import logicsim.AND;
import logicsim.LED;
import logicsim.NAND;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link VanillaGateType}
 */
public class VanillaGateTypeTest {
    @Test
    public void getTypeFromGate() throws Exception {
        AND and = new AND();
        Assert.assertEquals(VanillaGateType.AND, VanillaGateType.getTypeFromGate(and));
        NAND nand = new NAND();
        Assert.assertEquals(VanillaGateType.NAND, VanillaGateType.getTypeFromGate(nand));
        LED led = new LED();
        Assert.assertEquals(VanillaGateType.LED, VanillaGateType.getTypeFromGate(led));
    }

}