package ichttt.logicsimModLoader.update;

import com.google.common.base.Strings;
import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.update.threads.UpdateThreadMultiObjects;
import ichttt.logicsimModLoader.update.threads.UpdateThreadSingleObject;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * The new UpdateNotification window introduced in 0.2.1
 * @since 0.2.1
 */
public class GUIUpdateNotification implements ListSelectionListener, HyperlinkListener, ActionListener {
    private static final String NEWLINE_HTML = "<br>";

    private final Map<UpdateContext, VersionBase> updateMap;
    private final Map<UpdateContext, String> textCache = new HashMap<>();
    private final List<UpdateContext> updateList = new ArrayList<>();
    private final JEditorPane editorPane = new JEditorPane();
    private final JScrollPane rightScrollPanel;
    private final JDialog dialog, updateDiag;
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
        updateAll = new JButton(LogicSimModLoader.translate("updateAll"));
        updateAll.setToolTipText(LogicSimModLoader.translate("updateAllTooltip"));
        updateAll.addActionListener(event -> constructAndRunUpdateThread(new UpdateThreadMultiObjects(updateMap, this)));
        updateAll.setEnabled(this.updateMap.keySet().stream().anyMatch(UpdateContext::downloadAvailable));
        leftPanel.add(updateAll, lLayout);

        rLayout.weighty = 0.03;
        rLayout.weightx = 0.5;
        rLayout.gridx = 1;
        rLayout.gridy = 1;
        rLayout.anchor = GridBagConstraints.CENTER;
        rLayout.fill = GridBagConstraints.BOTH;
        updateMod = new JButton(LogicSimModLoader.translate("updateMod"));
        updateMod.setActionCommand("update");
        updateMod.addActionListener(this);
        rightOuterPanel.add(updateMod, rLayout);

        rLayout.gridx = 2;
        rLayout.gridy = 1;
        rLayout.weighty = 0.03;
        rLayout.weightx = 0.5;
        visitURL = new JButton(LogicSimModLoader.translate("visitWebsite"));
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
        centerComponent(dialog);

        updateDiag = new JDialog(parent);
        updateDiag.setTitle(LogicSimModLoader.translate("waitUpdating"));
        updateDiag.setMinimumSize(new Dimension((int) Math.round(dialog.getPreferredSize().width *0.25), (int) Math.round(dialog.getPreferredSize().height *0.2)));
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        updateDiag.add(bar);
        updateDiag.pack();
        updateDiag.setModal(true);
        updateDiag.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        centerComponent(updateDiag);

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                updateDiag.dispose();
            }
        });

        SwingUtilities.invokeLater(() -> dialog.setVisible(true));

    }

    private void updateRightComponent(UpdateContext context) {
        activeUpdateContext = context;
        URL website = context.getWebsite();
        String s = textCache.get(context);
        if (s == null) {
            VersionBase newVersion = updateMap.get(context);
            Mod mod = context.linkedModContainer.mod;
            URL changelogURL = context.getChangelogURL();
            String changelog = null;
            if (changelogURL != null) {
                try {
                    changelog = NetworkHelper.readURLUncached(changelogURL);
                } catch (IOException e) {
                    LSMLLog.log(String.format("Could not read changelog for mod %s with modid %s", mod.modName(), mod.modid()), Level.WARNING, e);
                }
            }
            s = buildText(mod, newVersion, changelog, website).replaceAll("\n", NEWLINE_HTML);
            textCache.put(context, s);
        }
        editorPane.setText(s);
        visitURL.setEnabled(website != null);
        updateMod.setEnabled(context.downloadAvailable());

        SwingUtilities.invokeLater(() -> rightScrollPanel.getVerticalScrollBar().setValue(0));
    }

    private static String buildText(Mod mod, VersionBase newVersion, @Nullable String changelog, @Nullable URL website) {
        String s = String.format(LogicSimModLoader.translate("updateString"), mod.modName(), mod.modid(), mod.version(), newVersion);
        if (website != null)
            s += "\nWebsite: " + "<a href=\"" + website + "\">" + website + "</a>";
        if (!Strings.isNullOrEmpty(changelog))
            s += "\n\n\n" + LogicSimModLoader.translate("changelog") + ":\n" + changelog;
        return s;
    }

    private static void centerComponent(Window comp) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        comp.setLocation((screenSize.width - comp.getWidth())/ 2 , (screenSize.height - comp.getHeight())/2);
    }

    private void constructAndRunUpdateThread(Runnable runnable) {
        SwingUtilities.invokeLater(() -> updateDiag.setVisible(true));
        Thread thread = new Thread(runnable);
        thread.setName("Update thread");
        thread.start();
    }

    @Override
    public void valueChanged(ListSelectionEvent event) {
        int index = ((JList) event.getSource()).getSelectedIndex();
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
            UpdateThreadSingleObject threadSingleObject = new UpdateThreadSingleObject(activeUpdateContext, updateMap.get(activeUpdateContext), this);
            constructAndRunUpdateThread(threadSingleObject);
        } else if (event.getActionCommand().equals("visit"))
            UpdateUtil.openWebsite(activeUpdateContext.getWebsite());
        else
            throw new RuntimeException("Invalid actionCommand " + event.getActionCommand());
    }

    private void callbackBaseTasks(UpdateContext ctx) {
        SwingUtilities.invokeLater(() -> updateDiag.setVisible(false));
        updateAll.setEnabled(this.updateMap.keySet().stream().anyMatch(UpdateContext::downloadAvailable));
        updateRightComponent(ctx);
    }

    public void callbackSingleUpdateState(UpdateContext ctx, boolean success) {
        callbackBaseTasks(ctx);
        Mod mod = ctx.linkedModContainer.mod;
        String text = success ? LogicSimModLoader.translate("updateSuccess") : LogicSimModLoader.translate("updateFail");
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, String.format(text, mod.modName(), mod.modid())));
    }

    public void callbackMultiUpdateState(String notification) {
        callbackBaseTasks(activeUpdateContext);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, notification));
    }
}
