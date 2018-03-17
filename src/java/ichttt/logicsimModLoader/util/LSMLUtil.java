package ichttt.logicsimModLoader.util;

import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.loader.Loader;
import logicsim.App;

import javax.annotation.Nullable;
import javax.swing.*;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Some util functions
 * @since 0.0.1
 */
public class LSMLUtil {
    /**
     * Closes a stream without producing any noise
     * @param closeable The stream you wish to close
     * @since 0.0.1
     */
    public static void closeSilent(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // NO-OP
            }
        }
    }

    /**
     * Tries to get the mod class from the current active Thread.
     * <br><b>This code is unsafe and may fail!</b>
     * It will get the earliest Class found.
     * The output should not be trusted!
     * @return The first Class in the Stacktrace annotated with {@link ichttt.logicsimModLoader.api.Mod} or null if not found
     * @since 0.0.1
     */
    public static Mod getActiveModFromCurrentThread() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste : stackTraceElements) {
            Annotation annotation;
            try {
                annotation = getModAnnotationForClass(ste.getClassName());
            } catch (ClassNotFoundException e) {
                LSMLLog.error("Could not get Class for name %s, this shouldn't happen!", ste.getClassName());
                return null;
            }
            if (annotation != null) {
                return (Mod) annotation;
            }
        }
        return null;
    }

    /**
     * Determines if a method is called from mod code.
     * <br><b>This code is unsafe and may fail!</b>
     * Only use this for logging purposes
     * @return true if called from a mod
     * @since 0.1.4
     */
    public static boolean isCalledFromModCode() {
        return getActiveModFromCurrentThread() != null && !LogicSimModLoader.isInDev();
    }

    /**
     * Returns the mod annotation for a given class name
     * @param className The name of the class annotated with {@link Mod}
     * @return The mod annotation or null if not present
     * @throws ClassNotFoundException If the class could not be found
     * @since 0.0.1
     */
    public static Mod getModAnnotationForClass(String className) throws ClassNotFoundException {
        Class cls;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            ClassLoader modClsLoader = Loader.getInstance().getModClassLoader();
            if (modClsLoader != null)
                cls = Class.forName(className, true, modClsLoader);
            else
                throw e;
        }
        return getModAnnotationForClass(cls);
    }

    /**
     * Returns the mod annotation for a given class name
     * @param clazz The class annotated with {@link Mod}
     * @return The mod annotation or null if not present
     * @since 0.0.1
     */
    public static Mod getModAnnotationForClass(Class clazz) {
        Annotation annotation = clazz.getAnnotation(Mod.class);
        if (annotation != null) {
            return (Mod) annotation;
        }
        return null;
    }

    /**
     * Shows a {@link javax.swing.JOptionPane} on the main window if the main window is constructed
     * @since 0.0.2
     */
    public static void showMessageDialogOnWindowIfAvailable(String message) {
        App app = LogicSimModLoader.getApp();
        if (app != null)
            JOptionPane.showMessageDialog(app.frame, message);
        else
            JOptionPane.showMessageDialog(null, message);
    }

    /**
     * Shows a {@link javax.swing.JOptionPane} on the main window if the main window is constructed
     * @since 0.0.2
     */
    public static void showMessageDialogOnWindowIfAvailable(String message, String title, int messageType) {
        App app = LogicSimModLoader.getApp();
        if (app != null)
            JOptionPane.showMessageDialog(app.frame, message, title, messageType);
        else
            JOptionPane.showMessageDialog(null, message, title, messageType);
    }

    public static void unzipZipFile(File zipFile, File outputFolder) {
        byte[] buffer = new byte[4096];
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                String fileName = entry.getName();
                File newFile = new File(outputFolder + "/" + fileName);
                if (fileName.endsWith("/")) {
                    entry = zis.getNextEntry();
                    continue;
                }
                FileOutputStream output = null;
                //noinspection ResultOfMethodCallIgnored
                (new File(newFile.getAbsolutePath()).getParentFile()).mkdirs();
                try {
                    output = new FileOutputStream(newFile);
                    int read;
                    while ((read = zis.read(buffer)) > 0) {
                        output.write(buffer, 0, read);
                    }
                } finally {
                    LSMLUtil.closeSilent(output);
                }
                entry = zis.getNextEntry();
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            LSMLUtil.closeSilent(zis);
        }
    }

    /**
     * Inverses a complete Array
     * @param input The array to be inverted
     * @return The reversed array
     * @since 0.0.1
     */
    public static <T> T[] inverseObjectArray(T[] input) {
        for(int i = 0; i < input.length / 2; i++) {
            T temp = input[i];
            input[i] = input[input.length - i - 1];
            input[input.length - i - 1] = temp;
        }
        return input;
    }
}
