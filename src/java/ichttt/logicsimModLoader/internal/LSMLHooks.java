package ichttt.logicsimModLoader.internal;

import ichttt.logicsimModLoader.ModState;
import ichttt.logicsimModLoader.event.LSMLEventBus;
import ichttt.logicsimModLoader.event.loading.LSMLInitEvent;
import ichttt.logicsimModLoader.gui.MenuBarHandler;

import javax.swing.*;

/**
 * Internal class for all hooks in the LogicSim Code
 * @since 0.0.1
 */
public class LSMLHooks {

    /**
     * @since 0.0.1
     */
    public static void doInit() {
        LSMLEventBus.EVENT_BUS.post(new LSMLInitEvent());
        ModContainer.doTransitionOnAllMods(ModState.INIT);
    }

    /**
     * @since 0.0.1
     */
    public static void menuBar(JMenuBar bar) {
        MenuBarHandler.addItems(bar);
    }
}
