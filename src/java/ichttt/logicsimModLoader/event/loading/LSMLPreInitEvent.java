package ichttt.logicsimModLoader.event.loading;

import ichttt.logicsimModLoader.internal.LSMLLog;

import java.util.logging.Logger;

/**
 * Called immediate after {@link LSMLRegistrationEvent}
 * @since 0.0.1
 */
public class LSMLPreInitEvent {

    /**
     * Get your own logger you can use for your mod.
     * Your logs will be saved to the save log file as LSML's log files
     * @param modid Your modid
     * @return The logger you should use for logging
     * @since 0.1.5
     */
    public Logger getCustomLogger(String modid) {
        return LSMLLog.getCustomLogger(modid);
    }
}
