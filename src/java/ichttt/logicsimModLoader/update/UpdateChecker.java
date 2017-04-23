package ichttt.logicsimModLoader.update;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.exceptions.InvalidVersionStringException;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Basic update checker.
 * This does not download the mod, it will only show that a update is available
 * A good place for this metadata is a github raw
 * @since 0.2.1 (Was before {@link ichttt.logicsimModLoader.UpdateChecker}
 */
public class UpdateChecker implements Runnable {
    private static Map<UpdateContext, VersionBase> modWithFoundUpdate = new HashMap<>();
    private static List<UpdateContext> toCheck = new ArrayList<>();
    private static boolean isRunning = false;
    private static boolean isFirstRun = true;

    @Deprecated
    public static void register(ModContainer yourMod, URL updateURL) {
        register(new UpdateContext(yourMod, updateURL));
    }

    public static void register(UpdateContext context) {
        toCheck.add(context);
    }

    /**
     * DO NOT CALL FROM MOD CODE
     * @since 0.1.0
     */
    @Override
    public void run() {
        isRunning = true;
        for (UpdateContext context : toCheck) {
            ModContainer yourMod = context.linkedModContainer;
            URL updateURL = context.updateURL;

            Preconditions.checkNotNull(yourMod);
            Preconditions.checkNotNull(updateURL);
            BufferedReader bf = null;
            try {
                URLConnection connection = updateURL.openConnection();
                connection.setUseCaches(false); // so the file is always up-to-date
                bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String read;
                String modid = null;
                String version = null;
                boolean doContinue = false;
                while ((read = bf.readLine()) != null) {
                    if (read.startsWith("#")) continue;
                    if (modid == null)
                        modid = read;
                    else if (version == null)
                        version = read;
                    else {
                        LSMLLog.error("Got too much data!");
                        doContinue = true;
                        break;
                    }
                }
                if (doContinue)
                    continue;

                if (Strings.isNullOrEmpty(modid) || !modid.equalsIgnoreCase(yourMod.mod.modid())) {
                    LSMLLog.warning("Could not verify modid for updateURL" + updateURL);
                    return;
                }
                if (Strings.isNullOrEmpty(version)) {
                    LSMLLog.warning("Could not parse version for updateURL" + updateURL);
                    return;
                }
                VersionBase modUpdateVersion = new VersionBase(version.trim());
                if (modUpdateVersion.compareTo(yourMod.VERSION) > 0) {
                    LSMLLog.info("Found update for mod %s: Installed version is %s, while current version is %s", yourMod.mod.modName(), yourMod.VERSION.getVersionString(), modUpdateVersion.getVersionString());
                    context.getUpdateListener().onUpdateAvailable();
                    modWithFoundUpdate.put(context, modUpdateVersion);
                } else
                    LSMLLog.fine("No update found for mod %s: Installed version is %s, while current version is %s", yourMod.mod.modName(), yourMod.VERSION.getVersionString(), modUpdateVersion.getVersionString());
            } catch (IOException e) {
                LSMLLog.warning("Could not check mod %s for updates", yourMod.mod.modName());
            } catch (InvalidVersionStringException e) {
                LSMLLog.log(String.format("Mod update file %s has invalid version!", yourMod.mod.modName()), Level.WARNING, e);
            } finally {
                LSMLUtil.closeSilent(bf);
            }
        }
        SwingUtilities.invokeLater(UpdateChecker::printUpdateNotification);
    }

    /**
     * Do not call from mod code!
     */
    private static void printUpdateNotification() {
        if (modWithFoundUpdate.isEmpty()) {
            if (isFirstRun)
                isFirstRun = false;
            else
                LSMLUtil.showMessageDialogOnWindowIfAvailable("No updates found");
        }
        else {
            GUIUpdateNotification notification = new GUIUpdateNotification(modWithFoundUpdate);
        }
        isRunning = false;
    }

    public static boolean isRunning() {
        return isRunning;
    }
}
