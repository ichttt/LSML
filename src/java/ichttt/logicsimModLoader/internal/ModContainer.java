package ichttt.logicsimModLoader.internal;

import ichttt.logicsimModLoader.VersionBase;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.config.Config;
import ichttt.logicsimModLoader.loader.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

/**
 * Wrapper for {@link Mod}
 * @since 0.0.1
 */
public class ModContainer {
    @Nonnull
    public final Mod mod;
    @Nonnull
    public final VersionBase VERSION;
    @Nullable
    public final File jarFile, modinfoFile;

    public ModContainer(Mod mod) {
        this(mod, null, null);
    }

    public ModContainer(Mod mod, @Nullable File jarFile, @Nullable File modinfoFile) {
        this.mod = mod;
        this.VERSION = new VersionBase(mod.version());
        this.jarFile = jarFile;
        this.modinfoFile = modinfoFile;
    }

    /**
     * Gets a file in the config dir based on your modid. Use this for {@link Config}
     * @return Your config file
     * @since 0.0.1
     */
    @Nonnull
    public File getSuggestedConfigFile() {
        return new File(Loader.getInstance().configPath + "/" + mod.modid() + ".cfg");
    }

}
