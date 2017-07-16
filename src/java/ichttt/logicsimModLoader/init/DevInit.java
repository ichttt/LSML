package ichttt.logicsimModLoader.init;

import ichttt.logicsimModLoader.loader.LSMLClassLoader;
import ichttt.logicsimModLoader.loader.Loader;

/**
 * Starts LSML with our classloader and sets the dev flag + add mods via args
 */
public class DevInit {

    /**
     * Should be launch target for mods, but nothing else
     * @param args Your mod path
     */
    public static void main(String[] args) throws Exception {
        LSMLClassLoader.init();
        for (String s : args)
            Loader.getInstance().addMod(Class.forName(s));
        LogicSimModLoader.isDev = true;
        LogicSimModLoader.init(null);
    }
}
