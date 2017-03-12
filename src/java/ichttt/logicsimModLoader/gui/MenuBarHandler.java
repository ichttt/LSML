package ichttt.logicsimModLoader.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Internal class handling the mods JMenuItem
 * @since 0.0.1
 */
public class MenuBarHandler implements ActionListener {
    public static final JMenu mods = new JMenu("Mods");
    private static JMenuItem item = new JMenuItem("Open mods settings");

    /**
     * Internal method, do not call
     */
    public static void addItems(JMenuBar bar) {
        item.addActionListener(new MenuBarHandler());
        mods.add(item);
        bar.add(MenuBarHandler.mods);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        JOptionPane.showMessageDialog(LogicSimModLoader.getApp().lsframe, "Loaded mods:" + modList);
        ModListGui.setVisible();
    }
}
