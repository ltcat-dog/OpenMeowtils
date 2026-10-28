package wtf.tatp.meowtils.module.render;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.RenderPlayerEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.module.meowtils.GUI;
import wtf.tatp.meowtils.util.TeamUtil;

public class AntiInvis extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public float opacity = 50.0F;
    private static final Set<EntityPlayer> INVISIBLE = Collections.newSetFromMap(new WeakHashMap<>());

    public AntiInvis() {
        super("Anti-Invis", Module.Category.Render);
        this.tag(Module.ModuleTag.SAFE);
        this.tooltip("Renders invisible players semi-transparent instead.");
        this.addOpacity(new OpacityValue("Opacity", "opacity", this));
    }

    @EventTarget
    public void onRenderPlayer(RenderPlayerEvent event) {
        EntityPlayer player = event.getPlayer();
        float dividedOpacity = this.opacity / 100.0F;
        if (event.getStage() == RenderPlayerEvent.Stage.PRE) {
            if (!this.shouldBeTransparent(player)) {
                return;
            }

            if (player.isInvisible()) {
                player.setInvisible(false);
                INVISIBLE.add(player);
            }

            if (dividedOpacity < 1.0F) {
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, dividedOpacity);
            }
        }

        if (event.getStage() == RenderPlayerEvent.Stage.POST) {
            if (INVISIBLE.contains(player)) {
                player.setInvisible(true);
                INVISIBLE.remove(player);
            }

            if (dividedOpacity < 1.0F) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glDisable(3042);
            }
        }
    }

    private boolean shouldBeTransparent(EntityPlayer player) {
        return Module.get(GUI.class).debugMode ? true : player != this.mc.thePlayer && player.isInvisible() && !TeamUtil.isBot(player);
    }
}
