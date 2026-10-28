package wtf.tatp.meowtils.stats;

import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.hypixel.Stats;

public interface StatsSource {
    String getId();

    StatsContainer fetch(String var1);

    default long getCooldown() {
        return Module.get(Stats.class).cooldown;
    }
}
