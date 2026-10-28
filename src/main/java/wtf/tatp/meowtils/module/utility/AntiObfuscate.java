package wtf.tatp.meowtils.module.utility;

import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.RenderStringEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;

public class AntiObfuscate extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;

    public AntiObfuscate() {
        super("AntiObfuscate", Module.Category.Utility);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Deobfuscates text.");
    }

    @EventTarget(
        priority = EventPriority.LOWEST
    )
    public void onRenderString(RenderStringEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null && event.getString() != null) {
            event.setString(event.getString().replace("§k", ""));
        }
    }
}
