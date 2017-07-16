package ichttt.logicsimModLoader.init;

import ichttt.logicsimModLoader.loader.LSMLClassLoader;

import java.lang.reflect.Method;

/**
 * Start LSML with our classloader
 */
public class Init {
    public static void main(String args[]) throws Exception {
        LSMLClassLoader.init();
        Method method = Class.forName("ichttt.logicsimModLoader.init.LogicSimModLoader", true, LSMLClassLoader.INSTANCE).getDeclaredMethod("init", String[].class);
        method.invoke(null, (Object) args);
    }
}
