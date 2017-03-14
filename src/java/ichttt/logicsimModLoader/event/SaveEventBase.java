package ichttt.logicsimModLoader.event;

import java.io.File;

/**
 * This contains all save-file related hooks.
 * If you want to save custom stuff, you should take a look at {@link ichttt.logicsimModLoader.api.ISaveHandler}
 * @since 0.0.2
 */
public abstract class SaveEventBase {
    public final File saveFile;

    /**
     * @param saveFile The file in action
     * @since 0.0.2
     */
    public SaveEventBase(File saveFile) {
        this.saveFile = saveFile;
    }

    /**
     * Called when a file has been loaded.
     * @since 0.0.2
     */
    public static class LoadEvent extends SaveEventBase {

        public LoadEvent(File saveFile) {
            super(saveFile);
        }
    }

    /**
     * Called when a file has been loaded.
     * @since 0.0.2
     */
    public static class SaveEvent extends SaveEventBase {

        public SaveEvent(File saveFile) {
            super(saveFile);
        }
    }
}
