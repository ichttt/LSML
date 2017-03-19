package ichttt.logicsimModLoader.init;

import ichttt.logicsimModLoader.ModState;
import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.event.LSMLEventBus;
import ichttt.logicsimModLoader.event.loading.LSMLPostInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLPreInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent;
import ichttt.logicsimModLoader.exceptions.MissingDependencyException;
import ichttt.logicsimModLoader.internal.LSMLInternalMod;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.internal.SaveHandler;
import ichttt.logicsimModLoader.loader.Loader;
import ichttt.logicsimModLoader.util.LSMLUtil;
import logicsim.App;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.logging.Level;


/**
 * Basic Bootstrap class.
 */
public final class LogicSimModLoader implements Thread.UncaughtExceptionHandler {
    private static App app;
    public static final VersionBase LSML_VERSION = new VersionBase(0,0,3);
    private static boolean hasInit = false;
    private static boolean isDev = false;

    /**
     * Run this from your dev environment to test your mod
     * @since 0.0.1
     */
    public static void startFromDev() {
        if (hasInit)
            throw new RuntimeException("Only call this once!");
        isDev = true;
        main(null);
    }

    /**
     * Determines if the ModLoader is started in a dev environment. Useful if you have assertions that may fail at dev
     * @return true if started from an ide
     */
    public static boolean isInDev() {
        return isDev;
    }

    /**
     * Starts off LSML and LogicSim itself
     * Dont't start from here if you are in a dev environment, start from {@link #startFromDev()}
     */
    public static void main(@Nullable String[] args) {
        if (hasInit)
            return;
        Thread.setDefaultUncaughtExceptionHandler(new LogicSimModLoader());
        Loader loader = Loader.getInstance(); //init loader to load libs
        coreInit();
        //Init our mod first
        try {
            loader.addMod(LSMLInternalMod.class);
        }
        catch (Exception e) {
            LSMLLog.warning("Could not load internal LSMLMod!");
        }
        loader.searchMods();

        LSMLEventBus.EVENT_BUS.post(new LSMLRegistrationEvent());
        //Close registration window for CustomConfigLoaders
        Config.closeRegistrationWindow();
        LSMLEventBus.EVENT_BUS.post(new LSMLPreInitEvent());
        ModContainer.doTransitionOnAllMods(ModState.PREINIT);
        app = new App(); // This starts LogicSim
        LSMLEventBus.EVENT_BUS.post(new LSMLPostInitEvent());
        ModContainer.doTransitionOnAllMods(ModState.POSTINIT);
        //Close registration window for CustomSaves
        SaveHandler.closeRegistrationWindow();
    }

    /**
     * If you run UnitTests, you should call this for consistency
     */
    public static void coreInit() {
        if (hasInit)
            return;
        hasInit = true;
        LSMLLog.init();
        ConfigInit.init();
        registerSubscriptions();
    }

    private static void registerSubscriptions() {
        LSMLEventBus.EVENT_BUS.register(new SaveHandler());
    }

    /**
     * Gets the app singleton from LogicSim
     * @return the singleton or null if not yet init
     */
    @Nullable
    public static App getApp() {
        return app;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LSMLLog.log("----------REPORTING EXCEPTION THROWN----------", Level.SEVERE, e);
        if (e instanceof MissingDependencyException) {
            LSMLUtil.showMessageDialogOnWindowIfAvailable("Could not continue because some mods are missing dependencies\n" + e.getMessage());
        } else {
            LSMLUtil.showMessageDialogOnWindowIfAvailable("There was an unexpected error and LSML could not continue. Further information can be found in the log", "Exception in app", JOptionPane.ERROR_MESSAGE);
        }
        System.exit(-1);
    }
}
