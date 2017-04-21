package ichttt.logicsimModLoader.event.loading;

import com.google.common.base.Preconditions;
import ichttt.logicsimModLoader.update.UpdateChecker;
import ichttt.logicsimModLoader.api.ISaveHandler;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.config.entry.IConfigEntryParser;
import ichttt.logicsimModLoader.gui.ModListGui;
import ichttt.logicsimModLoader.gui.IModGuiInterface;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.internal.SaveHandler;
import ichttt.logicsimModLoader.loader.Loader;
import ichttt.logicsimModLoader.update.UpdateContext;
import ichttt.logicsimModLoader.util.LSMLUtil;

import java.net.URL;

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

    @Deprecated
    public void registerSaveHandler(ISaveHandler handler) {
        Mod mod = LSMLUtil.getActiveModFromCurrentThread();
        Preconditions.checkNotNull(mod, "Failed to get ModContainer!");
        SaveHandler.registerSaveHandler(mod, handler);
    }

    public void registerSaveHandler(Mod yourMod, ISaveHandler handler) {
        SaveHandler.registerSaveHandler(yourMod, handler);
    }

    /**
     * @since 0.0.4
     */
    @Deprecated
    public void checkForUpdate(URL updateURL) {
        Mod mod = LSMLUtil.getActiveModFromCurrentThread();
        Preconditions.checkNotNull(mod, "Failed to get ModContainer!");
        checkForUpdate(Loader.getInstance().getModContainerForModID(mod.modid()), updateURL);
    }

    /**
     * @since 0.0.4
     * @deprecated Use {@link #checkForUpdate(UpdateContext)}
     */
    @Deprecated
    public void checkForUpdate(ModContainer yourMod, URL updateURL) {
        Preconditions.checkNotNull(yourMod);
        Preconditions.checkNotNull(updateURL);
        UpdateChecker.register(yourMod, updateURL);
    }

    public void checkForUpdate(UpdateContext context) {
        UpdateChecker.register(context);
    }
}
