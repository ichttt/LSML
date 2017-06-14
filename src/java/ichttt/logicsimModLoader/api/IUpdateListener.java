package ichttt.logicsimModLoader.api;

import ichttt.logicsimModLoader.VersionBase;

import java.io.IOException;

/**
 * Called by the {@link ichttt.logicsimModLoader.update.UpdateChecker}
 * Use this for e.g analytics or further update steps (e.g updating libs)
 * <b>WARNING</b> since version 0.3, interfaces can't be default anymore.
 * @since 0.2.2
 */
public interface IUpdateListener {
    /**
     * Called when the UpdateChecker found an update. This may be called at startup or when the user presses
     * the "update now" button.
     */
    void onUpdateAvailable();

    /**
     * Called when the download is about to begin.
     * @param forAllMods If the update all button has been pressed
     */
    void onUpdateDownloadPre(boolean forAllMods);

    /**
     * Called when download is complete.
     * If you use {@link ichttt.logicsimModLoader.update.UpdateContext#enableAutoUpdate(boolean)}, this is the phase
     * where you should do your stuff.
     * @throws IOException If you do updating yourself and something bad occurs
     */
    void onUpdateDownloadPost(VersionBase newVersion) throws IOException;


    /**
     * Wrapper for the time when we had default interfaces...
     * @since 0.3.0
     */
    class UpdateListenerWrapper {
        public static void onUpdateAvailable(IUpdateListener listener) {
            try {
                listener.onUpdateAvailable();
            } catch (LinkageError e) {
                //ignore this, byproduct of default interfaces
            }
        }

        public static void onUpdateDownloadPre(IUpdateListener listener, boolean forAllMods) {
            try {
                listener.onUpdateDownloadPre(forAllMods);
            } catch (LinkageError e) {
                //ignore this, byproduct of default interfaces
            }
        }

        public static void onUpdateDownloadPost(IUpdateListener listener, VersionBase newVersion) throws IOException {
            try {
                listener.onUpdateDownloadPost(newVersion);
            } catch (LinkageError e) {
                //ignore this, byproduct of default interfaces
            }
        }
    }
}
