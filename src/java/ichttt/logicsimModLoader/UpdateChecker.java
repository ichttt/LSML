package ichttt.logicsimModLoader;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Basic update checker.
 * This does not download the mod, it will only show that a update is available
 * A good place for this metadata is a github gist
 * @since 0.1.0
 */
public class UpdateChecker implements Runnable {
    private static Map<ModContainer, VersionBase> modWithFoundUpdate = new HashMap<>();
    private static Map<ModContainer, URL> toCheck = new HashMap<>();
    private static boolean isRunning = false;
    private static boolean isFirstRun = true;

    public static void register(ModContainer yourMod, URL updateURL) {
        toCheck.put(yourMod, updateURL);
    }

    /**
     * DO NOT CALL FROM MOD CODE
     * @since 0.1.0
     */
    @Override
    public void run() {
        isRunning = true;
        for (Map.Entry<ModContainer, URL> entry : toCheck.entrySet()) {
            ModContainer yourMod = entry.getKey();
            URL updateURL = entry.getValue();

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
                while ((read = bf.readLine()) != null) {
                    if (read.startsWith("#")) continue;
                    if (modid == null) modid = read;
                    else if (version == null) version = read;
                    else throw new RuntimeException("Got too much data!");
                }
                if (Strings.isNullOrEmpty(modid) || !modid.equalsIgnoreCase(yourMod.mod.modid())) {
                    LSMLLog.warning("Could not verify modid for updateURL" + updateURL);
                    return;
                }
                if (Strings.isNullOrEmpty(version)) {
                    LSMLLog.warning("Could not parse version for updateURL" + updateURL);
                    return;
                }
                VersionBase modUpdateVersion = new VersionBase(version.trim());
                if (!modUpdateVersion.getVersionString().equals(yourMod.VERSION.getVersionString()) && modUpdateVersion.isMinimum(yourMod.VERSION)) {
                    LSMLLog.info("Found update for mod %s: Installed version is %s, while current version is %s", yourMod.mod.modName(), yourMod.VERSION.getVersionString(), modUpdateVersion.getVersionString());
                    modWithFoundUpdate.put(yourMod, modUpdateVersion);
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
            StringBuilder builder = new StringBuilder();
            builder.append("Some mods have updates available!");
            modWithFoundUpdate.forEach((modContainer, versionBase) -> builder.append(String.format("\nMod %s: Installed version is %s, available version is %s",
                    modContainer.mod.modName(), modContainer.VERSION.getVersionString(), versionBase.getVersionString())));
            LSMLUtil.showMessageDialogOnWindowIfAvailable(builder.toString());
        }
        isRunning = false;
    }

    public static boolean isRunning() {
        return isRunning;
    }
}
