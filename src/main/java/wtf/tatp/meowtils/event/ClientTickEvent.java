package wtf.tatp.meowtils.event;

import wtf.tatp.meowtils.event.api.Event;

public class ClientTickEvent extends Event {
    private final ClientTickEvent.Phase phase;

    public ClientTickEvent(ClientTickEvent.Phase phase) {
        this.phase = phase;
    }

    public ClientTickEvent.Phase getPhase() {
        return this.phase;
    }

    public static enum Phase {
        PRE,
        POST;
    }
}
