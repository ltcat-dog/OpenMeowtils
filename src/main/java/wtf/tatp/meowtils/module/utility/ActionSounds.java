package wtf.tatp.meowtils.module.utility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.AttackEntityEvent;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.Util;

public class ActionSounds extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public boolean blockSound = true;
    @Config
    public boolean critSound = true;
    @Config
    public int volume = 100;
    private static long hurtTime = 0L;
    private static long lastCritTime = 0L;

    public ActionSounds() {
        super("ActionSounds", Module.Category.Utility);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Plays a sound when performing certain actions.");
        this.addSlider(new SliderValue("Volume", 0.0, 100.0, 5.0, "%", "volume", this, Integer.class));
        this.addToggle(new ToggleValue("Blocked damage", "blockSound", this));
        this.addToggle(new ToggleValue("Critical hit", "critSound", this));
    }

    @EventTarget
    public void onPacketReceived(ReceivePacketEvent event) {
        if (this.mc.theWorld != null && this.mc.thePlayer != null) {
            if (this.blockSound) {
                if (event.getPacket() instanceof S19PacketEntityStatus) {
                    if (((S19PacketEntityStatus)event.getPacket()).getEntity(this.mc.theWorld) != this.mc.thePlayer
                        || ((S19PacketEntityStatus)event.getPacket()).getOpCode() != 2) {
                        return;
                    }

                    if (!this.mc.thePlayer.isBlocking()) {
                        return;
                    }

                    long now = System.currentTimeMillis();
                    if (now - hurtTime >= 250L) {
                        Util.playSound(Util.Sound.ANVIL, this.volume);
                        hurtTime = now;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onAttackEntity(AttackEntityEvent event) {
        if (event.getTarget() instanceof EntityPlayer) {
            if (this.critSound) {
                if (!this.mc.thePlayer.onGround
                    && this.mc.thePlayer.fallDistance > 0.0F
                    && !this.mc.thePlayer.isInWater()
                    && !this.mc.thePlayer.isInLava()
                    && !this.mc.thePlayer.isRiding()) {
                    long now = System.currentTimeMillis();
                    if (now - lastCritTime > 250L) {
                        Util.playSound(Util.Sound.CRIT, this.volume);
                        lastCritTime = now;
                    }
                }
            }
        }
    }
}
