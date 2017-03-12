package ichttt.logicsimModLoader.loader;

import com.google.common.base.Strings;
import com.sun.istack.internal.Nullable;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.exceptions.ModException;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.annotation.Nonnull;
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
    private List<ModContainer> mods = new ArrayList<ModContainer>();

    /**
     * Gets all mods
     * @return A copied list of mods
     * @since 0.0.1
     */
    public List<ModContainer> getMods() {
        return new ArrayList<>(mods);
    }

    private static void createDirsIfNotExist(File path, String successMessage, String failMessage) {
        if (!path.exists()) {
            if (path.mkdirs()) {
                LSMLLog.fine(successMessage);
            }
            else {
                LSMLLog.error(failMessage);
                throw new RuntimeException(failMessage);
            }
        }
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
        createDirsIfNotExist(modPath, "Successfully create mods folder", "Could not create mod path!");
        createDirsIfNotExist(configPath, "Successfully create config folder", "Could not create config path!");
    }

    /**
     * Internal use only
     * @since 0.0.1
     */
    public void searchMods() {
        Mod mod = LSMLUtil.getActiveModFromCurrentThread();
        if (mod != null)
            throw new ModException(mod, "Mod called search mods. THIS IS NOT ALLOWED!");
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
                    LSMLLog.info("Ignoring non-Jarfile %s. It will be ignored", possibleMod);
            } else {
                LSMLLog.fine("Found possible mod %s. Injection into classpath", possibleMod);
                try {
                    addURL(possibleMod);
                }
                catch (Exception e) {
                    LSMLLog.error("Could not add mod %s to the ClassLoader! Exception caught: %s\nThis mod will be skipped", possibleMod, e);
                }
                modFiles.add(possibleMod);
            }
        }
        LSMLLog.info("Successfully injected %s mods.", modFiles.size());
        for (File modFile : modFiles) {
            try {
                String pathToInstance;
                try {
                    pathToInstance = ModDataReader.parseAnnotationInfo(modFile.toString().substring(0, modFile.toString().length() - 4));
                } catch (FileNotFoundException e) {
                    LSMLLog.info("No ModInfo for file %s found - ignoring", modFile);
                    continue;
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
        LogicSimModLoader.LSML_EVENT_BUS.register(clazz.newInstance());
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

    private static void addURL(File file) throws Exception {
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class); //THX people over at stackoverflow
        method.setAccessible(true);
        method.invoke(ClassLoader.getSystemClassLoader(), file.toURI().toURL()); // Hack the system Classloader
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
