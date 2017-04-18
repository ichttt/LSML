package ichttt.logicsimModLoader;

import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.loader.Loader;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * This class defines the possible states of mods.
 * @since 0.0.1
 * @deprecated All mods will have the same state at the same time (except for {@link #UNAVAILABLE}. Use {@link Loader#hasMod(String)}
 * TODO remove in 1.0
 */
@Deprecated
public enum ModState {
    UNAVAILABLE, FOUND, PREINIT, INIT, POSTINIT, LOADED;

    /**
     * Gets the state of a mod by the modid
     * @param modid The modid of the mod you want the state from
     * @return The current state of the mod
     * @deprecated Use {@link Loader#hasMod(String)}
     */
    @Deprecated
    @Nonnull
    public static ModState getStateForMod(String modid) {
        return getStateForMod(container -> container.mod.modid().equals(modid));
    }

    /**
     * Gets the state of a mod
     * @param mod The mod you want the state from
     * @return The current state of the mod
     * @deprecated All mods have the same state at the same time
     */
    @Deprecated
    @Nonnull
    public static ModState getStateForMod(Mod mod) {
        return getStateForMod(modContainer -> modContainer.mod.equals(mod));
    }

    /**
     * Gets the state of a mod
     * @param predicate The predicate that decides this modContainer is the modContainer you are searching for
     * @return The current state of the mod
     * @deprecated All mods have the same state at the same time
     */
    @Deprecated
    @Nonnull
    public static ModState getStateForMod(Predicate<? super ModContainer> predicate) {
        return Loader.getInstance().getMods().stream().
                filter(predicate).
                findAny().
                map(ModContainer::getState).
                orElse(UNAVAILABLE);
    }
}