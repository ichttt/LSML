package ichttt.logicsimModLoader.loader;

import ichttt.logicsimModLoader.exceptions.MalformedFileException;
import ichttt.logicsimModLoader.util.LSMLUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @since 0.0.1
 */
public class ModDataReader {


    static String parseAnnotationInfo(String rawName) throws IOException {
        BufferedReader reader = null;
        String read;
        try {
            File modInfo = new File(rawName + ".modinfo");
            reader = new BufferedReader(new FileReader(modInfo));
            while (true) {
                read = reader.readLine();
                if (read == null)
                    break;
                read = read.replaceAll(" ", "");
                if (!read.startsWith("#"))
                    break;
            }
        }
        finally {
            LSMLUtil.closeSilent(reader);
        }
        if (read == null || !read.startsWith("modClass="))
            throw new MalformedFileException(String.format("Illegal String %s in file %s ", read, rawName));
        read = read.substring("modClass=".length());
        return read;
    }
}
