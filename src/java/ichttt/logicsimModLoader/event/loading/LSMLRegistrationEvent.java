package ichttt.logicsimModLoader.event.loading;

import com.google.common.base.Preconditions;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.config.entry.IConfigEntryParser;
import ichttt.logicsimModLoader.gui.ModListGui;
import ichttt.logicsimModLoader.gui.IModGuiInterface;
import ichttt.logicsimModLoader.util.LSMLUtil;

/**
 * Called before anything else
 * @since 0.0.1
 */
public class LSMLRegistrationEvent {
    /**
     * Register your own custom {@link IConfigEntryParser}.
     * Only register one time per class
     * @param parser Your custom parser
     * @since 0.0.1
     */
    public void registerConfigParser(IConfigEntryParser parser) {
        Config.registerCustomEntryParser(parser);
    }

    /**
     * @deprecated Use {@link #registerModGui(Mod, IModGuiInterface)} instead, as this is unsafe!
     * @param guiInterface Your guiInterface
     */
    @Deprecated
    public void registerModGui(IModGuiInterface guiInterface) {
        Mod mod = LSMLUtil.getActiveModFromCurrentThread();
        Preconditions.checkNotNull(mod, "Failed to get ModContainer!");
        ModListGui.registerModGui(mod, guiInterface);
    }

    /**
     * Registers your guiInterface to be shown in the mods list
     * @param yourMod Your mod. You may get it by parsing the class to {@link LSMLUtil#getModAnnotationForClass(Class)}
     * @param guiInterface Your guiInterface
     */
    public void registerModGui(Mod yourMod, IModGuiInterface guiInterface) {
        ModListGui.registerModGui(yourMod, guiInterface);
    }
}
