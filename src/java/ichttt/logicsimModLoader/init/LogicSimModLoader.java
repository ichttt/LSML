package ichttt.logicsimModLoader.init;

import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.event.LSMLEventBus;
import ichttt.logicsimModLoader.event.loading.LSMLPostInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLPreInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent;
import ichttt.logicsimModLoader.exceptions.MissingDependencyException;
import ichttt.logicsimModLoader.internal.LSMLInternalMod;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.SaveHandler;
import ichttt.logicsimModLoader.loader.Loader;
import ichttt.logicsimModLoader.update.UpdateChecker;
import ichttt.logicsimModLoader.util.I18nHelper;
import ichttt.logicsimModLoader.util.LSMLUtil;
import logicsim.App;
import logicsim.I18N;

import javax.annotation.Nullable;
import javax.swing.*;
import java.io.File;
import java.util.logging.Level;


/**
 * Basic Bootstrap class.
 */
public final class LogicSimModLoader implements Thread.UncaughtExceptionHandler {
    private static App app;
    public static final String LSML_VERSION_STRING = "0.2.0";
    public static final VersionBase LSML_VERSION = new VersionBase(LSML_VERSION_STRING);
    private static boolean hasInit = false;
    private static boolean isDev = false;
    private static I18nHelper i18n;

    /**
     * LSML INTERNAL - Use an instance of {@link I18nHelper} for your own code
     */
    public static String translate(String s) {
        return i18n == null ? s : i18n.translate(s);
    }

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
     * Don't start from here if you are in a dev environment, start from {@link #startFromDev()}
     */
    public static void main(@Nullable String[] args) {
        if (hasInit)
            return;
        LogicSimModLoader instance = new LogicSimModLoader();
        Thread.currentThread().setUncaughtExceptionHandler(instance); //Add exception handler for this thread
        boolean loadProgressBar = true;
        if (args != null) {
            if (args.length != 0 && args[0].equalsIgnoreCase("disableProgressBar")) {
                loadProgressBar = false;
            }
        }
        if (loadProgressBar)
            ProgressBarManager.init(); //init the progress bar
        else
            LSMLLog.info("ProgressBar is disabled");
        coreInit();
        Loader loader = Loader.getInstance(); //init loader to load libs
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            File tempDir = Loader.getInstance().tempPath;
            if (tempDir.isDirectory() && tempDir.listFiles().length == 0)
                if (!tempDir.delete())
                    LSMLLog.fine("Could not clean up temp dir");
        }));
        registerSubscriptions();
        i18n = new I18nHelper("lsml");

        //Init our mod first
        try {
            loader.addMod(LSMLInternalMod.class);
        }
        catch (Exception e) {
            LSMLLog.warning("Could not load internal LSMLMod!");
        }
        loader.updatePendingMods();

        ProgressBarManager.stepBar("Searching mods...");
        loader.searchMods();
        ProgressBarManager.stepBar("Registering mod hooks...");
        LSMLEventBus.EVENT_BUS.post(new LSMLRegistrationEvent());
        //Close registration window for CustomConfigLoaders
        Config.closeRegistrationWindow();
        ProgressBarManager.stepBar("Sending PreInit to mods...");
        LSMLEventBus.EVENT_BUS.post(new LSMLPreInitEvent());

        if (LSMLInternalMod.checkForUpdates()) { //Need to do this here so the config is loaded
            LSMLLog.info("Starting LSML update checker...");
            Thread UPDATE_THREAD = new Thread(new UpdateChecker());
            UPDATE_THREAD.setName("UpdateCheckerThread");
            UPDATE_THREAD.setDaemon(true);
            UPDATE_THREAD.start();
        } else
            LSMLLog.fine("Update checker is disabled");

        app = new App(); // This starts LogicSim
        ProgressBarManager.stepBar("Sending PostInit to mods...");
        LSMLEventBus.EVENT_BUS.post(new LSMLPostInitEvent());

        LSMLLog.fine("Finishing up...");
        //Close registration window for CustomSaves
        SaveHandler.closeRegistrationWindow();
        ProgressBarManager.destroyWindow();
        app.frame.setVisible(true);
        LSMLLog.fine("Done. LSML and LogicSim started successfully");
    }

    /**
     * If you run UnitTests, you should call this for consistency
     */
    public static void coreInit() {
        if (hasInit)
            return;
        hasInit = true;
        LSMLLog.init();
        new I18N(); //Create this as early as possible
        ConfigInit.init();
    }

    private static void registerSubscriptions() {
        LSMLEventBus.EVENT_BUS.register(new SaveHandler());
    }

    /**
     * Gets the app singleton from LogicSim
     * @return the singleton or null if not yet init
     * @since 0.0.1
     */
    @Nullable
    public static App getApp() {
        return app;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LSMLLog.log("----------REPORTING EXCEPTION THROWN----------", Level.SEVERE, e);
        if (e instanceof MissingDependencyException) {
            LSMLUtil.showMessageDialogOnWindowIfAvailable(translate("missingDeps") + "\n" + e.getMessage());
        } else {
            LSMLUtil.showMessageDialogOnWindowIfAvailable(translate("unexpectedError"), translate("exception"), JOptionPane.ERROR_MESSAGE);
        }
        System.exit(-1);
    }
}
