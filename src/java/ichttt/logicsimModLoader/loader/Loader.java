package ichttt.logicsimModLoader.loader;

import com.google.common.base.Strings;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.event.LSMLEventBus;
import ichttt.logicsimModLoader.exceptions.ModException;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Loads the jarfiles from the libs and mods folder and parses them
 * @since 0.0.1
 */
public class Loader {
    private static class IgnoreCaseSorter implements Comparator<File> {
        @Override
        public int compare(File file1, File file2) {
            return file1 != null && file2 != null ? file1.getName().compareToIgnoreCase(file2.getName()) : file1 == null ? -1 : 1;
        }
    }

    private static Loader loader;
    @Nonnull
    public static Loader getInstance() {
        if (loader == null) {
            loader = new Loader();
        }
        return loader;
    }

    @Nonnull
    public final File modPath;
    @Nonnull
    public final Path basePath;
    @Nonnull
    public final File configPath;
    @Nonnull
    public final File libPath;
    private List<ModContainer> mods = new ArrayList<ModContainer>();

    /**
     * Gets all mods
     * @return A copied list of mods
     * @since 0.0.1
     */
    public List<ModContainer> getMods() {
        return new ArrayList<>(mods);
    }

    /**
     * Return the ModContainer for a mod
     * @param modid The modid of the mod you want to find
     * @return The {@link ModContainer} or null if not found
     * @since 0.0.1
     */
    @Nullable
    public ModContainer getModContainerForModID(String modid) {
        return mods.stream().
                filter(modContainer -> modContainer.mod.modid().equals(modid)).
                findAny().
                orElse(null);
    }

    private Loader() {
        basePath = Paths.get(".").toAbsolutePath().normalize();
        configPath = new File(basePath + "/config");
        modPath = new File(basePath + "/mods");
        libPath = new File(basePath + "/libs");
        createDirsIfNotExist(modPath, "Successfully create mods folder", "Could not create mod path!");
        createDirsIfNotExist(configPath, "Successfully create config folder", "Could not create config path!");
        createDirsIfNotExist(libPath, null, null);

        //Load libs. We do this here because something else may need it
        File[] libs = libPath.listFiles();
        if (libs == null) {
            LSMLLog.error("Could not load libs - libPath.listFiles returned null!");
            throw new RuntimeException("Could not load libs - libPath.listFiles returned null!");
        }
        Arrays.stream(libs).forEach(Loader::addURL); //Add the libs to classpath
    }

    /**
     * Internal use only
     * @since 0.0.1
     */
    public void searchMods() {
        Mod mod = LSMLUtil.getActiveModFromCurrentThread();
        if (mod != null && !LogicSimModLoader.isInDev())
            throw new ModException(mod, "Mod called search mods. THIS IS NOT ALLOWED!");
        else
            LSMLLog.fine("Loading mods...");
        File[] files = modPath.listFiles();
        if (files == null) {
            LSMLLog.error("Could not load mods - modPath.listFiles returned null!");
            throw new RuntimeException("Could not load mods - modPath.listFiles returned null!");
        }
        Arrays.sort(files, new IgnoreCaseSorter());
        List<File> modFiles = new ArrayList<>();
        for (File possibleMod : files) {
            if (!possibleMod.isFile() || !possibleMod.getName().endsWith(".jar")) {
                if (!possibleMod.getName().endsWith(".modinfo"))
                    LSMLLog.info("Found non-Jarfile %s. It will be ignored", possibleMod);
            } else {
                LSMLLog.fine("Found possible mod %s.", possibleMod);
                modFiles.add(possibleMod);
            }
        }

        LSMLLog.info("Successfully injected %s mods.", modFiles.size());
        for (File modFile : modFiles) {
            try {
                String pathToInstance;
                try {
                    pathToInstance = ModDataReader.parseModInfo(modFile, modFile.toString().substring(0, modFile.toString().length() - 4));
                } catch (FileNotFoundException e) {
                    LSMLLog.info("No ModInfo for file %s found - ignoring", modFile);
                    continue;
                }

                if (!addURL(modFile)) { //Add to classpath
                    LSMLLog.warning("Skipping mod %s as it failed to inject into classpath!", modFile);
                }

                Class<?> modClass = Class.forName(pathToInstance);
                Mod currentMod = LSMLUtil.getModAnnotationForClass(modClass);
                if (currentMod == null) {
                    LSMLLog.warning("Could not find Mod annotation for %s, skipping!", modFile);
                    continue;
                }

                if (getModContainerForModID((currentMod).modid()) != null) {
                    LSMLLog.warning("Found duplicate modid %s, skipping!", (currentMod).modid());
                }
                // Check the modid
                doModChecks(currentMod);
                ModContainer container = new ModContainer(currentMod);
                //Register mod to the EventBus
                try {
                    register(modClass);
                } catch (Exception e) {
                    LSMLLog.warning("Error registering mod %s to the EventBus! No events will be fired for this mod!\nException:%s", container.mod.modid(), e);
                }
                mods.add(container);
            } catch (Throwable e) {
                throw new RuntimeException("Error loading mod file " + modFile, e);
            }
        }
    }

    private void register(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        LSMLEventBus.EVENT_BUS.register(clazz.newInstance());
    }

    /**
     * Use this to add your mods in a dev environment or adding in API mods
     * @param clazz The class implementing @Mod
     * @param mod The modContainer your mod will use.
     * @since 0.0.1
     */
    public void addMod(Class clazz, ModContainer mod) {
        LSMLLog.info("Manually adding mod " + mod.mod.modid());
        doModChecks(mod.mod);
        try {
            register(clazz);
        } catch (Exception e) {
            throw new ModException(mod.mod, "Error calling Registration Event for mod " +  mod.mod.modid(), e);
        }
        mods.add(mod);
    }

    /**
     * Use this to add your mods in a dev environment or adding in API mods
     * @param clazz The class implementing @Mod
     * @since 0.0.1
     */
    public void addMod(Class clazz) throws IllegalAccessException, InstantiationException {
        addMod(clazz, new ModContainer((Mod) clazz.getAnnotation(Mod.class)));
    }

    private static void createDirsIfNotExist(File path,@Nullable String successMessage,@Nullable String failMessage) {
        if (!path.exists()) {
            if (path.mkdirs()) {
                if (successMessage != null)
                    LSMLLog.fine(successMessage);
            }
            else {
                if (failMessage != null) {
                    LSMLLog.error(failMessage);
                    throw new RuntimeException(failMessage);
                }
            }
        }
    }

    private static boolean addURL(File file) {
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class); //THX people over at stackoverflow
            method.setAccessible(true);
            method.invoke(ClassLoader.getSystemClassLoader(), file.toURI().toURL()); // Hack the system Classloader
            LSMLLog.fine("Added file %s to the classpath", file);
            return true;
        }
        catch (Throwable e) {
            LSMLLog.error("Could not add file %s to the classloader. Exception caught: %s\nThis file will be skipped", file, e);
            return false;
        }
    }

    private void doModChecks(Mod mod) {
        if (Strings.isNullOrEmpty(mod.modid()))
            throw new RuntimeException("Modid cannot be empty!");
        for (ModContainer container : mods) {
            if (container.mod.modid().equals(mod.modid()))
                throw new RuntimeException("Found duplicate modid" + mod.modid());
        }
    }
}
