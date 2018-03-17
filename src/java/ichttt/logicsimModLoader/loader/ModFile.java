package ichttt.logicsimModLoader.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

class ModFile {
    public final File file;
    public final String pathToInstance;

    public ModFile(File file, String pathToInstance) {

        this.file = file;
        this.pathToInstance = pathToInstance;
    }

    public URL getFileURL() throws MalformedURLException {
        return file.toURI().toURL();
    }
}
