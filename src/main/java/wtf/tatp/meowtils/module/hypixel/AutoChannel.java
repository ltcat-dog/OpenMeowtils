package wtf.tatp.meowtils.module.hypixel;

import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;

public class AutoChannel extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;

    public AutoChannel() {
        super("AutoChannel", Module.Category.Hypixel);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Automatically switches chat channel when joining/leaving a party.");
    }

    public static void swapToAll() {
        if (Module.get(AutoChannel.class).enabled) {
            Meowtils.sendCleanMessage("/chat all");
        }
    }

    public static void swapToParty() {
        if (Module.get(AutoChannel.class).enabled) {
            Meowtils.sendCleanMessage("/chat party");
        }
    }
}
