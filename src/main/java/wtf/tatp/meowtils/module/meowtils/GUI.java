package wtf.tatp.meowtils.module.meowtils;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ModuleManager;
import wtf.tatp.meowtils.gui.values.BindValue;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;

public class GUI extends Module {
    @Config
    public boolean enabled = true;
    @Config
    public int key = 54;
    @Config
    public String scale = "Auto";
    @Config
    public boolean tooltips = true;
    @Config
    public boolean blurGui = true;
    @Config
    public int red = 189;
    @Config
    public int green = 140;
    @Config
    public int blue = 255;
    @Config
    public boolean debugMode = false;
    @Config
    public String featureMode = "Unrestricted";
    @Config
    public int scrollSpeed = 10;
    @Config
    public boolean firstStartup = true;
    public static final int RED_DEFAULT = 189;
    public static final int GREEN_DEFAULT = 140;
    public static final int BLUE_DEFAULT = 255;
    private static String lastFeatureMode;
    private static String lastScaleMode;

    public GUI() {
        super("GUI", Module.Category.Meowtils, true);
        this.tooltip(
            "GUI related settings.\n§bMiddle click §f- Bind any module (including this for GUI bind)\n§bScroll wheel §f- Scroll categories/modules if they are too long\n§bFeatures §f- Restrict what type of modules to show"
        );
        ColorLink color = new ColorLink("red", "green", "blue", this);
        this.addColor(new ColorValue("GUI color", color));
        this.addSaturation(new SaturationValue(color));
        this.addBrightness(new BrightnessValue(color));
        this.addSlider(new SliderValue("Scroll speed", 1.0, 25.0, 1.0, null, "scrollSpeed", this, int.class));
        this.addMode(new ModeValue("GUI Scale", Arrays.asList("Tiny", "Small", "Normal", "Large", "Huge", "Auto"), "scale", this));
        this.addMode(new ModeValue("Features", Arrays.asList("Unrestricted", "Safe", "Legit"), "featureMode", this));
        this.addToggle(new ToggleValue("Show tooltips", "tooltips", this));
        this.addToggle(new ToggleValue("Blur background", "blurGui", this));
        this.addBind(new BindValue("Bind", "key", this));
        this.addButton(new ButtonValue("Reset GUI color", 5.0F, () -> {
            this.red = 189;
            this.green = 140;
            this.blue = 255;
            ConfigManager.save();
            if (Notifications.getMode() != Notifications.Mode.CHAT) {
                NotificationManager.show("GUI", "Reset GUI colors!", NotificationManager.Type.INFO, 2000L);
            }

            if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
                Meowtils.addMessage("Reset GUI colors!");
            }
        }));
    }

    public static void init() {
        GUI gui = Module.get(GUI.class);
        lastFeatureMode = gui.featureMode;
        lastScaleMode = gui.scale;
    }

    public static boolean shouldShowModule(Module m) {
        String mode = Module.get(GUI.class).featureMode;
        if (m.alwaysEnabled) {
            return true;
        } else if (!mode.equals("Unrestricted") && m.getTag() == Module.ModuleTag.BLATANT) {
            return false;
        } else {
            return mode.equals("Legit") && m.getTag() != Module.ModuleTag.LEGIT
                ? false
                : !mode.equals("Safe") || m.getTag() == Module.ModuleTag.SAFE || m.getTag() == Module.ModuleTag.LEGIT;
        }
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null && event.getPhase() == ClientTickEvent.Phase.POST) {
            String currentFeatureMode = this.featureMode;
            String currentScaleMode = this.scale;
            if (!Objects.equals(currentFeatureMode, lastFeatureMode)) {
                lastFeatureMode = currentFeatureMode;
                this.changeFeatureMode();
                if (Notifications.getMode() != Notifications.Mode.CHAT) {
                    NotificationManager.show("Restricted Features", "Set to: §e" + currentFeatureMode, NotificationManager.Type.INFO, 2000L);
                }

                if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
                    Meowtils.addMessage("Restricted features set to: " + EnumChatFormatting.YELLOW + currentFeatureMode);
                }
            }

            if (!Objects.equals(currentScaleMode, lastScaleMode)) {
                lastScaleMode = currentScaleMode;
                Meowtils.addMessage(
                    EnumChatFormatting.RED.toString()
                        + EnumChatFormatting.BOLD
                        + "CUSTOM GUI SCALES MAY NOT RENDER PROPERLY!\n"
                        + EnumChatFormatting.GRAY.toString()
                        + EnumChatFormatting.ITALIC
                        + "This warning can be ignored, as it's visual only."
                );
            }
        }
    }

    private void changeFeatureMode() {
        for (Module m : ModuleManager.getModules()) {
            if (!shouldShowModule(m) && m.getState()) {
                m.setState(false);
            }
        }

        Meowtils.getClickGUI().rebuildFrames();
    }
}
