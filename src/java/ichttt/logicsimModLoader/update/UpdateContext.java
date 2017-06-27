package ichttt.logicsimModLoader.update;

import com.google.common.base.Preconditions;
import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.api.IUpdateListener;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.exceptions.ModException;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;

/**
 * An UpdateContext holds all the information required for the {@link UpdateChecker}
 * @since 0.2.1
 */
public class UpdateContext implements Comparable<UpdateContext> {
    public final ModContainer linkedModContainer;
    public final URL updateURL;
    private URL website, changelogURL, pathToRemoteJar, pathToRemoteModinfo;
    private boolean isFinished, isDownloaded, noDownload;
    private IUpdateListener updateListener = DUMMY_INSTANCE;
    private Certificate[] certificates;

    /**
     * Creates the basic context for the new updateChecker
     * @param container Your mod container
     * @param updateURL The URL where your information for updating is found. Lines starting with # will be ignored, the first without a '#' <b>must</b> the first line be the modid and the next your up-to-date version
     */
    public UpdateContext(ModContainer container, URL updateURL) {
        this.linkedModContainer = container;
        this.updateURL = updateURL;
    }

    private void checkNull(Object obj, String message) {
        Preconditions.checkArgument(obj == null, message);
    }

    /**
     * Provides your homepage, so users can visit it with a single click
     * @param website The URL to your homepage.
     */
    @Nonnull
    public UpdateContext withWebsite(URL website) {
        checkNull(this.website, "DownloadURL is already set!");
        this.website = website;
        return this;
    }

    /**
     * Provides a changelog for your mod. You can use html commands in this file.
     * @param changelogURL The URL to a copy of your up-to-date changelog
     */
    @Nonnull
    public UpdateContext withChangelogURL(URL changelogURL) {
        checkNull(this.changelogURL, "ChangelogURL is already set!");
        this.changelogURL = changelogURL;
        return this;
    }

    /**
     * Enables the auto-download feature on your mod. You have to do nothing but register it here and update the remote
     * files
     * @param pathToRemoteJar The URL where your updated mod jar is found
     * @param pathToRemoteModinfo The URL where your updated modinfo is found
     */
    @Nonnull
    public UpdateContext enableAutoUpdate(URL pathToRemoteJar, URL pathToRemoteModinfo) {
        checkNull(this.pathToRemoteJar, "Path to remote JAR is already set!");
        this.pathToRemoteJar = pathToRemoteJar;
        this.pathToRemoteModinfo = pathToRemoteModinfo;
        return this;
    }

    /**
     * Most mods shouldn't use this. <b>Only use this if you know what you are doing.</b>.LSML needs this because it isn't a mod.
     * <br>If you set this, you will be excluded from the automatic downloader, but the user can still download and the
     * events on the {@link IUpdateListener} will still be called.
     * You have to download and update yourself when {@link IUpdateListener#onUpdateDownloadPost(VersionBase)} is called
     * @param takeCareAboutDownloadYourself True to ensure that you take care about the button press yourself
     */
    public UpdateContext enableAutoUpdate(boolean takeCareAboutDownloadYourself) {
        Mod activeMod = LSMLUtil.getActiveModFromCurrentThread();
        if (activeMod == null)
            LSMLLog.warning("Unknown mod registered auto update with manuel download.");
        else if (!activeMod.modid().equals("LSML"))
            LSMLLog.fine("Mod %s (modid%s) registered auto update with manuel download. This is not recommended", activeMod.modName(), activeMod.modid());
        checkNull(this.pathToRemoteJar, "Path to remote JAR is already set!");
        this.noDownload = takeCareAboutDownloadYourself;
        return this;
    }

    /**
     * Registers an UpdateListener as specified in {@link IUpdateListener}
     * @since 0.2.2
     */
    @Nonnull
    public UpdateContext registerUpdateListener(IUpdateListener listener) {
        Preconditions.checkArgument(this.updateListener == DUMMY_INSTANCE, "UpdateListener is already set!");
        this.updateListener = listener;
        return this;
    }

    /**
     * Enables verification of the downloaded file using the class certificate.
     * This is only necessary for enable auto update
     * <br><b>THIS REQUIRES YOUR JAR TO BE SIGNED AND WILL DISABLE UPDATING IF NO CERTIFICATE IS FOUND!</b>
     * <br>If the downloaded jar does not match this fingerprint, the update will be rolled back.
     * @since 0.3.1
     */
    @Nonnull
    public UpdateContext enableCertificateValidation() throws ModException {
        Certificate[] certificates = linkedModContainer.getClass().getProtectionDomain().getCodeSource().getCertificates();
        if (certificates == null)
            throw new ModException(linkedModContainer.mod, "Could not find certificates!");
        this.certificates = certificates;
        return this;
    }

    @Nullable
    public URL getWebsite() {
        return website;
    }

    @Nullable
    public URL getChangelogURL() {
        return changelogURL;
    }

    @Nullable
    public URL getPathToRemoteJar() {
        return pathToRemoteJar;
    }

    public boolean downloadAvailable() {
        return ((this.pathToRemoteJar != null && this.pathToRemoteModinfo != null) || this.noDownload)&& !this.isDownloaded;
    }

    void setDownloaded() {
        Preconditions.checkNotNull(this.website);
        this.isDownloaded = true;
    }

    @Nullable
    public IUpdateListener getUpdateListener() {
        return updateListener;
    }

    public boolean noDownloading() {
        return noDownload;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    @Nullable
    public URL getPathToRemoteModinfo() {
        return pathToRemoteModinfo;
    }

    public Certificate[] getCertificates() {
        return certificates;
    }

    @Override
    public int compareTo(UpdateContext o) {
        return this.linkedModContainer.mod.modName().compareToIgnoreCase(o.linkedModContainer.mod.modName());
    }

    private static final IUpdateListener DUMMY_INSTANCE = new IUpdateListener(){
        @Override
        public void onUpdateAvailable() {
        }

        @Override
        public void onUpdateDownloadPre(boolean forAllMods) {
        }

        @Override
        public void onUpdateDownloadPost(VersionBase newVersion) throws IOException {
        }
    };
}
