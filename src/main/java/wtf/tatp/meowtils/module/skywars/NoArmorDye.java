package wtf.tatp.meowtils.module.skywars;

import java.util.Arrays;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ModeValue;

public class NoArmorDye extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public String mode = "Both";

    public NoArmorDye() {
        super("NoArmorDye", Module.Category.Skywars);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Removes dye from leather armor.");
        this.addMode(new ModeValue("Mode", Arrays.asList("Both", "Model", "Item"), "mode", this));
    }
}
