package ichttt.logicsimModLoader.gui;

import com.google.common.base.Strings;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.loader.Loader;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Internal class handling the mods JMenuItem
 * @since 0.0.1
 */
public class MenuBarHandler implements ActionListener {
    public static final JMenu mods = new JMenu("Mods");
    private static final JMenuItem settings = new JMenuItem(LogicSimModLoader.translate("showModSettings"));
    private static final JMenuItem modList = new JMenuItem(LogicSimModLoader.translate("showMods"));

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
            LSMLLog.error("Unknown source %s" + e.getSource());
    }

    private static void listMods() {
        StringBuilder builder = new StringBuilder();
        builder.append(LogicSimModLoader.translate("loadedMods"));
        for (ModContainer container : Loader.getInstance().getMods()) {
            if (Strings.isNullOrEmpty(container.mod.author()))
                builder.append("\n").append(String.format("%s v.%s (modid %s)", container.mod.modName(), container.VERSION.getVersionString(), container.mod.modid()));
            else
                builder.append("\n").append(String.format("%s v.%s by %s (modid %s)", container.mod.modName(), container.VERSION.getVersionString(), container.mod.author(), container.mod.modid()));
        }
        LSMLUtil.showMessageDialogOnWindowIfAvailable(builder.toString());
    }
}
