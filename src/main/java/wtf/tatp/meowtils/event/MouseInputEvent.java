package wtf.tatp.meowtils.event;

import wtf.tatp.meowtils.event.api.Event;

public class MouseInputEvent extends Event {
    private final MouseInputEvent.Action action;

    public MouseInputEvent(MouseInputEvent.Action action) {
        this.action = action;
    }

    public MouseInputEvent.Action getAction() {
        return this.action;
    }

    public static enum Action {
        RIGHT_CLICK,
        LEFT_CLICK,
        MIDDLE_CLICK;
    }
}
