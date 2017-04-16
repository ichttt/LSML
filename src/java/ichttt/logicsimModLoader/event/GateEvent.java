package ichttt.logicsimModLoader.event;

import logicsim.Gate;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * Various gate related events
 */
public abstract class GateEvent {

    public final Gate gate;
    /**
     * Class for all GateEvents
     * @since 0.0.2
     */
    private GateEvent(Gate gate) {
        this.gate = gate;
    }


    /**
     * Called when a new {@link Gate} is created
     * @since 0.0.2
     */
    public static class GateConstructionEvent extends GateEvent {
        public GateConstructionEvent(Gate gate) {
            super(gate);
        }
    }


    /**
     * Called when a {@link Gate} is redrawn
     * @since 0.0.2
     */
    public static class GateDrawEvent extends GateEvent {
        /**
         * The graphics context. Possible to be null
         * @since 0.2.0
         */
        public final Graphics graphicsContext;

        @Deprecated
        public GateDrawEvent(Gate gate) {
            this(gate, null);
        }

        /**
         * @since 0.2.0
         */
        public GateDrawEvent(Gate gate, @Nullable Graphics graphics) {
            super(gate);
            graphicsContext = graphics;
        }
    }


    /**
     * Called when a {@link Gate} was deleted
     * @since 0.0.3
     */
    public static class GateDeleteEvent extends GateEvent {
        public GateDeleteEvent(Gate gate) {
            super(gate);
        }
    }


    /**
     * Called when a {@link Gate} has been selected
     * @since 0.0.4
     */
    public static class GateSelectionEvent extends GateEvent {
        public GateSelectionEvent(Gate gate) {
            super(gate);
        }
    }


    /**
     * Called when a {@link Gate} is created from the GUI.
     * <br>Unlike {@link GateConstructionEvent}, this does not fire when internal gates have been created.
     * <br><b>The {@link GateConstructionEvent} will be called, too.</b> This means you will receive both events for the same gate,
     * if you subscribed for it
     * @since 0.2.0
     */
    public static class GateGUICreationEvent extends GateEvent {
        public GateGUICreationEvent(Gate gate) {
            super(gate);
        }
    }
}
