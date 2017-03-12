package ichttt.logicsimModLoader.gui;

import com.google.common.base.Preconditions;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.init.LogicSimModLoader;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 0.0.1
 * Manages the ModList. You can register your own entry using e.g. {@link #registerModGui(Mod, IModGuiInterface)}
 * or {@link ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent#registerModGui(Mod, IModGuiInterface)}
 */
public class ModListGui implements ListSelectionListener {
    private JSplitPane configPanel;
    private JDialog dialog;
    private JPanel left;
    private static final List<IModGuiInterface> modInterfaces = new ArrayList<>();
    private static final Map<Mod, IModGuiInterface> modInterfacesMap = new HashMap<>();

    private static ModListGui INSTANCE = null;

    static void setVisible() {
        if (INSTANCE == null) {
            INSTANCE = new ModListGui();
        }
        INSTANCE.dialog.setVisible(true);
    }

    private ModListGui() {
        dialog = new JDialog();
        JList<String> containerJList = new JList<>();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        modInterfacesMap.keySet().forEach((Mod mod) -> listModel.addElement(mod.modName()));
        modInterfaces.forEach(IModGuiInterface::setup);
        containerJList.setModel(listModel);
        containerJList.addListSelectionListener(this);
        left = new JPanel();
        configPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, modInterfaces.get(0).draw());
        configPanel.setDividerLocation(JSplitPane.HORIZONTAL_SPLIT);
        left.add(containerJList);
        dialog.add(configPanel);
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocation(LogicSimModLoader.getApp().lsframe.getX(), LogicSimModLoader.getApp().lsframe.getY());
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() instanceof JList) {
            JList list = (JList) e.getSource();
            IModGuiInterface guiInterface = modInterfaces.get(list.getSelectedIndex());
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
        modInterfaces.add(guiInterface); //TODO Ugly hack
        modInterfacesMap.put(mod, guiInterface);
    }
}
