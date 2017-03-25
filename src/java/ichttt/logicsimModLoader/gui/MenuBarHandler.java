package ichttt.logicsimModLoader.gui;

import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.loader.Loader;
import ichttt.logicsimModLoader.util.LSMLUtil;
import jdk.nashorn.internal.scripts.JO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Internal class handling the mods JMenuItem
 * @since 0.0.1
 */
public class MenuBarHandler implements ActionListener {
    public static final JMenu mods = new JMenu("Mods");
    private static JMenuItem settings = new JMenuItem("Open mods settings");
    private static JMenuItem modList = new JMenuItem("Show loaded mods");

    /**
     * Internal method, do not call
     */
    public static void addItems(JMenuBar bar) {
        MenuBarHandler handler = new MenuBarHandler();
        settings.addActionListener(handler);
        modList.addActionListener(handler);
        mods.add(modList);
        mods.add(settings);
        bar.add(MenuBarHandler.mods);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(settings))
            ModListGui.setVisible();
        else if (e.getSource().equals(modList)) {
            listMods();
        }
        else
            LSMLLog.error("Unkown source %s" + e.getSource());
    }

    private static void listMods() {
        StringBuilder builder = new StringBuilder();
        builder.append("Loaded mods:");
        for (ModContainer container : Loader.getInstance().getMods()) {
            builder.append("\n").append(String.format("%s v.%s (modid %s)", container.mod.modName(), container.VERSION.getVersionString(), container.mod.modid()));
        }
        LSMLUtil.showMessageDialogOnWindowIfAvailable(builder.toString());
    }
}
