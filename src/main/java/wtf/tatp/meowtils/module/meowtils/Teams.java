package wtf.tatp.meowtils.module.meowtils;

import java.util.Arrays;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;

public class Teams extends Module {
    @Config
    public boolean enabled = true;
    @Config
    public int key = 0;
    @Config
    public String ignoreBotMode = "Dynamic";
    @Config
    public boolean ignoreTeam = true;
    @Config
    public boolean ignoreFriends = false;

    public Teams() {
        super("Teams", Module.Category.Meowtils, true);
        this.tooltip("Makes certain modules ignore teammates.");
        this.addMode(new ModeValue("Ignore bots", Arrays.asList("Hypixel", "Universal", "Dynamic", "None"), "ignoreBotMode", this));
        this.addToggle(new ToggleValue("Ignore team", "ignoreTeam", this));
        this.addToggle(new ToggleValue("Ignore friends", "ignoreFriends", this));
    }
}
