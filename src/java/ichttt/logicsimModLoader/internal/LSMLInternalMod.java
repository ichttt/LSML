package ichttt.logicsimModLoader.internal;

import com.google.common.eventbus.Subscribe;
import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.api.IUpdateListener;
import ichttt.logicsimModLoader.api.InjectContainer;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.config.ConfigCategory;
import ichttt.logicsimModLoader.config.entry.BooleanConfigEntry;
import ichttt.logicsimModLoader.event.loading.LSMLPreInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent;
import ichttt.logicsimModLoader.exceptions.ModException;
import ichttt.logicsimModLoader.gui.IModGuiInterface;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.loader.Loader;
import ichttt.logicsimModLoader.update.UpdateChecker;
import ichttt.logicsimModLoader.update.UpdateContext;
import ichttt.logicsimModLoader.util.LSMLUtil;
import ichttt.logicsimModLoader.util.NetworkHelper;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Inter mod required for some LSML components.
 * Do not depend on any of these methods, they may change without a warning
 */
@Mod(modid = LSMLInternalMod.MODID, modName = "LogicSimModLoader", version = LogicSimModLoader.LSML_VERSION_STRING, author = "Tobias Hotz")
public class LSMLInternalMod implements ActionListener, IModGuiInterface, IUpdateListener {
    public static final String MODID = "LSML";
    private static Config config;
    private static JPanel panel;
    private static BooleanConfigEntry warnOnSave, checkForUpdates;
    private static JCheckBox warnOnSaveBox, checkForUpdatesBox;
    @InjectContainer
    private static ModContainer container;

    //Cause j7 does not not have default interfaces
    @Override
    public void onUpdateAvailable() {}
    @Override
    public void onUpdateDownloadPre(boolean forAllMods) {}

    @Override
    public void onUpdateDownloadPost(VersionBase newVersion) throws IOException {
        //We have to retrieve our URL dynamically
        URL downloadURl = NetworkHelper.getLatestURLFromGithubAPI(new URL("https://api.github.com/repos/ichttt/LSML/releases/latest"));
        Loader loader = Loader.getInstance();
        Loader.createDirsIfNotExist(loader.tempPath, null, "Could not create temp path!");
        File file = new File(loader.tempPath + "/LSMLUPDATE.zip");
        File supposedLSMLFile = new File(loader.tempPath + "/LSMLUPDATE/LSML.jar");
        try {
            NetworkHelper.readFileFromURL(downloadURl, file);
            LSMLUtil.unzipZipFile(file, new File(loader.tempPath + "/LSMLUPDATE"));
            if (!supposedLSMLFile.exists())
                throw new IOException("Could not find File!");
            String newFileName = "/LSMLv" + newVersion + ".jar";
            Files.copy(supposedLSMLFile.toPath(), new File(loader.basePath.toString() + newFileName).toPath());
            JOptionPane.showMessageDialog(null, String.format(LogicSimModLoader.translate("LSMLUpdateHint"), newFileName.substring(1)));
        } finally { //Cleanup the created files
            if (!file.delete())
                LSMLLog.fine("Could not clean up " + file.getName());
            Path directory = Paths.get(loader.tempPath + "/LSMLUPDATE");
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() { //http://stackoverflow.com/questions/779519/delete-directories-recursively-in-java/27917071#27917071
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Subscribe
    public void register(LSMLRegistrationEvent event) {
        event.registerModGui(LSMLUtil.getModAnnotationForClass(LSMLInternalMod.class), this);
        try {
            UpdateContext context = new UpdateContext(container, new URL("https://raw.githubusercontent.com/ichttt/LSML/master/LSMLUpdate.txt")).
                    withChangelogURL(new URL("https://raw.githubusercontent.com/ichttt/LSML/master/changes.txt")).
                    withWebsite(new URL("https://github.com/ichttt/LSML/releases/latest")).
                    registerUpdateListener(this);
            try {
                context.enableCertificateValidation().enableAutoUpdate(true);
            } catch (ModException e) {
                LSMLLog.warning("Could not enable certificate validation! Disabling auto-update for LSML as a security precaution.");
            }
            event.checkForUpdate(context);
        } catch (IOException e) {
            LSMLLog.log("Error registering UpdateChecker.", Level.SEVERE, e);
        }
    }

    @Subscribe
    public void onPreInit(LSMLPreInitEvent event) {
        config = new Config(container);
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
