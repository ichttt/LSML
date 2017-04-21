package ichttt.logicsimModLoader.api;

import ichttt.logicsimModLoader.VersionBase;

import java.io.IOException;

/**
 * Called by the {@link ichttt.logicsimModLoader.update.UpdateChecker}
 */
public interface IUpdateListener {
    default void onUpdateAvailable() {}

    default void onUpdateDownloadPre(boolean forAllMods) {}

    /**
     * Called when download is complete.
     * If you use {@link ichttt.logicsimModLoader.update.UpdateContext#enableAutoUpdate(boolean)}, this is the phase
     * where you should do your stuff.
     * @throws IOException If you do updating yourself and something bad occurs
     */
    default void onUpdateDownloadPost(VersionBase newVersion) throws IOException {}
}
