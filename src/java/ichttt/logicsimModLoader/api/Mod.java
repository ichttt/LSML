package ichttt.logicsimModLoader.api;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a Mod to the ModLoader.
 * The class implementing this <b>has to</b> be named in a file called [yourModJarName].modinfo.
 * @since 0.0.1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mod {
    /**
     * A unique String used by the system to build up an identity.
     * Other mods may depend on it, so you should not change it if you want to keep compat with older versions
     * @since 0.0.1
     */
    String modid();

    /**
     * The String your mod will be listed as.
     * You may change this at any time, as no one should depend on this String.
     * @since 0.0.1
     */
    String modName();

    /**
     * The current Version of your mod. This must be in this format: [MAJOR.MINOR.PATCH].
     * The ModLoder will convert this to a {@link ichttt.logicsimModLoader.VersionBase}, and mods can depend on it
     * @since 0.0.1
     */
    String version();

    /**
     * A short description what your mod is doing.
     * Currently unused //TODO
     * @since 0.0.1
     */
    String comment() default "";
}
