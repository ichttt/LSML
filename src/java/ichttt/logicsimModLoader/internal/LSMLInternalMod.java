package ichttt.logicsimModLoader.internal;

import com.google.common.eventbus.Subscribe;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.config.ConfigCategory;
import ichttt.logicsimModLoader.config.entry.BooleanConfigEntry;
import ichttt.logicsimModLoader.event.loading.LSMLPreInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent;
import ichttt.logicsimModLoader.gui.IModGuiInterface;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.loader.Loader;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Inter mod required for some LSML components.
 * <br><b>Do not depend on this version, it will not get bumped</b>.
 * <br>Depend on {@link ichttt.logicsimModLoader.init.LogicSimModLoader#LSML_VERSION} instead.
 * Do not depend on any of these methods, they may change without a warning
 */
@Mod(modid = LSMLInternalMod.MODID, modName = "LogicSimModLoader", version = LogicSimModLoader.LSML_VERSION_STRING)
public class LSMLInternalMod implements IModGuiInterface {
    public static final String MODID = "LSML";
    private JList<String> mods;
    private DefaultListModel<String> modsList;
    private JPanel panel;

    @Subscribe
    public void register(LSMLRegistrationEvent event) {
        event.registerModGui(LSMLUtil.getModAnnotationForClass(LSMLInternalMod.class), this);
        try {
            event.checkForUpdate(Loader.getInstance().getModContainerForModID(MODID), new URL("https://raw.githubusercontent.com/ichttt/LSML/master/LSMLUpdate.txt"));
        } catch (MalformedURLException e) {
            LSMLLog.log("What just happened?", Level.SEVERE, e);
        }
    }

    @Subscribe
    public void onPreInit(LSMLPreInitEvent event) {
        @SuppressWarnings("ConstantConditions") Config config = new Config(Loader.getInstance().getModContainerForModID(MODID));
        ConfigCategory category = new ConfigCategory("General");
        category.addEntry(new BooleanConfigEntry("warnOnLoad", true, "Warn on load if mod-saved data could not be loaded"));
        config.addCategory(category);
        config.load();
        config.save();
    }

    @Override
    public void setup() {
        mods = new JList<>();
        modsList = new DefaultListModel<>();
        GridBagConstraints layout = new GridBagConstraints();
        for (ModContainer modContainer : Loader.getInstance().getMods())
            modsList.addElement(String.format("%s v.%s (modid %s)", modContainer.mod.modName(), modContainer.VERSION.getVersionString(), modContainer.mod.modid()));
        mods.setModel(modsList);
        panel = new JPanel(new GridBagLayout());
        layout.gridy = 1;
        layout.gridheight = 1;
        layout.gridx = 1;
        layout.weightx = 1.0;
        layout.weighty = 0.1;
        panel.add(new JLabel("Loaded mods:"), layout);
        GridBagConstraints layout2 = new GridBagConstraints();
        layout2.gridheight = 1;
        layout2.gridx = 1;
        layout2.weightx = 1.0;
        layout2.weighty = 0.9;
        layout2.gridheight = 2;
        layout2.gridy = 2;
        panel.add(mods, layout2);
    }

    @Nonnull
    @Override
    public JPanel draw() {
        return panel;
    }
}
