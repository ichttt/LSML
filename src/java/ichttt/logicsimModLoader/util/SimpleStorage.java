package ichttt.logicsimModLoader.util;

import ichttt.logicsimModLoader.api.ISaveHandler;
import ichttt.logicsimModLoader.api.Mod;
import ichttt.logicsimModLoader.exceptions.MalformedFileException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple implementation of {@link ISaveHandler}.
 * You can use this for {@link ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent#registerSaveHandler(Mod, ISaveHandler)}, or implement it yourself.
 * @since 0.1.5
 */
public class SimpleStorage implements ISaveHandler {
    public static final String SIMPLE_FORMAT_IDENTIFIER = "SF";
    public static final String KEY_IDENTIFIER = "K";
    public static final String VALUE_IDENTIFIER = "V";

    private final Map<String, String> keyValueMap = new HashMap<>();

    public void putEntry(String key, String value) {
        keyValueMap.put(key, value);
    }

    @Nonnull
    @Override
    public List<String> saveLines() {
        List<String> list = new ArrayList<>();
        list.add(SIMPLE_FORMAT_IDENTIFIER);
        for (Map.Entry entry : keyValueMap.entrySet()) {
            list.add(KEY_IDENTIFIER + entry.getKey() + VALUE_IDENTIFIER + entry.getValue());
        }
        return list;
    }

    @Override
    public void loadLines(List<String> lines) {
        if (!lines.get(0).equals(SIMPLE_FORMAT_IDENTIFIER))
            throw new MalformedFileException("SaveHandler is not using SimpleFormat");
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.startsWith(KEY_IDENTIFIER))
                throw new MalformedFileException(String.format("Invalid character %s at line %s", line.substring(0, 1), i));
            line = line.substring(1);
            String[] split = line.split(VALUE_IDENTIFIER);
            if (split.length != 2)
                throw new MalformedFileException(String.format("Invalid character %s at line %s", line.substring(0, 1), i));
            keyValueMap.put(split[0], split[1]);
        }
    }

    @Nullable
    public String getValue(String key) {
        return keyValueMap.get(key);
    }
}
