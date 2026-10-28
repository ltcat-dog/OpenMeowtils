package wtf.tatp.meowtils.event;

import net.minecraft.client.multiplayer.WorldClient;
import wtf.tatp.meowtils.event.api.Event;

public class WorldEvent extends Event {
    private final WorldClient world;
    private final WorldEvent.Type type;

    public WorldEvent(WorldClient world, WorldEvent.Type type) {
        this.world = world;
        this.type = type;
    }

    public WorldClient getWorld() {
        return this.world;
    }

    public WorldEvent.Type getType() {
        return this.type;
    }

    public static enum Type {
        LOAD,
        UNLOAD;
    }
}
