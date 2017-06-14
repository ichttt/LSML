package ichttt.logicsimModLoader.internal;

import com.google.common.eventbus.Subscribe;
import ichttt.logicsimModLoader.api.ISaveHandler;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.event.SaveEventBase;
import ichttt.logicsimModLoader.exceptions.ModException;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

/**
 * FILE STRUCTURE:
 * 1.ROW: FileHeader containing saveVersion
 * 2.ROW: Header containing all modids that registered saveHandlers
 * Next rows: modid + saved lines
 * @since 0.0.2
 */
public class SaveHandler {
    private static Map<String, ISaveHandler> saveHandlers = new HashMap<>();
    private static String mods;
    private static boolean registrationAllowed = true;
    public static final int CURRENT_REV = 1;
    public static final String REV_STRING = "LSMLREV" + CURRENT_REV;
    public static final String MOD_START = "modStart";
    public static final String MOD_END = "modEnd";
    private static final String SPLITTER = ",";

    private static void saveFile(File ourFile) throws IOException {
        if (!ourFile.exists() && !ourFile.createNewFile())
            throw new IOException(String.format("Could not create new file %s!", ourFile));
        //Create header
        BufferedWriter bf = new BufferedWriter(new FileWriter(ourFile));
        bf.write(REV_STRING);
        bf.newLine();
        bf.write(mods);
        bf.newLine();
        for (Map.Entry<String, ISaveHandler> handler : saveHandlers.entrySet()) {
            String mod = handler.getKey();
            try {
                bf.write(MOD_START + mod);
                bf.newLine();
                for (String line : handler.getValue().saveLines()) {
                    bf.write(line);
                    bf.newLine();
                }
                bf.write(MOD_END + mod);
                bf.newLine();
            } catch (IOException e) {
                LSMLLog.error("A critical error occurred while saving mod data");
                LSMLUtil.showMessageDialogOnWindowIfAvailable("Could not save mod specific data!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        LSMLUtil.closeSilent(bf);
    }

    private boolean loadFile(File ourFile) {
        BufferedReader br = null;
        boolean hasError = false;
        try {
            br = new BufferedReader(new FileReader(ourFile));
            br.readLine(); //Skip the header, we already read it.
            br.readLine();
            while (true) {
                String modStart = br.readLine();
                if (modStart == null)
                    break;
                if (!modStart.startsWith(MOD_START)) {
                    hasError = true;
                    LSMLLog.error("Error loading file %s: Corrupted mod header!", ourFile);
                    continue;
                }
                modStart = modStart.substring(MOD_START.length());
                ISaveHandler handler = saveHandlers.get(modStart);
                List<String> lines = new ArrayList<>();
                while (true) {
                    String line = br.readLine();
                    if (line.startsWith(MOD_END)) {
                        line = line.substring(MOD_END.length());
                        if (!line.equals(modStart)) {
                            LSMLLog.error("Error loading file %s: Corrupted mod header end!", ourFile);
                            hasError = true;
                        }
                        break;
                    }
                    lines.add(line);
                }
                handler.loadLines(lines);
            }
        } catch (IOException e) {
            LSMLLog.error("A critical error occurred while loading mod data");
            LSMLUtil.showMessageDialogOnWindowIfAvailable("Could not load mod specific data!", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            LSMLUtil.closeSilent(br);
        }
        return hasError;
    }

    private static boolean verifyFileHeader(File ourFile) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(ourFile));
            String line1 = br.readLine();
            String line2 = br.readLine();
            String[] modsSaved = line2.split(SPLITTER);
            if (!line1.equals(REV_STRING))
                return false;
            for (String mod : modsSaved) {
                if (!saveHandlers.containsKey(mod))
                    return false;
            }
            return true;
        } catch (IOException e) {
            LSMLLog.log("Error while reading file header!", Level.SEVERE, e);
            return false;
        } finally {
            LSMLUtil.closeSilent(br);
        }
    }

    @Subscribe
    public void onSave(SaveEventBase.SaveEvent event) {
        if (saveHandlers.isEmpty()) //NO-OP when nothing is registered
            return;
        try {
            File ourFile = new File(event.saveFile.getPath() + ".lsml");
            saveFile(ourFile);
        } catch (IOException e) {
            LSMLLog.log("A critical error occurred while saving mod data", Level.SEVERE, e);
            LSMLUtil.showMessageDialogOnWindowIfAvailable("Could not save mod specific data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Subscribe
    public void onLoad(SaveEventBase.LoadEvent event) {
        try {
            File ourFile = new File(event.saveFile.getPath() + ".lsml");
            if (ourFile.exists()) {
                LSMLLog.fine("Trying loading additional save data for file %s", event.saveFile);
                if (!verifyFileHeader(ourFile)) {
                    LSMLLog.warning("Could not verify header");
                    if (LSMLInternalMod.warnOnSave())
                        JOptionPane.showMessageDialog(LogicSimModLoader.getApp().frame, "The file you are trying to load has mod specific data saved." +
                            "If you want to be safe, exit the save without saving anything.\nA Backup of the mod data will be created, but if you modify anything, this might be of no use."
                            , "Missing mods", JOptionPane.WARNING_MESSAGE);
                    Files.copy(ourFile.toPath(), new File(ourFile.getPath() + ".bak").toPath());
                }
                if (saveHandlers.isEmpty()) //Only check header when nothing is registered
                    return;
                loadFile(ourFile);
            } else {
                LSMLLog.fine("No additional save data found for file %s", event.saveFile);
            }
        } catch (IOException e) {
            LSMLLog.log("A critical error occurred while loading mod data", Level.SEVERE, e);
            LSMLUtil.showMessageDialogOnWindowIfAvailable("Could not load mod specific data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * INTERNAL USE ONLY
     * @since 0.0.2
     */
    public static void closeRegistrationWindow() {
        if (!registrationAllowed) {
            LSMLLog.fine("Should close registration window but already closed!");
            return;
        }
        if (LSMLUtil.isCalledFromModCode())
            LSMLLog.error("A mod tried closing the registration window. THIS IS NOT ALLOWED!");
        registrationAllowed = false;
        if (!saveHandlers.isEmpty()) {
            StringBuilder writerList = new StringBuilder();
            for (String s : saveHandlers.keySet()) {
                writerList.append(s).append(SPLITTER);
            }
            String s = writerList.toString();
            mods = s.substring(0, s.length() - 1);
        }
    }

    /**
     * Register a custom save handler. Your will be able to attach your information to a save file.
     * @param mod Your mod
     * @param handler Your ISaveHandler
     */
    public static void registerSaveHandler(Mod mod, ISaveHandler handler) {
        if (!registrationAllowed)
            throw new ModException(mod, "A mod tried to register a save handler after the window has been closed. THIS IS NOT ALLOWED!");
        saveHandlers.put(mod.modid(), handler);
    }

}
