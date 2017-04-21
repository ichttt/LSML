package ichttt.logicsimModLoader.update;

import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.loader.Loader;
import ichttt.logicsimModLoader.loader.ModDataReader;
import ichttt.logicsimModLoader.util.NetworkHelper;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Various utils needed for the new {@link UpdateChecker}
 */
public class UpdateUtil {
    public static boolean updateMod(UpdateContext ctx, VersionBase newVersion) {
        ModContainer container = ctx.linkedModContainer;
        if (ctx.noDownloading()) {
            try {
                ctx.getUpdateListener().onUpdateDownloadPost(newVersion);
            } catch (Exception e) {
                return false;
            }
            ctx.setDownloaded();
            return true;
        }

        if (ctx.getPathToRemoteModinfo() == null || ctx.getPathToRemoteJar() == null || container.modinfoFile == null || container.jarFile == null)
            return false;
        File tempPath = Loader.getInstance().tempPath;
        Loader.createDirsIfNotExist(tempPath, null, null);
        File outputModinfo = new File(tempPath + "/" + container.modinfoFile.getName());
        File outputJar = new File(tempPath  + "/" + container.jarFile.getName());
        if (outputJar.exists()) {
            if (!outputJar.delete()) {
                LSMLLog.info("Could not update mod %s. Reason: Jar delete failed", container.mod.modid());
                return false;
            }
        }
        if (outputModinfo.exists()) {
            if (!outputModinfo.delete()) {
                LSMLLog.info("Could not update mod %s. Reason: Modinfo delete failed", container.mod.modid());
                return false;
            }
        }
        try {
            NetworkHelper.readFileFromURL(ctx.getPathToRemoteJar(), outputJar);
            NetworkHelper.readFileFromURL(ctx.getPathToRemoteModinfo(), outputModinfo);
            ModDataReader.parseModInfo(outputJar, outputModinfo); //try parsing the modinfo for a first test. If this fails, we abort the update
        } catch (Exception e) {
            LSMLLog.log("Could not update mod " + container.mod.modid(), Level.INFO, e);
            if (!outputJar.delete())
                LSMLLog.fine("Could not cleanup jar file!");
            if (!outputModinfo.delete())
                LSMLLog.fine("Could not cleanup modinfo file!");
            return false;
        }
        ctx.setDownloaded();
        try {
            ctx.getUpdateListener().onUpdateDownloadPost(newVersion);
        } catch (IOException e) {
            LSMLLog.fine("onUpdateDownloadPost failed, but everything is downloaded - Continue as normal!");
        }
        return true;
    }

    public static void openWebsite(URL website) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(website.toURI());
            } catch (URISyntaxException | IOException | UnsupportedOperationException e) {
                LSMLLog.log("Could not open link!", Level.WARNING, e);
            }
        }
        else {
            LSMLLog.warning("Could not open link - java.awt.Desktop is not supported!");
        }
    }
}
