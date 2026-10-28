package wtf.tatp.meowtils.module.hypixel;

import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Server;

public class AutoTip extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public boolean hide = true;
    @Config
    public int delay = 5;
    private static int tickCounter = 0;
    private static long lastTipTime = 0L;

    public AutoTip() {
        super("AutoTip", Module.Category.Hypixel);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Automatically runs /tipall every x minutes.");
        this.addToggle(new ToggleValue("Hide messages", "hide", this));
        this.addSlider(new SliderValue("Delay", 1.0, 10.0, 1.0, "min", "delay", this, int.class));
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (this.mc.theWorld != null && this.mc.thePlayer != null && event.getPhase() == ClientTickEvent.Phase.POST) {
            if (!Server.HYPIXEL.isNotActive()) {
                tickCounter++;
                if (tickCounter >= 200) {
                    tickCounter = 0;
                    long currentTime = System.currentTimeMillis();
                    long tipDelay = this.delay * 60000L;
                    if (currentTime - lastTipTime >= tipDelay) {
                        Meowtils.sendCleanMessage("/tip all");
                        lastTipTime = currentTime;
                    }
                }
            }
        }
    }

    @EventTarget(
        priority = EventPriority.LOWEST
    )
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.getType() == 0) {
            if (!Server.HYPIXEL.isNotActive()) {
                String msg = event.getComponent().getUnformattedText();
                if ((
                        msg.contains("You tipped")
                            || msg.contains("You already tipped everyone")
                            || msg.contains("No one has a network booster active right now! Try again later.")
                    )
                    && this.hide) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
