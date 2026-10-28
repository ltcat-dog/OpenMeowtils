package wtf.tatp.meowtils.event;

import wtf.tatp.meowtils.event.api.Event;

public class RenderTickEvent extends Event {
    private final RenderTickEvent.Phase phase;
    private final float partialTicks;

    public RenderTickEvent(RenderTickEvent.Phase phase, float partialTicks) {
        this.phase = phase;
        this.partialTicks = partialTicks;
    }

    public RenderTickEvent.Phase getPhase() {
        return this.phase;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public static enum Phase {
        PRE,
        POST;
    }
}
