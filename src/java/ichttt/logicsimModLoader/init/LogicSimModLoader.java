package ichttt.logicsimModLoader.init;

import com.google.common.eventbus.EventBus;
import ichttt.logicsimModLoader.ModState;
import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.event.loading.LSMLPostInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLPreInitEvent;
import ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent;
import ichttt.logicsimModLoader.internal.LSMLInternalMod;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.loader.Loader;
import logicsim.App;


/**
 * Basic Bootstrap class.
 */
public final class LogicSimModLoader {
    private static App app;
    public static final VersionBase LSML_VERSION = new VersionBase(0,0,1);
    public static final EventBus LSML_EVENT_BUS = new EventBus();
    private static boolean hasInit = false;

    /**
     * Starts off LSML and LogicSim itself
     */
    public static void main(String[] args) {
        coreInit();
        Loader loader = Loader.getInstance();
        //Init our mod first
        try {
            loader.addMod(LSMLInternalMod.class);
        }
        catch (Exception e) {
            LSMLLog.warning("Could not load internal LSMLMod!");
        }
        loader.searchMods();

        LSML_EVENT_BUS.post(new LSMLRegistrationEvent());
        LSML_EVENT_BUS.post(new LSMLPreInitEvent());
        ModContainer.doTransitionOnAllMods(ModState.PREINIT);
        app = new App(); // This starts LogicSim
        LSML_EVENT_BUS.post(new LSMLPostInitEvent());
        ModContainer.doTransitionOnAllMods(ModState.POSTINIT);
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
    }

    public static App getApp() {
        return app;
    }
}
