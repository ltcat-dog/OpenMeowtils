package wtf.tatp.meowtils.module.render;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.util.User32Util;

public class Animations extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public boolean fakeAutoblock = true;
    @Config
    public String autoblockMode = "Always";
    @Config
    public boolean cancelSwing = false;
    @Config
    public boolean cancelSwingRightClick = false;
    @Config
    public boolean cancelConsume = false;
    @Config
    public boolean cancelBow = false;
    private static long lastClickTime = 0L;
    private static boolean isKillaura = false;

    public Animations() {
        super("Animations", Module.Category.Render);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Allows you to change certain animations. Visual only.\n§eNote: §eRightclick §eonly §eworks §eon §ewindows.");
        this.addToggle(new ToggleValue("Fake autoblock", "fakeAutoblock", this));
        this.addMode(new ModeValue("Activate", Arrays.asList("Always", "Rightclick", "Killaura"), "autoblockMode", this));
        this.addToggle(new ToggleValue("Cancel swing animation", "cancelSwing", this));
        this.addCheck(new CheckValue("Rightclick only", "cancelSwingRightClick", this));
        this.addToggle(new ToggleValue("Cancel consume animation", "cancelConsume", this));
        this.addToggle(new ToggleValue("Cancel bow animation", "cancelBow", this));
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null && event.getPhase() == ClientTickEvent.Phase.POST) {
            if (this.autoblockMode.equals("Killaura")) {
                if (User32Util.holdingLeftClick() || User32Util.holdingRightClick()) {
                    lastClickTime = System.currentTimeMillis();
                }

                long timeSinceClick = System.currentTimeMillis() - lastClickTime;
                isKillaura = this.mc.thePlayer.isSwingInProgress && !User32Util.holdingLeftClick() && !User32Util.holdingRightClick() && timeSinceClick > 500L;
            }
        }
    }

    private static boolean isSword(ItemStack item) {
        return item != null && item.getItem() instanceof ItemSword;
    }

    public static boolean shouldBlock() {
        Minecraft mc = Minecraft.getMinecraft();
        Animations a = Module.get(Animations.class);
        if (!a.enabled) {
            return false;
        } else if (!a.fakeAutoblock) {
            return false;
        } else if (!isSword(mc.thePlayer.getHeldItem())) {
            return false;
        } else {
            String var2 = Module.get(Animations.class).autoblockMode;
            switch (var2) {
                case "Always":
                    return mc.thePlayer.isSwingInProgress;
                case "Rightclick":
                    return User32Util.holdingRightClick();
                case "Killaura":
                    return isKillaura;
                default:
                    return false;
            }
        }
    }
}
