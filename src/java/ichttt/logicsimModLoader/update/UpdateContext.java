package ichttt.logicsimModLoader.update;

import com.google.common.base.Preconditions;
import ichttt.logicsimModLoader.api.IUpdateListener;
import ichttt.logicsimModLoader.internal.ModContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;

/**
 * An UpdateContext holds all the information required for the {@link UpdateChecker}
 * @since 0.2.1
 */
public class UpdateContext implements Comparable<UpdateContext> {
    public final ModContainer linkedModContainer;
    public final URL updateURL;
    private URL website, changelogURL, pathToRemoteJar, pathToRemoteModinfo;
    private boolean isFinished, isDownloaded, noDownload;
    private IUpdateListener updateListener = new DummyUpdateListener();

    public UpdateContext(ModContainer container, URL updateURL) {
        this.linkedModContainer = container;
        this.updateURL = updateURL;
    }

    private void checkNull(Object obj, String message) {
        Preconditions.checkArgument(obj == null, message);
    }

    @Nonnull
    public UpdateContext withWebsite(URL downloadURL) {
        checkNull(this.website, "DownloadURL is already set!");
        this.website = downloadURL;
        return this;
    }

    @Nonnull
    public UpdateContext withChangelogURL(URL changelogURL) {
        checkNull(this.changelogURL, "ChangelogURL is already set!");
        this.changelogURL = changelogURL;
        return this;
    }

    @Nonnull
    public UpdateContext enableAutoUpdate(URL pathToRemoteJar, URL pathToRemoteModinfo) {
        checkNull(this.pathToRemoteJar, "Path to remote JAR is already set!");
        this.pathToRemoteJar = pathToRemoteJar;
        this.pathToRemoteModinfo = pathToRemoteModinfo;
        return this;
    }

    /**
     * Most mods shouldn't use this. <b>Only use this if you know what you are doing.</b>
     * <br>LSML needs this because it isn't a mod.
     * @param takeCareAboutDownloadYourself True to ensure that you take care about the button press yourself
     */
    public UpdateContext enableAutoUpdate(boolean takeCareAboutDownloadYourself) {
        checkNull(this.pathToRemoteJar, "Path to remote JAR is already set!");
        this.noDownload = takeCareAboutDownloadYourself;
        return this;
    }

    /**
     * @since 0.2.2
     */
    @Nonnull
    public UpdateContext registerUpdateListener(IUpdateListener listener) {
        this.updateListener = listener;
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

    /**
     * INTERNAL API - DO NOT CALL FROM MOD CODE
     */
    public void setDownloaded() {
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

    @Override
    public int compareTo(UpdateContext o) {
        return this.linkedModContainer.mod.modName().compareToIgnoreCase(o.linkedModContainer.mod.modName());
    }

    private static final class DummyUpdateListener implements IUpdateListener {}
}
