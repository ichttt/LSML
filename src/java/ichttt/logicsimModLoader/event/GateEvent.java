package ichttt.logicsimModLoader.event;

import logicsim.Gate;

/**
 * Created by Tobias on 16.03.2017.
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
     */
    public static class GateDrawEvent extends GateEvent {
        public GateDrawEvent(Gate gate) {
            super(gate);
        }
    }
}
