package wtf.tatp.meowtils.module.hypixel;

import java.util.Arrays;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.RenderStringEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.TextValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Util;

public class AccountHider extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public boolean skin = false;
    @Config
    public String skinLocation = "";
    @Config
    public boolean hideName = false;
    @Config
    public String customName = "You";
    @Config
    public String armMode = "Default";

    public AccountHider() {
        super("AccountHider", Module.Category.Hypixel);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Visually modify account information.\n§d/customname <name> §f- Set custom name");
        this.addExpand(new ExpandValue("Custom name", e -> {
            e.addToggle(new ToggleValue("Enabled", "hideName", this));
            e.addText(new TextValue("Name", "You", "customName", this));
        }));
        this.addExpand(new ExpandValue("Custom skin", e -> {
            e.addToggle(new ToggleValue("Enabled", "skin", this));
            e.addMode(new ModeValue("Arms", Arrays.asList("Default", "Slim"), "armMode", this));
            e.addText(new TextValue("Skin", "File name", "skinLocation", this));
            e.addButton(new ButtonValue("Skin folder", 5.0F, () -> Util.openFolder(Meowtils.CUSTOM_SKIN_DIR, "skin")));
        }));
    }

    @EventTarget(
        priority = EventPriority.LOWEST
    )
    public void onRenderString(RenderStringEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null && event.getString() != null) {
            String text = event.getString();
            String playerName = this.mc.thePlayer.getName();
            String fakeName = this.customName.isEmpty() ? "You" : ColorUtil.convertFormatting(this.customName);
            if (this.hideName && text.contains(playerName)) {
                event.setString(text.replace(playerName, fakeName));
            }
        }
    }
}
