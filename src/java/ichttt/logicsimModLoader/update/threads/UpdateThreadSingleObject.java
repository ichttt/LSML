package ichttt.logicsimModLoader.update.threads;

import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.update.GUIUpdateNotification;
import ichttt.logicsimModLoader.update.UpdateContext;
import ichttt.logicsimModLoader.update.UpdateUtil;

/**
 * Created by Tobias on 22.04.2017.
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
        notification.callbackSingleUpdateState(ctx, UpdateUtil.updateMod(ctx, newVersion));
    }
}
