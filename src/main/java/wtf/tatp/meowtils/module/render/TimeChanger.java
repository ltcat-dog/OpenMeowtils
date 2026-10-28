package wtf.tatp.meowtils.module.render;

import java.time.LocalTime;
import org.lwjgl.input.Keyboard;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BindValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;

public class TimeChanger extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public int time = 12;
    @Config
    public boolean realTime = false;
    @Config
    public int increaseKey = 0;
    @Config
    public int decreaseKey = 0;
    private static int tickCounter = 0;

    public TimeChanger() {
        super("TimeChanger", Module.Category.Render);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Changes time clientside.");
        this.addSlider(new SliderValue("Time", 0.0, 24.0, 1.0, "hours", "time", this, Integer.class));
        this.addToggle(new ToggleValue("Use real time", "realTime", this));
        this.addBind(new BindValue("Increase", "increaseKey", this));
        this.addBind(new BindValue("Decrease", "decreaseKey", this));
    }

    public static long getRealTime() {
        LocalTime now = LocalTime.now();
        double totalHours = now.getHour() + now.getMinute() / 60.0 + now.getSecond() / 3600.0;
        return toIngameTime(totalHours);
    }

    public static long toIngameTime(double hour) {
        double shifted = hour - 6.0;
        if (shifted < 0.0) {
            shifted += 24.0;
        }

        return (long)(shifted * 1000.0);
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null && event.getPhase() == ClientTickEvent.Phase.POST) {
            if (!Keyboard.isKeyDown(0)) {
                if (!this.realTime) {
                    if (this.time >= 0 && this.time <= 24) {
                        tickCounter++;
                        if (tickCounter >= 2) {
                            tickCounter = 0;
                            if (Keyboard.isKeyDown(this.increaseKey)) {
                                if (this.time > 23) {
                                    return;
                                }

                                this.time++;
                            } else if (Keyboard.isKeyDown(this.decreaseKey)) {
                                if (this.time < 1) {
                                    return;
                                }

                                this.time--;
                            }
                        }
                    }
                }
            }
        }
    }
}
