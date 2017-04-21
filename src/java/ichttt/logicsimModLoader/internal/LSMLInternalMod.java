package ichttt.logicsimModLoader.internal;

import com.google.common.eventbus.Subscribe;
import ichttt.logicsimModLoader.update.UpdateChecker;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.config.ConfigCategory;
import ichttt.logicsimModLoader.config.entry.BooleanConfigEntry;
import ichttt.logicsimModLoader.event.loading.LSMLPreInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent;
import ichttt.logicsimModLoader.gui.IModGuiInterface;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.loader.Loader;
import ichttt.logicsimModLoader.update.UpdateContext;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Inter mod required for some LSML components.
 * Do not depend on any of these methods, they may change without a warning
 */
@Mod(modid = LSMLInternalMod.MODID, modName = "LogicSimModLoader", version = LogicSimModLoader.LSML_VERSION_STRING, author = "Tobias Hotz")
public class LSMLInternalMod implements ActionListener, IModGuiInterface {
    public static final String MODID = "LSML";
    private static Config config;
    private static JPanel panel;
    private static BooleanConfigEntry warnOnSave, checkForUpdates;
    private static JCheckBox warnOnSaveBox, checkForUpdatesBox;

    @Subscribe
    public void register(LSMLRegistrationEvent event) {
        event.registerModGui(LSMLUtil.getModAnnotationForClass(LSMLInternalMod.class), this);
        try {
            event.checkForUpdate(new UpdateContext(Loader.getInstance().getModContainerForModID(MODID), new URL("https://raw.githubusercontent.com/ichttt/LSML/master/LSMLUpdate.txt")).
                    withChangelogURL(new URL("https://raw.githubusercontent.com/ichttt/LSML/master/changes.txt")).
                    withWebsite(new URL("https://github.com/ichttt/LSML/releases/latest")));
        } catch (MalformedURLException e) {
            LSMLLog.log("Error registering UpdateChecker. How?", Level.SEVERE, e);
        }
    }

    @Subscribe
    public void onPreInit(LSMLPreInitEvent event) {
        config = new Config(Loader.getInstance().getModContainerForModID(MODID));
        ConfigCategory category = new ConfigCategory("General");

        warnOnSave = new BooleanConfigEntry("warnOnLoad", true, "Warn on load if mod-saved data could not be loaded");
        category.addEntry(warnOnSave);

        List<String> lines = new ArrayList<>();
        lines.add("Enable a lightweight update checker.");
        lines.add("If an update is found, the user will be notified, but nothing will be downloaded.");
        checkForUpdates = new BooleanConfigEntry("checkForUpdates", true, lines);
        category.addEntry(checkForUpdates);

        config.addCategory(category);
        config.load();
        config.save();
    }

    @Override
    public void setup() {
        panel = new JPanel(new GridLayout(0,1));
        panel.add(new JLabel(LogicSimModLoader.translate("LSMLSettings")));

        warnOnSaveBox = new JCheckBox(LogicSimModLoader.translate("warnOnLoad"));
        warnOnSaveBox.setSelected(warnOnSave.getValue());
        warnOnSaveBox.addActionListener(this);
        panel.add(warnOnSaveBox);

        checkForUpdatesBox = new JCheckBox(LogicSimModLoader.translate("updateChecker"));
        checkForUpdatesBox.setSelected(checkForUpdates.value);
        checkForUpdatesBox.addActionListener(this);
        panel.add(checkForUpdatesBox);

        JButton searchUpdatesNow = new JButton(LogicSimModLoader.translate("searchNow") + "!");
        searchUpdatesNow.setActionCommand("searchNow");
        searchUpdatesNow.addActionListener(this);
        panel.add(searchUpdatesNow);
    }

    @Nonnull
    @Override
    public JPanel draw() {
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equalsIgnoreCase("searchNow")) {
            if (!UpdateChecker.isRunning()) {
                Thread UPDATE_THREAD = new Thread(new UpdateChecker());
                UPDATE_THREAD.setName("UpdateCheckerThread");
                UPDATE_THREAD.setDaemon(true);
                UPDATE_THREAD.start();
            }
            return;
        }
        warnOnSave.value = warnOnSaveBox.isSelected();
        checkForUpdates.value = checkForUpdatesBox.isSelected();
        config.save();
    }

    public static boolean checkForUpdates() {
        return checkForUpdates.value;
    }

    public static boolean warnOnSave() {
        return warnOnSave.value;
    }
}
