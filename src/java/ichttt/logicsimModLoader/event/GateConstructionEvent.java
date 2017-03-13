package ichttt.logicsimModLoader.event;

import logicsim.Gate;

/**
 * Called when a new {@link logicsim.Gate} is created
 * @since 0.0.2
 */
public class GateConstructionEvent {
    public final Gate gate;
    public GateConstructionEvent(Gate gate) {
        this.gate = gate;
    }

    
}
