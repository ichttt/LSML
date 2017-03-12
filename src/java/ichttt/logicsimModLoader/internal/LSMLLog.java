package ichttt.logicsimModLoader.internal;

import ichttt.logicsimModLoader.loader.Loader;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * Internal logging class
 * @since 0.0.1
 */
public class LSMLLog {
    private static final Logger logger = Logger.getLogger("LogicSimModLoader");
    private static boolean hasInit = false;

    public static void init() {
        if (hasInit) {
            LSMLLog.warning("Tried init the logger twice!");
            return;
        }
        logger.setLevel(Level.ALL);
        hasInit = true;
        Handler handler;
        String logPath = Loader.getInstance().basePath.toString() + "/log";
        if ((new File(logPath)).mkdirs()) {
            LSMLLog.error("Could not create log save dirs!");
            return;
        }
        try {
            handler = new FileHandler(logPath + "/LSMLLog.log");
        } catch (IOException e) {
            e.printStackTrace();
            LSMLLog.error("Could not start log file-saving!");
            return;
        }
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
    }

    public static void log(String s, Level level, Object... format) {
        logger.log(level, String.format(s, format));
    }

    public static void log(String s, Level level, Throwable throwable) {
        logger.log(level, s, throwable);
    }

    public static void fine(String s, Object... format) {
        log(s, Level.FINE, format);
    }

    public static void info(String s, Object... format) {
        log(s, Level.INFO, format);
    }

    public static void warning(String s ,Object... format) {
        log(s, Level.WARNING, format);
    }

    public static void error(String s, Object... format) {
        log(s, Level.SEVERE, format);
    }
}
