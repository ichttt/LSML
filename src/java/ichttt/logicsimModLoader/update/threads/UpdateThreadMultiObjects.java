package ichttt.logicsimModLoader.update.threads;

import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.update.GUIUpdateNotification;
import ichttt.logicsimModLoader.update.UpdateContext;
import ichttt.logicsimModLoader.update.UpdateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Tobias on 23.04.2017.
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
            s= "Update successful! It will be applied at the next startup!";
        } else {
            StringBuilder failedMods = new StringBuilder();
            failedUpdates.forEach(mod -> failedMods.append(String.format("\nCould not update mod %s (modid %s)", mod.modName(), mod.modid())));
            s = "The following mods failed to update:" + failedMods.toString() + "\nYou have to update these manuel\nThe other mods will be updated during restart";
        }
        notification.callbackMultiUpdateState(s);
    }
}
