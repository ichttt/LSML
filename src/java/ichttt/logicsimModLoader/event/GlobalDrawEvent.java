package ichttt.logicsimModLoader.event;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * Called when the panel is redrawn. This is called much less frequently as {@link GateEvent.GateDrawEvent}, as this
 * is called for <b>all</b> gates on the GateList. If you want to draw something yourself, subscribe to this.
 * @since 0.2.0
 */
public class GlobalDrawEvent {
    @Nonnull
    public final Graphics graphics;

    public GlobalDrawEvent(@Nonnull Graphics graphics) {
        this.graphics = graphics;
    }
}
