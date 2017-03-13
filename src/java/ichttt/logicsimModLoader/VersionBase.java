package ichttt.logicsimModLoader;

import ichttt.logicsimModLoader.exceptions.InvalidVersionStringException;

/**
 * A basic version system. The version is immutable
 * @since 0.0.1
 */
public class VersionBase {
    public final int major, minor, patch;

    public VersionBase(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public VersionBase(String versionString) {
        char[] chars = versionString.toCharArray();
        int pos = 0;
        int arrayPos = 0;
        String[] split = new String[3];
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '.') {
                split[arrayPos] = versionString.substring(pos, i);
                pos = i+1;
                arrayPos++;
            }
        }
        split[arrayPos] = versionString.substring(pos);
        try {
            this.major = Integer.parseInt(split[0]);
            this.minor = Integer.parseInt(split[1]);
            this.patch = Integer.parseInt(split[2]);
        }
        catch (NumberFormatException e) {
            throw new InvalidVersionStringException(versionString);
        }
    }

    public String getVersionString() {
        return String.format("%s.%s.%s", major , minor, patch);
    }

    /**
     * Checks this version against another
     * @param versionBase The minimum requirement
     * @return true if the requirement is satisfied
     */
    public boolean isMinimum(VersionBase versionBase) {
        if (this.major<versionBase.major)
            return false;
        if (this.major>versionBase.major)
            return true;
        if (this.minor<versionBase.minor)
            return false;
        if (this.minor>versionBase.minor)
            return true;
        if (this.patch<versionBase.patch)
            return false;
        if (this.patch>=versionBase.patch)
            return true;
        throw new RuntimeException("You missed a case O.o");
    }
}
