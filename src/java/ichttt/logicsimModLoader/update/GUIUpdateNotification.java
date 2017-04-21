package ichttt.logicsimModLoader.update;

import com.google.common.base.Strings;
import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.util.NetworkHelper;
import logicsim.App;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * The new UpdateNotification window introduced in 0.2.1
 *
 */
public class GUIUpdateNotification implements ListSelectionListener, HyperlinkListener, ActionListener {
    private static final String NEWLINE_HTML = "<br>";

    private final Map<UpdateContext, VersionBase> updateMap;
    private final List<UpdateContext> updateList = new ArrayList<>();
    private final JEditorPane editorPane = new JEditorPane();
    private final JScrollPane rightScrollPanel;
    private final JDialog dialog;
    private final JButton updateMod, updateAll, visitURL;

    private UpdateContext activeUpdateContext;

    public GUIUpdateNotification(Map<UpdateContext, VersionBase> updateMap) {
        this.updateMap = new LinkedHashMap<>(updateMap.size());
        updateMap.entrySet().stream().
                sorted(Map.Entry.comparingByKey()).
                forEach(entry -> this.updateMap.put(entry.getKey(), entry.getValue()));
        JPanel leftPanel = new JPanel(new GridBagLayout());
        JScrollPane leftScrollPanel = new JScrollPane(leftPanel);
        leftScrollPanel.setMinimumSize(new Dimension(128, 72));
        leftScrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JSplitPane mainPanel = new JSplitPane();
        mainPanel.setLeftComponent(leftScrollPanel);
        JPanel rightPanel = new JPanel(new GridLayout(1, 0));
        rightScrollPanel = new JScrollPane(rightPanel);
        JPanel rightOuterPanel = new JPanel(new GridBagLayout());
        mainPanel.setRightComponent(rightOuterPanel);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);
        this.updateMap.keySet().forEach(context -> {
            String modid = context.linkedModContainer.mod.modName();
            listModel.addElement(modid);
            updateList.add(context);
        });
        listModel.trimToSize();

        GridBagConstraints lLayout = new GridBagConstraints();
        GridBagConstraints rLayout = new GridBagConstraints();
        lLayout.weighty = 0.03;
        lLayout.weightx = 1;
        lLayout.gridx = 1;
        lLayout.gridy = 1;
        lLayout.anchor = GridBagConstraints.PAGE_START;
        lLayout.fill = GridBagConstraints.BOTH;
        updateAll = new JButton("Update all possible mods");
        updateAll.setToolTipText("Updates all mods that opted in to automatic update.\nNot all mod may be updated this way.");
        updateAll.addActionListener(event -> updateAll());
        updateAll.setEnabled(this.updateMap.keySet().stream().anyMatch(context -> context.getPathToRemoteJar() != null && context.getPathToRemoteModinfo() != null && !context.isDownloaded()));
        leftPanel.add(updateAll, lLayout);

        rLayout.weighty = 0.03;
        rLayout.weightx = 0.5;
        rLayout.gridx = 1;
        rLayout.gridy = 1;
        rLayout.anchor = GridBagConstraints.CENTER;
        rLayout.fill = GridBagConstraints.BOTH;
        updateMod = new JButton("Update mod");
        updateMod.setActionCommand("update");
        updateMod.addActionListener(this);
        rightOuterPanel.add(updateMod, rLayout);

        rLayout.gridx = 2;
        rLayout.gridy = 1;
        rLayout.weighty = 0.03;
        rLayout.weightx = 0.5;
        visitURL = new JButton("Visit Website");
        visitURL.setActionCommand("visit");
        visitURL.addActionListener(this);
        rightOuterPanel.add(visitURL, rLayout);

        rLayout.gridx = 1;
        rLayout.gridy = 2;
        rLayout.gridwidth = 2;
        rLayout.weighty = 0.97;
        rLayout.weighty = 1;
        rLayout.fill = GridBagConstraints.BOTH;
        rightOuterPanel.add(rightScrollPanel, rLayout);

        lLayout.gridx = 1;
        lLayout.gridy = 2;
        lLayout.weighty = 0.97;
        lLayout.ipady = 50;
        lLayout.weightx = 1;
        leftPanel.add(list, lLayout);
        leftPanel.setPreferredSize(new Dimension(160, 90));
        list.addListSelectionListener(this);
        list.setSelectedIndex(0);

        editorPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        editorPane.setEditable(false);
        editorPane.addHyperlinkListener(this);
        rightPanel.add(editorPane);

        App app = LogicSimModLoader.getApp();
        JFrame parent = null;
        if (app != null)
            parent = app.frame;
        dialog = new JDialog(parent);
        dialog.setTitle("Update checker");
        dialog.add(mainPanel);
        dialog.setModal(true);
        dialog.setMinimumSize(new Dimension(640, 360));
        Dimension preferredSize = app == null ? dialog.getMinimumSize() : new Dimension((int) Math.round(app.frame.getSize().width * 0.8), (int) Math.round(app.frame.getSize().height * 0.8));
        dialog.setPreferredSize(preferredSize);
        dialog.pack();
        updateRightComponent(updateList.get(0));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation((screenSize.width - dialog.getWidth())/ 2 , (screenSize.height - dialog.getHeight())/2);
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void updateRightComponent(UpdateContext context) {
        activeUpdateContext = context;
        VersionBase newVersion = updateMap.get(context);
        Mod mod = context.linkedModContainer.mod;
        URL changelogURL = context.getChangelogURL();
        URL website = context.getWebsite();
        String changelog = null;
        if (changelogURL != null) {
            try {
                changelog = NetworkHelper.readURLUncached(changelogURL);
            } catch (IOException e) {
                LSMLLog.log(String.format("Could not read changelog for mod %s with modid %s", mod.modName(), mod.modid()), Level.WARNING, e);
            }
        }
        editorPane.setText(buildText(mod, newVersion, changelog, website).replaceAll("\n", NEWLINE_HTML));
        visitURL.setEnabled(website != null);
        updateMod.setEnabled(context.getPathToRemoteJar() != null && context.getPathToRemoteModinfo() != null && !context.isDownloaded());

        SwingUtilities.invokeLater(() -> rightScrollPanel.getVerticalScrollBar().setValue(0));
    }

    private static String buildText(Mod mod, VersionBase newVersion, @Nullable String changelog, @Nullable URL website) {
        String s = String.format("Mod %s (modid %s) has an Update available!\nOld Version was <b>%s</b>, new Version is <b>%s</b>", mod.modName(), mod.modid(), mod.version(), newVersion);
        if (website != null)
            s += "\nWebsite: " + "<a href=\"" + website + "\">" + website + "</a>";
        if (!Strings.isNullOrEmpty(changelog))
            s += "\n\n\nChangelog:\n" + changelog;
        return s;
    }

    private void updateAll() {
        List<Mod> failedUpdates = new ArrayList<>();
        for (UpdateContext ctx : updateMap.keySet()) {
            if (ctx.isDownloaded())
                continue;
            if (!UpdateUtil.updateMod(ctx))
                failedUpdates.add(ctx.linkedModContainer.mod);
        }
        StringBuilder failedMods = new StringBuilder();
        failedUpdates.forEach(mod -> failedMods.append(String.format("\nCould not update mod %s (modid %s)", mod.modName(), mod.modid())));
        JOptionPane.showMessageDialog(dialog, "The following mods failed to update:" + failedMods.toString() + "\nYou have to update these manuel\nThe other mods will be updated during restart");
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int index = ((JList) e.getSource()).getSelectedIndex();
        updateRightComponent(updateList.get(index));
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            UpdateUtil.openWebsite(event.getURL());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("update")) {
            if (UpdateUtil.updateMod(activeUpdateContext)) {
                JOptionPane.showMessageDialog(dialog, "Update successful! It will be applied at the next startup!");
                updateMod.setEnabled(false);
                updateAll.setEnabled(this.updateMap.keySet().stream().anyMatch(context -> context.getPathToRemoteJar() != null && context.getPathToRemoteModinfo() != null && !context.isDownloaded()));
            } else {
                JOptionPane.showMessageDialog(dialog, "Could not update mod. You have to try manuel");
            }
        } else if (event.getActionCommand().equals("visit")) {
            UpdateUtil.openWebsite(activeUpdateContext.getWebsite());
        } else
            throw new RuntimeException("Invalid actionCommand " + event.getActionCommand());
    }
}
