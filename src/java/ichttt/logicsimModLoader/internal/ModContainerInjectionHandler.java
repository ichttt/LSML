package ichttt.logicsimModLoader.internal;

import com.google.common.base.Strings;
import ichttt.logicsimModLoader.api.InjectContainer;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.loader.Loader;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Injects a {@link ModContainer} into the field via the {@link InjectContainer} annotation.
 */
public class ModContainerInjectionHandler {
    private static boolean canInject = false;
    private static List<Injection> injectLater = Collections.synchronizedList(new ArrayList<Injection>());

    private static class Injection {
        public final Field f;
        public final String modid;
        public final boolean optional;

        private Injection(Field f, String modid, boolean optional) {
            this.f = f;
            this.modid = modid;
            this.optional = optional;
        }
    }

    private ModContainerInjectionHandler() {}

    public static void injectNow() {
        LSMLLog.fine("ModContainers build, injecting in already collected fields");
        canInject = true;
        for (Injection injection : injectLater) {
            inject(injection.f, injection.modid, injection.optional);
        }
    }

    public static void handle(Field f, InjectContainer annotation, Class<?> clazz) {
        if (!ModContainer.class.isAssignableFrom(f.getType()))
            throw new RuntimeException("Failed to inject container in class " + f.getClass().getName() + " because the field is not a ModContainer!");
        if (!Modifier.isStatic(f.getModifiers()))
            throw new RuntimeException("Failed to inject container in class " + f.getClass().getName() + " because the field is not static!");
        String modidToInject;
        if (Strings.isNullOrEmpty(annotation.modid())) {
            Mod mod = clazz.getAnnotation(Mod.class);
            if (mod == null)
                throw new RuntimeException("Failed to inject container in class " + f.getClass().getName() + " because the modid is not specified and it is not in the same class as the @Mod");
            modidToInject = mod.modid();
        } else
            modidToInject = annotation.modid();
        if (canInject)
            inject(f, modidToInject, annotation.optional());
        else
            injectLater.add(new Injection(f, modidToInject, annotation.optional()));
    }

    public static void inject(Field f, String modid, boolean optional) {
        ModContainer container = Loader.getInstance().getModContainerForModID(modid);
        if (!optional && container == null)
            throw new RuntimeException("Could not find mod container for modid " + modid + " but it is enforced by annotation");
        try {
            f.setAccessible(true); //makes final and private fields possible
            f.set(null, container);
        } catch (ReflectiveOperationException e) {
            //How?
            throw new RuntimeException(e);
        }
    }
}
