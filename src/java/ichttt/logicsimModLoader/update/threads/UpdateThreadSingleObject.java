package ichttt.logicsimModLoader.update.threads;

import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.api.IUpdateListener;
import ichttt.logicsimModLoader.update.GUIUpdateNotification;
import ichttt.logicsimModLoader.update.UpdateContext;
import ichttt.logicsimModLoader.update.UpdateUtil;

/**
 * Thread for updating a single mod. Avoids lockup on the GUI thread.
 */
public class UpdateThreadSingleObject implements Runnable {
    private final VersionBase newVersion;
    private final UpdateContext ctx;
    private final GUIUpdateNotification notification;

    public UpdateThreadSingleObject(UpdateContext ctx, VersionBase newVersion, GUIUpdateNotification notification) {
        this.ctx = ctx;
        this.newVersion = newVersion;
        this.notification = notification;
    }

    @Override
    public void run() {
        IUpdateListener.UpdateListenerWrapper.onUpdateDownloadPre(ctx.getUpdateListener(), false);
        notification.callbackSingleUpdateState(ctx, UpdateUtil.updateMod(ctx, newVersion));
    }
}
