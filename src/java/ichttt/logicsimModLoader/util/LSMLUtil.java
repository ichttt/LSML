package ichttt.logicsimModLoader.util;

import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.LSMLLog;
import logicsim.App;

import javax.annotation.Nullable;
import javax.swing.*;
import java.io.Closeable;
import java.lang.annotation.Annotation;

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
        stackTraceElements = inverseObjectArray(stackTraceElements);
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
     * Returns the mod annotation for a given class name
     * @param className The name of the class annotated with {@link Mod}
     * @return The mod annotation or null if not present
     * @throws ClassNotFoundException If the class could not be found
     * @since 0.0.1
     */
    public static Mod getModAnnotationForClass(String className) throws ClassNotFoundException {
        return getModAnnotationForClass(Class.forName(className));
    }

    /**
     * Returns the mod annotation for a given class name
     * @param clazz The class annotated with {@link Mod}
     * @return The mod annotation or null if not present
     * @since 0.0.1
     */
    public static Mod getModAnnotationForClass(Class clazz){
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

    /**
     * Inverses a complete Array
     * @param input The array to be inverted
     * @return The reversed array
     * @since 0.0.1
     */
    public static <T> T[] inverseObjectArray(T[] input) {
        for(int i = 0; i < input.length / 2; i++)
        {
            T temp = input[i];
            input[i] = input[input.length - i - 1];
            input[input.length - i - 1] = temp;
        }
        return input;
    }
}
