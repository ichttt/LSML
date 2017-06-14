package ichttt.logicsimModLoader.config;

import com.google.common.base.Strings;
import ichttt.logicsimModLoader.config.entry.ConfigEntryBase;
import ichttt.logicsimModLoader.internal.LSMLLog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.0.1
 */
public class ConfigCategory extends ConfigElement {
    @Nonnull
    public final String name;
    private final List<ConfigEntryBase> configEntrys = new ArrayList<>();

    /**
     * Creates a new ConfigCategory
     * <b>Do not create categories with duplicate names!</b>
     * Please don't use null as a name, support will be remove soon(tm)
     * @param name The name of the category
     * @since 0.0.1
     */
    public ConfigCategory(@Nullable String name) {
        super(1);
        if (name == null) {
            LSMLLog.warning("ConfigCategory is using null as name. This is deprecated!");
            name = "Default";
        }
        this.name = name;
    }

    /**
     * Adds an entry to this config.
     * If the entry is already present, it will be ignored
     * @param entry The entry you want to add
     * @since 0.0.1
     */
    public void addEntry(ConfigEntryBase entry) {
        ConfigEntryBase toRemove = null;
        for (ConfigEntryBase entryBase : configEntrys) {
            if (entryBase.key.equals(entry.key)) {
                try {
                    //noinspection unchecked
                    entryBase.setValue(entry.getValue());
                    return;
                } catch (Exception e) {
                    //Just fall back to the old behavior
                    toRemove = entryBase;
                    break; //Should only find one
                }
            }
        }
        if (toRemove != null)
            configEntrys.remove(toRemove);
        configEntrys.add(entry);
    }

    /**
     * Gets a config entry
     * @param key The key of the entry
     * @return The entry or null if not found
     * @since 0.0.1
     */
    @Nullable
    public ConfigEntryBase getConfigEntry(String key) {
        for (ConfigEntryBase<?> entryBase : configEntrys) {
            if (entryBase.key.equals(key))
                return entryBase;
        }
        return null;
    }

    @Nonnull
    @Override
    public List<String> generateLines() {
        List<String> lines = new ArrayList<>();
        lines.add("----------" + name + "----------");
        for (ConfigEntryBase entry : configEntrys) {
            lines.addAll(entry.getOffsetLines());
            lines.add("");
        }

        if (lines.size()>1) {
            for (int i = 1; i < lines.size(); i++) {
                lines.set(i, "|" + lines.get(i));
            }
        }

        String s = Strings.repeat("-", name.length() + 20);
        lines.add(s);
        return lines;
    }
}
