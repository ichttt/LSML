package ichttt.logicsimModLoader.util;

import logicsim.Gate;

import javax.annotation.Nullable;

/**
 * Helper for specifying a gate type.
 * @since 0.2.3
 */
@SuppressWarnings("SpellCheckingInspection")
public enum VanillaGateType {
    AND, BININ, CLK, DFlipFlop, DRFlipFlop, Dummy, EQU, HIGH, JKCFlipFlop, LCD, LED, LOW,
    MODIN, MODOUT, Module, MonoFlop, NAND, NOR, NOT, OffDelay, OR, RSFlipFlop, SevenSegment,
    SWITCH, TextLabel, XOR;

    @Nullable
    public static VanillaGateType getTypeFromGate(Gate gate) {
        Class<? extends Gate> gateClass = gate.getClass();
        if (gateClass.equals(logicsim.AND.class))
            return AND;
        if (gateClass.equals(logicsim.BININ.class))
            return BININ;
        if (gateClass.equals(logicsim.CLK.class))
            return CLK;
        if (gateClass.equals(logicsim.DFlipFlop.class))
            return DFlipFlop;
        if (gateClass.equals(logicsim.DRFlipFlop.class))
            return DRFlipFlop;
        if (gateClass.equals(logicsim.Dummy.class))
            return Dummy;
        if (gateClass.equals(logicsim.EQU.class))
            return EQU;
        if (gateClass.equals(logicsim.HIGH.class))
            return HIGH;
        if (gateClass.equals(logicsim.JKCFlipFlop.class))
            return JKCFlipFlop;
        if (gateClass.equals(logicsim.LCD.class))
            return LCD;
        if (gateClass.equals(logicsim.LED.class))
            return LED;
        if (gateClass.equals(logicsim.LOW.class))
            return LOW;
        if (gateClass.equals(logicsim.MODIN.class))
            return MODIN;
        if (gateClass.equals(logicsim.MODOUT.class))
            return MODOUT;
        if (gateClass.equals(logicsim.Module.class))
            return Module;
        if (gateClass.equals(logicsim.MonoFlop.class))
            return MonoFlop;
        if (gateClass.equals(logicsim.NAND.class))
            return NAND;
        if (gateClass.equals(logicsim.NOR.class))
            return NOR;
        if (gateClass.equals(logicsim.NOT.class))
            return NOT;
        if (gateClass.equals(logicsim.OffDelay.class))
            return OffDelay;
        if (gateClass.equals(logicsim.OR.class))
            return OR;
        if (gateClass.equals(logicsim.RSFlipFlop.class))
            return RSFlipFlop;
        if (gateClass.equals(logicsim.SevenSegment.class))
            return SevenSegment;
        if (gateClass.equals(logicsim.SWITCH.class))
            return SWITCH;
        if (gateClass.equals(logicsim.TextLabel.class))
            return TextLabel;
        if (gateClass.equals(logicsim.XOR.class))
            return XOR;
        return null;
    }
}
