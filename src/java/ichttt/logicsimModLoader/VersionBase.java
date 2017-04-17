package ichttt.logicsimModLoader;

import ichttt.logicsimModLoader.exceptions.InvalidVersionStringException;

import javax.annotation.Nonnull;

/**
 * A basic version system. The version is immutable
 * @since 0.0.1
 */
public class VersionBase implements Comparable<VersionBase> {
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
        return compareTo(versionBase) >= 0;
    }

    /**
     * @since 0.0.2
     */
    @Override
    public String toString() {
        return getVersionString();
    }

    /**
     * @since 0.2.0
     */
    @Override
    public int compareTo(@Nonnull VersionBase o) {
        int result = this.major - o.major;
        if (result != 0)
            return result;
        result = this.minor - o.minor;
        if (result != 0)
            return result;
        return this.patch - o.patch;
    }

    /**
     * @since 0.2.0
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VersionBase) {
            VersionBase toCompare = (VersionBase) obj;
            return toCompare.compareTo(this) == 0;
        }
        return false;
    }
}
