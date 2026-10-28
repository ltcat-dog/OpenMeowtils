package wtf.tatp.meowtils.module.bedwars;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.RenderGameOverlayEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.GuiUtil;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.hudeditor.HudEntry;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Duels;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.util.ColorUtil;

public class HeightOverlay extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public boolean showHud = true;
    @Config
    public int red = 255;
    @Config
    public int green = 255;
    @Config
    public int blue = 255;
    @Config
    public float scale = 0.65F;
    @Config
    public int posX = 1;
    @Config
    public int posY = 1;
    @Config
    public boolean woolOverlay = true;
    @Config
    public String woolColor = "§7Gray";
    private static final Pattern MAP_PATTERN = Pattern.compile("You are currently playing on (.+)");
    private static Map<String, Integer> mapHeights;
    private static boolean scanningMap = false;
    private static int height = 255;

    public HeightOverlay() {
        super("HeightOverlay", Module.Category.Bedwars);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Displays map height in HUD and colors wool.");
        ColorLink colorLink = new ColorLink("red", "green", "blue", this);
        this.addColor(new ColorValue("Text color", colorLink));
        this.addSaturation(new SaturationValue(colorLink));
        this.addBrightness(new BrightnessValue(colorLink));
        this.addToggle(new ToggleValue("Show HUD", "showHud", this));
        this.addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, float.class));
        this.addToggle(new ToggleValue("Show wool overlay", "woolOverlay", this));
        this.addMode(new ModeValue("Color", Arrays.asList("§7Gray", "§8Black", "§6Orange"), "woolColor", this));
    }

    public static void init() {
        load();
    }

    public static int getHeight() {
        return height;
    }

    public static EnumDyeColor getWoolColor() {
        String var0 = Module.get(HeightOverlay.class).woolColor;
        switch (var0) {
            case "§7Gray":
                return EnumDyeColor.SILVER;
            case "§8Black":
                return EnumDyeColor.BLACK;
            case "§6Orange":
                return EnumDyeColor.ORANGE;
            default:
                return EnumDyeColor.GRAY;
        }
    }

    @EventTarget(
        priority = EventPriority.LOW
    )
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.getType() == 0) {
            if (!Server.HYPIXEL.isNotActive()) {
                if (!Bedwars.GAME.isNotActive() || !Duels.BEDWARS.isNotActive()) {
                    String msg = event.getComponent().getUnformattedText();
                    if (msg.equals("The game starts in 1 second!") || msg.equals("You will respawn in 6 seconds!")) {
                        Meowtils.sendCleanMessage("/map");
                        scanningMap = true;
                    }

                    Matcher matcher = MAP_PATTERN.matcher(msg);
                    if (matcher.find() && scanningMap) {
                        String mapName = matcher.group(1).trim();
                        height = mapHeights == null ? 255 : mapHeights.getOrDefault(mapName.toLowerCase(), 255);
                        scanningMap = false;
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null) {
            boolean inEditor = GuiUtil.inEditor();
            if (this.mc.currentScreen == null || inEditor) {
                if (!Server.HYPIXEL.isNotActive() || inEditor) {
                    if (!Bedwars.GAME.isNotActive() || !Duels.BEDWARS.isNotActive() || inEditor) {
                        if (!Bedwars.PRE_GAME.isActive() || inEditor) {
                            if (this.showHud) {
                                EnumChatFormatting heightColor = this.mc.thePlayer.getPosition().getY() < height
                                    ? EnumChatFormatting.GREEN
                                    : EnumChatFormatting.RED;
                                Meowtils.drawString(
                                    "Height: "
                                        + heightColor
                                        + this.mc.thePlayer.getPosition().getY()
                                        + EnumChatFormatting.GRAY
                                        + "/"
                                        + EnumChatFormatting.GOLD
                                        + height,
                                    this.posX,
                                    this.posY,
                                    this.scale,
                                    ColorUtil.rgb(this.red, this.green, this.blue)
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    private static void load() {
        try {
            InputStream inputStream = HeightOverlay.class.getClassLoader().getResourceAsStream("meowtils/bedwars/bedwars_map_height.json");
            if (inputStream == null) {
                Meowtils.error("Failed to find bedwars_map_height.json");
                return;
            }

            Type type = (new TypeToken<Map<String, Integer>>() {}).getType();
            mapHeights = (Map<String, Integer>)new Gson().fromJson(new InputStreamReader(inputStream), type);
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }

    @Override
    public List<HudEntry> hudEditor() {
        return this.showHud
            ? Collections.singletonList(new HudEntry(null, this, "posX", "posY", () -> GuiUtil.getHudBounds("Height: 99/999", 1, this.scale)))
            : Collections.emptyList();
    }

    @Override
    public void onReset() {
        height = 255;
    }
}
