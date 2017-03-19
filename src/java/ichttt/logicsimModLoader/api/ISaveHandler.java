package ichttt.logicsimModLoader.api;

import java.util.List;

/**
 * @since 0.0.2
 */
public interface ISaveHandler {
    List<String> saveLines();

    void loadLines(List<String> list);
}
