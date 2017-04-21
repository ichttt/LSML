package ichttt.logicsimModLoader.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * A few utilities for download stuff
 */
public class NetworkHelper {
    public static String readURLUncached(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String read;
            StringBuilder builder = new StringBuilder();
            while ((read = reader.readLine()) != null) {
                builder.append(read).append("\n");
            }
            return builder.toString();
        } finally {
            LSMLUtil.closeSilent(reader);
        }
    }

    public static void readFileFromURL(URL url, File outputFile) throws IOException {
        if (!outputFile.exists())
            if (!outputFile.createNewFile())
                throw new RuntimeException("Could not create File " + outputFile);
        URLConnection connection = url.openConnection();
        InputStream input = null;
        try {
            input = connection.getInputStream();
            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream(outputFile);
                byte[] buffer = new byte[4096];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    stream.write(buffer, 0, read);
                }
            } finally {
                LSMLUtil.closeSilent(stream);
            }
        } finally {
            LSMLUtil.closeSilent(input);
        }
    }
}
