package wtf.tatp.meowtils.module.utility;

import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;

public class ViewClip extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;

    public ViewClip() {
        super("ViewClip", Module.Category.Utility);
        this.tag(Module.ModuleTag.SAFE);
        this.tooltip("Allows your view to clip through blocks when in third person.");
    }
}
