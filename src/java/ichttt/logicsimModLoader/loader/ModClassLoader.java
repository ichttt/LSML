package ichttt.logicsimModLoader.loader;

import ichttt.logicsimModLoader.internal.LSMLLog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ModClassLoader extends URLClassLoader {
    public ModClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader());
    }

    public void injectFile(File file) {
        try {
            addURL(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot transform file " + file + " to URL!", e);
        }
        LSMLLog.fine("Added file %s to the classpath", file);
    }


}
