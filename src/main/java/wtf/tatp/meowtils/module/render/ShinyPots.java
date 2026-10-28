package wtf.tatp.meowtils.module.render;

import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;

public class ShinyPots extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;

    public ShinyPots() {
        super("ShinyPots", Module.Category.Render);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Renders potion color as slot background.");
    }
}
