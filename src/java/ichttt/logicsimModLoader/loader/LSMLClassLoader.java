package ichttt.logicsimModLoader.loader;

import ichttt.logicsimModLoader.api.InjectContainer;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.ModContainerInjectionHandler;
import sun.misc.SharedSecrets;
import sun.misc.URLClassPath;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Class Loader for LSML to read {@link InjectContainer} annotations
 */
public class LSMLClassLoader extends URLClassLoader {
    public static final LSMLClassLoader INSTANCE;
    private final URLClassPath ucp = SharedSecrets.getJavaNetAccess().getURLClassPath(this);
    static {
        URLClassLoader ucl = (URLClassLoader) LogicSimModLoader.class.getClassLoader();
        INSTANCE = new LSMLClassLoader(ucl.getURLs());
        Thread.currentThread().setContextClassLoader(INSTANCE);
        try {
            Field systemClassLoader = ClassLoader.class.getDeclaredField("scl");
            systemClassLoader.setAccessible(true);
            systemClassLoader.set(null, INSTANCE);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public LSMLClassLoader(URL[] urls) {
        super(urls);
        ClassLoader.registerAsParallelCapable();
    }

    @Override
    protected void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = super.loadClass(name, resolve);
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) { //scan to inject instances
            InjectContainer injectionAnnotation = f.getAnnotation(InjectContainer.class);
            if (injectionAnnotation != null) {
                ModContainerInjectionHandler.handle(f, injectionAnnotation, clazz);
            }
        }
        return clazz;
    }

    public static void init() {
        //static init
    }
}
