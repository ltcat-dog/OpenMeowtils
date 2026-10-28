package wtf.tatp.meowtils.module.utility;

import net.minecraft.client.settings.KeyBinding;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.RenderTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;

public class Sprint extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;

    public Sprint() {
        super("Sprint", Module.Category.Utility);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Automatically toggles sprint for you.");
    }

    @EventTarget
    public void onRenderTick(RenderTickEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null && event.getPhase() == RenderTickEvent.Phase.PRE) {
            KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }
}
