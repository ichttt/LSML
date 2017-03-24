package ichttt.logicsimModLoader.gui;

import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.init.LogicSimModLoader;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @since 0.0.1
 * Manages the ModList. You can register your own entry using e.g. {@link #registerModGui(Mod, IModGuiInterface)}
 * or {@link ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent#registerModGui(Mod, IModGuiInterface)}
 */
public class ModListGui implements ListSelectionListener {
    private JSplitPane configPanel;
    private JDialog dialog;
    private JPanel left;
    private static final List<String> modids = new ArrayList<>();
    private static final List<IModGuiInterface> modGuiInterfaces = new ArrayList<>();

    private static ModListGui INSTANCE = null;

    static void setVisible() {
        if (INSTANCE == null) {
            INSTANCE = new ModListGui();
        }
        INSTANCE.dialog.setVisible(true);
    }

    private ModListGui() {
        dialog = new JDialog(LogicSimModLoader.getApp().frame);
        dialog.setTitle("Mod Options");
        JList<String> containerJList = new JList<>();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        modids.forEach(listModel::addElement);
        modGuiInterfaces.forEach(IModGuiInterface::setup);
        containerJList.setModel(listModel);
        containerJList.addListSelectionListener(this);
        left = new JPanel();
        configPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, modGuiInterfaces.get(0).draw());
        left.add(containerJList);
        dialog.add(configPanel);
        dialog.setModal(true);
        dialog.setMinimumSize(new Dimension(600, 400));
        dialog.pack();
        //noinspection ConstantConditions
        dialog.setLocation(LogicSimModLoader.getApp().lsframe.getX(), LogicSimModLoader.getApp().lsframe.getY()); //FIXME
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() instanceof JList) {
            JList list = (JList) e.getSource();
            IModGuiInterface guiInterface = modGuiInterfaces.get(list.getSelectedIndex());
            configPanel.setRightComponent(guiInterface.draw());
            dialog.repaint();
            dialog.printAll(dialog.getGraphics());
        }
    }

    /**
     * Registers your own {@link IModGuiInterface} to the system. It's best to do this during the {@link ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent}
     * @param mod Your mod
     * @param guiInterface Your guiInterface
     * @since 0.0.1
     */
    public static void registerModGui(Mod mod, IModGuiInterface guiInterface) {
        modids.add(mod.modid());
        modGuiInterfaces.add(guiInterface);
    }
}
