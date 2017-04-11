package ichttt.logicsimModLoader.api;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since 0.0.2
 */
public interface ISaveHandler {
    @Nonnull
    List<String> saveLines();

    void loadLines(List<String> list);
}
