package ichttt.logicsimModLoader.loader;

import com.google.common.base.Strings;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.event.LSMLEventBus;
import ichttt.logicsimModLoader.exceptions.ModException;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.internal.ModContainer;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * Loads the jarfiles from the libs and mods folder and parses them
 * @since 0.0.1
 */
public class Loader {
    private static Loader loader;
    @Nonnull
    public static Loader getInstance() {
        if (loader == null) {
            loader = new Loader();
        }
        return loader;
    }

    @Nonnull
    public final Path basePath;
    @Nonnull
    public final File configPath, libPath, modPath, tempPath;
    private final ArrayList<ModContainer> mods = new ArrayList<ModContainer>();

    private Loader() {
        basePath = Paths.get(".").toAbsolutePath().normalize();
        configPath = new File(basePath + "/config");
        modPath = new File(basePath + "/mods");
        libPath = new File(basePath + "/libs");
        tempPath = new File(basePath + "/temp");
        createDirsIfNotExist(modPath, "Successfully create mods folder", "Could not create mod path!");
        createDirsIfNotExist(configPath, "Successfully create config folder", "Could not create config path!");
        createDirsIfNotExist(libPath, null, null);

        //Load libs. We do this here because something else may need it
        File[] libs = libPath.listFiles();
        LSMLLog.fine("Loading libs");
        if (libs == null) {
            LSMLLog.error("Could not load libs - libPath.listFiles returned null!");
            throw new RuntimeException("Could not load libs - libPath.listFiles returned null!");
        }
        for (File lib : libs) {
            addURL(lib); //Add the libs to classpath
        }
    }

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
        for (ModContainer container : mods) {
            if (container.mod.modid().equals(modid))
                return container;
        }
        return null;
    }

    public boolean hasMod(String modid) {
        return getModContainerForModID(modid) != null;
    }

    /**
     * Internal use only
     * @since 0.0.1
     */
    public void searchMods() {
        if (LSMLUtil.isCalledFromModCode()) {
            LSMLLog.error("Mod called search mods. THIS IS NOT ALLOWED!");
            return;
        }
        LSMLLog.fine("Loading mods...");
        File[] files = modPath.listFiles();
        if (files == null) {
            LSMLLog.error("Could not load mods - modPath.listFiles returned null!");
            throw new RuntimeException("Could not load mods - modPath.listFiles returned null!");
        }
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
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

        Iterator<File> modIterator = modFiles.iterator();
        while (modIterator.hasNext()){
            File modFile = modIterator.next();
            LSMLLog.fine("Processing file %s", modFile);
            try {
                String pathToInstance;
                try {
                    pathToInstance = ModDataReader.parseModInfo(modFile, modFile.toString().substring(0, modFile.toString().length() - 4));
                } catch (FileNotFoundException e) {
                    LSMLLog.info("No ModInfo for file %s found - ignoring", modFile);
                    modIterator.remove();
                    continue;
                }

                if (!addURL(modFile)) { //Add to classpath
                    LSMLLog.error("Skipping mod %s as it failed to inject into classpath!", modFile);
                    modIterator.remove();
                    continue;
                }

                Class<?> modClass = Class.forName(pathToInstance, true, LSMLClassLoader.INSTANCE);
                Mod currentMod = LSMLUtil.getModAnnotationForClass(modClass);
                if (currentMod == null) {
                    LSMLLog.warning("Could not find Mod annotation for %s, skipping!", modFile);
                    modIterator.remove();
                    continue;
                }
                LSMLLog.fine("Found Mod annotation");

                // Check the modid
                doModChecks(currentMod);
                ModContainer container = new ModContainer(currentMod, modFile, new File(modFile.toString().substring(0, modFile.toString().length() - 4) + ".modinfo"));
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
        mods.trimToSize();
        LSMLLog.info("Successfully injected %s mods.", modFiles.size());
    }

    private void register(Class<?> clazz) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        clazz = Class.forName(clazz.getName(), true, LSMLClassLoader.INSTANCE);
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
    public void addMod(Class clazz) {
        addMod(clazz, new ModContainer((Mod) clazz.getAnnotation(Mod.class)));
    }

    public static void createDirsIfNotExist(File path,@Nullable String successMessage,@Nullable String failMessage) {
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
            LSMLClassLoader.INSTANCE.addURL(file.toURI().toURL());
            LSMLLog.fine("Added file %s to the classpath", file);
            return true;
        }
        catch (Exception e) {
            LSMLLog.error("Could not add file %s to the classloader. Exception caught: %s\nThis file will be skipped", file, e);
            return false;
        }
    }

    private void doModChecks(Mod mod) {
        if (Strings.isNullOrEmpty(mod.modid()))
            throw new RuntimeException("Modid cannot be empty!");
        if (getModContainerForModID(mod.modid()) != null) {
            throw new RuntimeException("Found duplicate modid " + mod.modid());
        }
    }

    public void updatePendingMods() {
        if (!tempPath.exists())
            return;
        List<File> jarsToCopy = new ArrayList<>();
        for (File f : tempPath.listFiles()) {
            if (!f.isFile())
                continue;
            String filename = f.getName();
            File inModDir = new File(modPath + "/" +filename);
            if(filename.endsWith(".modinfo") && inModDir.exists()) {
                if (!inModDir.delete()) {
                    LSMLLog.log("Could not update mod " + f, Level.WARNING);
                    continue;
                }
                try {
                    Files.copy(f.toPath(), inModDir.toPath());
                } catch (IOException e) {
                    LSMLLog.log("Could not update mod " + f, Level.WARNING, e);
                    continue;
                }
                jarsToCopy.add(new File(tempPath + "/" + filename.substring(0,filename.length() - ".modinfo".length()) + ".jar"));
                if (!f.delete())
                    LSMLLog.fine("Failed to cleanup file " + f);
                else
                    LSMLLog.fine("Updated modinfo " + f);
            }
        }
        for (File f : jarsToCopy) {
            String filename = f.getName();
            File inModDir = new File(modPath + "/"+ filename);
            if (!inModDir.delete()) {
                LSMLLog.warning("Could not update mod " + f);
                continue;
            }
            try {
                Files.copy(f.toPath(), inModDir.toPath());
            } catch (IOException e) {
                LSMLLog.log("Could not update mod " + f, Level.WARNING, e);
                continue;
            }
            if (!f.delete())
                LSMLLog.info("Failed to cleanup file " + f);
            else
                LSMLLog.info("Updated jar " + f);
        }
    }
}
