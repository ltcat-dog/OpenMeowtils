package wtf.tatp.meowtils.module.skywars;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.EntityJoinWorldEvent;
import wtf.tatp.meowtils.event.KeyInputEvent;
import wtf.tatp.meowtils.event.RenderGameOverlayEvent;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.GuiUtil;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.hudeditor.HudEntry;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.Render;
import wtf.tatp.meowtils.util.Util;

public class PearlDetector extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public int red = 255;
    @Config
    public int green = 255;
    @Config
    public int blue = 255;
    @Config
    public float scale = 0.65F;
    @Config
    public boolean timerSelf = true;
    @Config
    public boolean positionOthers = true;
    @Config
    public boolean thrownAlert = true;
    @Config
    public boolean sound = true;
    @Config
    public boolean ender = true;
    @Config
    public boolean corrupt = true;
    @Config
    public boolean warp = true;
    @Config
    public int posX = 1;
    @Config
    public int posY = 1;
    @Config
    public String timerStartMode = "On message";
    private static final String WARNING_MESSAGE = EnumChatFormatting.DARK_RED.toString() + EnumChatFormatting.BOLD + " !!!";
    private static final Map<UUID, PearlDetector.PearlType> LAST_HELD_PEARL = new HashMap<>();
    private static final List<PearlDetector.BoxLocation> ACTIVE_BOXES = new ArrayList<>();
    private static float cooldown = 0.0F;
    private static int tickCounter = 0;
    private static boolean thrown = false;

    public PearlDetector() {
        super("PearlDetector", Module.Category.Skywars);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Detects thrown pearls & displays various information such as warp location\nfor other players and warp timer for yourself.");
        ColorLink color = new ColorLink("red", "green", "blue", this);
        this.addColor(new ColorValue("Text color", color));
        this.addSaturation(new SaturationValue(color));
        this.addBrightness(new BrightnessValue(color));
        this.addMode(new ModeValue("Timer", Arrays.asList("On message", "On click"), "timerStartMode", this));
        this.addSlider(new SliderValue("Text scale", 0.5, 1.5, 0.05, null, "scale", this, float.class));
        this.addToggle(new ToggleValue("Ping sound", "sound", this));
        this.addToggle(new ToggleValue("Timer for self", "timerSelf", this));
        this.addToggle(new ToggleValue("Position for others", "positionOthers", this));
        this.addToggle(new ToggleValue("Thrown pearl alert", "thrownAlert", this));
        this.addCheck(new CheckValue("§5Ender §fPearl", "ender", this));
        this.addCheck(new CheckValue("§3Corrupt §fPearl", "corrupt", this));
        this.addCheck(new CheckValue("§dTime §dWarp §fPearl", "warp", this));
    }

    @EventTarget
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (this.mc.theWorld != null && this.mc.thePlayer != null) {
            if (this.mc.currentScreen == null || GuiUtil.inEditor()) {
                if (Skywars.GAME.isActive() || GuiUtil.inEditor()) {
                    if (this.timerSelf) {
                        if (thrown || GuiUtil.inEditor()) {
                            int color = ColorUtil.rgb(this.red, this.green, this.blue);
                            if (GuiUtil.inEditor()) {
                                Meowtils.drawString("Time Warp: " + EnumChatFormatting.GREEN + "5.0", this.posX, this.posY, this.scale, color);
                            }

                            EnumChatFormatting cooldownColor = cooldown >= 2.5
                                ? EnumChatFormatting.DARK_GREEN
                                : (
                                    cooldown >= 2.0F
                                        ? EnumChatFormatting.GREEN
                                        : (
                                            cooldown >= 1.5
                                                ? EnumChatFormatting.YELLOW
                                                : (
                                                    cooldown >= 1.0F
                                                        ? EnumChatFormatting.GOLD
                                                        : (cooldown >= 0.5 ? EnumChatFormatting.RED : EnumChatFormatting.DARK_RED)
                                                )
                                        )
                                );
                            Meowtils.drawString(
                                "Time Warp: " + cooldownColor + String.format("%.1f", cooldown).replace(",", "."), this.posX, this.posY, this.scale, color
                            );
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onChatReceived(ChatReceivedEvent event) {
        if (!Skywars.GAME.isNotActive()) {
            if (event.getType() == 0) {
                if (!this.timerStartMode.equals("On click")) {
                    String msg = event.getComponent().getUnformattedText();
                    if (msg.equals("You will be warped back in 3 seconds!")) {
                        thrown = true;
                        cooldown = 3.0F;
                        tickCounter = 0;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onKeyInput(KeyInputEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null) {
            if (!Skywars.GAME.isNotActive()) {
                if (!this.timerStartMode.equals("On message")) {
                    if (!thrown) {
                        if (event.getKey() == this.mc.gameSettings.keyBindUseItem) {
                            ItemStack held = this.mc.thePlayer.getHeldItem();
                            if (held == null) {
                                return;
                            }

                            if (!(held.getItem() instanceof ItemEnderPearl)) {
                                return;
                            }

                            if (held.getTooltip(this.mc.thePlayer, false).stream().anyMatch(s -> s.contains("Teleport back"))) {
                                thrown = true;
                                cooldown = 3.0F;
                                tickCounter = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null && event.getPhase() == ClientTickEvent.Phase.POST) {
            if (!Skywars.GAME.isNotActive() || !Skywars.MINI.isNotActive()) {
                if (thrown && cooldown > 0.0F) {
                    tickCounter++;
                    if (tickCounter >= 2) {
                        cooldown -= 0.1F;
                        tickCounter = 0;
                        if (cooldown <= 0.0F) {
                            cooldown = 0.0F;
                            thrown = false;
                        }
                    }
                }

                for (EntityPlayer player : this.mc.theWorld.playerEntities) {
                    if (player != this.mc.thePlayer) {
                        ItemStack held = player.getHeldItem();
                        if (held != null && held.getItem() == Items.ender_pearl) {
                            PearlDetector.PearlType type;
                            if (held.hasEffect()) {
                                if (held.getTooltip(this.mc.thePlayer, false).stream().anyMatch(s -> s.contains("Teleport back"))) {
                                    type = PearlDetector.PearlType.TIME_WARP;
                                } else {
                                    type = PearlDetector.PearlType.CORRUPT;
                                }
                            } else {
                                type = PearlDetector.PearlType.NORMAL;
                            }

                            LAST_HELD_PEARL.put(player.getUniqueID(), type);
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!Skywars.GAME.isNotActive() || !Skywars.MINI.isNotActive()) {
            if (this.positionOthers) {
                if (event.getEntity() instanceof EntityEnderPearl) {
                    EntityEnderPearl pearl = (EntityEnderPearl)event.getEntity();
                    EntityPlayer closest = null;
                    double closestDistanceSq = Double.MAX_VALUE;

                    for (EntityPlayer player : pearl.worldObj.playerEntities) {
                        if (player != this.mc.thePlayer) {
                            double distanceSq = player.getDistanceSqToEntity(pearl);
                            if (distanceSq < 4.0 && distanceSq < closestDistanceSq) {
                                closestDistanceSq = distanceSq;
                                closest = player;
                            }
                        }
                    }

                    if (closest == null) {
                        return;
                    }

                    PearlDetector.PearlType type = LAST_HELD_PEARL.getOrDefault(closest.getUniqueID(), PearlDetector.PearlType.NONE);
                    this.alert(closest, type);
                }
            }
        }
    }

    @EventTarget
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!Skywars.GAME.isNotActive()) {
            if (this.positionOthers) {
                long now = System.currentTimeMillis();
                ACTIVE_BOXES.removeIf(boxx -> now - boxx.spawnTime > 4000L);

                for (PearlDetector.BoxLocation pos : ACTIVE_BOXES) {
                    Color color = ColorUtil.getColor(this.red, this.green, this.blue, 150);
                    double width = 0.6;
                    double height = 1.8;
                    double halfWidth = 0.3;
                    AxisAlignedBB box = new AxisAlignedBB(pos.x - 0.3, pos.y, pos.z - 0.3, pos.x + 0.3, pos.y + 1.8, pos.z + 0.3);
                    Render.drawBlockBox(box, null, true, color, false, color, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    private void alert(EntityPlayer player, PearlDetector.PearlType type) {
        String name = NameUtil.getTabDisplayName(player.getName());
        String message = name + EnumChatFormatting.GRAY + " threw a ";
        if (this.positionOthers && type == PearlDetector.PearlType.TIME_WARP) {
            ACTIVE_BOXES.add(new PearlDetector.BoxLocation(player.posX, player.posY, player.posZ, System.currentTimeMillis()));
        }

        if (this.thrownAlert) {
            if (type == PearlDetector.PearlType.TIME_WARP && this.warp) {
                Meowtils.addMessage(message + EnumChatFormatting.LIGHT_PURPLE + "Time Warp Pearl" + WARNING_MESSAGE);
            } else if (type == PearlDetector.PearlType.CORRUPT && this.corrupt) {
                Meowtils.addMessage(message + EnumChatFormatting.DARK_AQUA + "Corrupt Pearl" + WARNING_MESSAGE);
            } else {
                if (type != PearlDetector.PearlType.NORMAL || !this.ender) {
                    return;
                }

                Meowtils.addMessage(name + EnumChatFormatting.GRAY + " threw an " + EnumChatFormatting.DARK_PURPLE + "Ender Pearl" + WARNING_MESSAGE);
            }

            if (this.sound) {
                Util.playSound(Util.Sound.MEOW, 100);
            }
        }
    }

    @Override
    public List<HudEntry> hudEditor() {
        return this.timerSelf
            ? Collections.singletonList(new HudEntry(null, this, "posX", "posY", () -> GuiUtil.getHudBounds("Time Warp: 5 ", 1, this.scale)))
            : Collections.emptyList();
    }

    @Override
    public void onReset() {
        LAST_HELD_PEARL.clear();
        ACTIVE_BOXES.clear();
        cooldown = 0.0F;
        tickCounter = 0;
        thrown = false;
    }

    private static class BoxLocation {
        public final double x;
        public final double y;
        public final double z;
        public final long spawnTime;

        public BoxLocation(double x, double y, double z, long spawnTime) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.spawnTime = spawnTime;
        }
    }

    private static enum PearlType {
        NONE,
        NORMAL,
        CORRUPT,
        TIME_WARP;
    }
}
