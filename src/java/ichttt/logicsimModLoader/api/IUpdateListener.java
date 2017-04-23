package ichttt.logicsimModLoader.api;

import ichttt.logicsimModLoader.VersionBase;

import java.io.IOException;

/**
 * Called by the {@link ichttt.logicsimModLoader.update.UpdateChecker}
 * Use this for e.g analytics or further update steps (e.g updating libs)
 * @since 0.2.2
 */
public interface IUpdateListener {
    /**
     * Called when the UpdateChecker found an update. This may be called at startup or when the user presses
     * the "update now" button.
     */
    default void onUpdateAvailable() {}

    /**
     * Called when the download is about to begin.
     * @param forAllMods If the update all button has been pressed
     */
    default void onUpdateDownloadPre(boolean forAllMods) {}

    /**
     * Called when download is complete.
     * If you use {@link ichttt.logicsimModLoader.update.UpdateContext#enableAutoUpdate(boolean)}, this is the phase
     * where you should do your stuff.
     * @throws IOException If you do updating yourself and something bad occurs
     */
    default void onUpdateDownloadPost(VersionBase newVersion) throws IOException {}
}
