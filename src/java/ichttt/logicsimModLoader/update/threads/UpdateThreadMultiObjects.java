package ichttt.logicsimModLoader.update.threads;

import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.update.GUIUpdateNotification;
import ichttt.logicsimModLoader.update.UpdateContext;
import ichttt.logicsimModLoader.update.UpdateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Thread to update a single mod. Avoids lockup on the GUI thread.
 */
public class UpdateThreadMultiObjects implements Runnable {
    private final Map<UpdateContext, VersionBase> updateMap;
    private final GUIUpdateNotification notification;

    public UpdateThreadMultiObjects(Map<UpdateContext, VersionBase> updateMap, GUIUpdateNotification notification) {
        this.updateMap = updateMap;
        this.notification = notification;
    }

    @Override
    public void run() {
        List<Mod> failedUpdates = new ArrayList<>();
        for (Map.Entry<UpdateContext, VersionBase> entry : updateMap.entrySet()) {
            UpdateContext ctx = entry.getKey();
            if (ctx.isDownloaded())
                continue;
            ctx.getUpdateListener().onUpdateDownloadPre(true);
            if (!UpdateUtil.updateMod(ctx, entry.getValue()))
                failedUpdates.add(ctx.linkedModContainer.mod);
        }
        String s;
        if (failedUpdates.isEmpty()) {
            s = LogicSimModLoader.translate("updateSuccess");
        } else {
            StringBuilder failedMods = new StringBuilder();
            failedUpdates.forEach(mod -> failedMods.append(String.format("\n" + LogicSimModLoader.translate("updateFail"), mod.modName(), mod.modid())));
            s = String.format(LogicSimModLoader.translate("updateFailedMods"), failedMods.toString());
        }
        notification.callbackMultiUpdateState(s);
    }
}
