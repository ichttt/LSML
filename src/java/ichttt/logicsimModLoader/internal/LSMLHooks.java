package ichttt.logicsimModLoader.internal;

import ichttt.logicsimModLoader.event.LSMLEventBus;
import ichttt.logicsimModLoader.event.loading.LSMLInitEvent;
import ichttt.logicsimModLoader.gui.MenuBarHandler;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.init.ProgressBarManager;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Internal class for all hooks in the LogicSim Code
 * @since 0.0.1
 */
public class LSMLHooks {

    /**
     * @since 0.0.1
     */
    public static void doInit() {
        ProgressBarManager.stepBar("Sending Init to mods...");
        LSMLEventBus.EVENT_BUS.post(new LSMLInitEvent());
    }

    /**
     * @since 0.0.1
     */
    public static void menuBar(JMenuBar bar) {
        MenuBarHandler.addItems(bar);
    }

    public static void helpItem(JMenu help) {
        JMenuItem aboutLsml = new JMenuItem(LogicSimModLoader.translate("aboutLSML"));
        aboutLsml.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                LSMLUtil.showMessageDialogOnWindowIfAvailable("LSML - The LogicSim Mod Loader\nCopyright Tobias Hotz, 2017.\nLicensed under GPL2+");
            }
        });
        help.add(aboutLsml);
    }
}
