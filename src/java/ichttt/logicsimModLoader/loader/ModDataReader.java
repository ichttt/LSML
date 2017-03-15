package ichttt.logicsimModLoader.loader;

import ichttt.logicsimModLoader.exceptions.MalformedFileException;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.annotation.Nonnull;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @since 0.0.1
 */
class ModDataReader {
    private static final String SHA_STRING = "SHA1String=";
    private static final String MOD_CLASS_STRING = "modClass=";
    @Nonnull
    static String parseModInfo(File jarFile, String rawName) throws IOException {
        BufferedReader reader = null;
        String modClass = null;
        try {
            File modInfo = new File(rawName + ".modinfo");
            reader = new BufferedReader(new FileReader(modInfo));
            String read;
            while (true) {
                read = reader.readLine();
                if (read == null)
                    break;
                read = read.replaceAll(" ", "");
                if (read.startsWith(SHA_STRING) || read.startsWith(SHA_STRING.toLowerCase()) || read.startsWith(SHA_STRING.toUpperCase()))
                    parseJarValidation(jarFile, read);
                if (read.startsWith(MOD_CLASS_STRING) || read.startsWith(MOD_CLASS_STRING.toLowerCase()) || read.startsWith(MOD_CLASS_STRING.toUpperCase()))
                    modClass = parseModClass(read);
            }
        }
        finally {
            LSMLUtil.closeSilent(reader);
        }
        if (modClass == null)
            throw new MalformedFileException(String.format("Mod %s does not define modClass", rawName));
        return modClass;
    }

    @Nonnull
    private static String parseModClass(String line) {
        return line.substring(MOD_CLASS_STRING.length());
    }


    private static void parseJarValidation(File jarFile, String line) throws IOException {
        FileInputStream inputStream = null;
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            inputStream = new FileInputStream(jarFile);
            StringBuilder hexString = new StringBuilder();

            byte[] buffer = new byte[1024];

            int readData;
            while ((readData = inputStream.read(buffer)) != -1) {
                sha1.update(buffer, 0, readData);
            }

            byte[] hash = sha1.digest();
            for (byte hashComp : hash) {
                if ((0xff & hashComp) < 0x10) {
                    hexString.append("0").append(Integer.toHexString((0xFF & hashComp)));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hashComp));
                }
            }

            if (!hexString.toString().equalsIgnoreCase(line.substring(SHA_STRING.length()))) {
                LSMLLog.error("Stopping loading because of mismatching SHAString", jarFile);
                LSMLLog.error("File has String %s while modinfo wants %s", hexString.toString().toUpperCase(), line.substring(SHA_STRING.length()).toUpperCase());
                throw new SecurityException("Cannot continue loading because of security concerns.");
            } else {
                LSMLLog.fine("Found valid checksum for mod %s", jarFile);
            }
        } catch (NoSuchAlgorithmException e) { //What JRE does not have SHA1...
            boolean ignoreMissAlgorithm = false;
            try {
                if (!System.getProperty("lsml.ignoreMissingAlgorithm").equalsIgnoreCase("true"))
                    ignoreMissAlgorithm = true;
            } catch (Exception ignored) {}
            if (!ignoreMissAlgorithm) {
                LSMLLog.error("Stopping loading because the file %s cold not be verified and lsml.ignoreMissingAlgorithm is not set to true!", jarFile);
                throw new SecurityException("Cannot continue loading because of security concerns.");
            }
        } finally {
            LSMLUtil.closeSilent(inputStream);
        }
    }
}
