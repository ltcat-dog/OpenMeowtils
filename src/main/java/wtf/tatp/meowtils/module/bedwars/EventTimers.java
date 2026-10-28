package wtf.tatp.meowtils.module.bedwars;

import java.util.Arrays;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.RenderGameOverlayEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.GuiUtil;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.hudeditor.HudEntry;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.util.Render;
import wtf.tatp.meowtils.util.Util;

public class EventTimers extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public int eventPosX = 0;
    @Config
    public int eventPosY = 0;
    @Config
    public float scale = 0.65F;
    @Config
    public boolean eventTime = true;
    @Config
    public boolean onlyNext = false;
    @Config
    public boolean romanNumerals = false;
    @Config
    public boolean eventDynamicColor = false;
    @Config
    public boolean diamondTimer = true;
    @Config
    public boolean emeraldTimer = true;
    @Config
    public boolean bedGoneTimer = true;
    @Config
    public boolean suddenDeathTimer = true;
    @Config
    public boolean gameEndTimer = true;
    @Config
    public boolean emeraldTime = true;
    @Config
    public int emeraldPosX = 1;
    @Config
    public int emeraldPosY = 1;
    @Config
    public boolean emeraldDynamicColor = true;
    private static final int EMERALD_II_TIME = 720;
    private static final int EMERALD_III_TIME = 1440;
    private static final int FIRST_EMERALD_SPAWN_TIME = 31;
    private static final EventTimers.EmeraldEntry EIGHT_TEAMS_MODE_DATA = new EventTimers.EmeraldEntry(65, 50, 35, 4);
    private static final EventTimers.EmeraldEntry FOUR_TEAMS_MODE_DATA = new EventTimers.EmeraldEntry(55, 40, 27, 2);
    private static final ItemStack EMERALD_ICON = new ItemStack(Items.emerald);
    private static long gameStartTime = 0L;
    private static boolean gameStarted = false;

    public EventTimers() {
        super("EventTimers", Module.Category.Bedwars);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Displays event timers.");
        this.addSlider(new SliderValue("Scale", 0.5, 1.5, 0.05, null, "scale", this, float.class));
        this.addExpand(new ExpandValue("Events", e -> {
            e.addToggle(new ToggleValue("Enabled", "eventTime", this));
            e.addToggle(new ToggleValue("Show next events only", "onlyNext", this));
            e.addToggle(new ToggleValue("Roman numerals", "romanNumerals", this));
            e.addToggle(new ToggleValue("Dynamic color", "eventDynamicColor", this));
            e.addCheck(new CheckValue("§bDiamond §7Timer", "diamondTimer", this));
            e.addCheck(new CheckValue("§2Emerald §7Timer", "emeraldTimer", this));
            e.addCheck(new CheckValue("§6Bed Gone §7Timer", "bedGoneTimer", this));
            e.addCheck(new CheckValue("§5Sudden Death §7Timer", "suddenDeathTimer", this));
            e.addCheck(new CheckValue("§cGame End §7Timer", "gameEndTimer", this));
        }));
        this.addExpand(new ExpandValue("Emeralds", e -> {
            e.addToggle(new ToggleValue("Enabled", "emeraldTime", this));
            e.addToggle(new ToggleValue("Dynamic color", "emeraldDynamicColor", this));
        }));
    }

    @EventTarget
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null) {
            if (this.mc.currentScreen == null || GuiUtil.inEditor()) {
                if (!Bedwars.GAME.isNotActive() && !Bedwars.PRE_GAME.isActive() || GuiUtil.inEditor()) {
                    if (!gameStarted) {
                        gameStartTime = System.currentTimeMillis();
                        gameStarted = true;
                    }

                    long now = System.currentTimeMillis();
                    int elapsedSeconds = (int)Math.max(0L, (now - gameStartTime) / 1000L);
                    if (this.eventTime) {
                        int x = this.eventPosX;
                        int y = this.eventPosY;
                        boolean diamondShown = false;
                        boolean emeraldShown = false;
                        int shown = 0;
                        String diamond2 = (this.eventDynamicColor ? EnumChatFormatting.AQUA : EnumChatFormatting.WHITE)
                            + "Diamond "
                            + EnumChatFormatting.WHITE
                            + (this.romanNumerals ? "II" : "2");
                        String diamond3 = (this.eventDynamicColor ? EnumChatFormatting.AQUA : EnumChatFormatting.WHITE)
                            + "Diamond "
                            + EnumChatFormatting.WHITE
                            + (this.romanNumerals ? "III" : "3");
                        String emerald2 = (this.eventDynamicColor ? EnumChatFormatting.DARK_GREEN : EnumChatFormatting.WHITE)
                            + "Emerald "
                            + EnumChatFormatting.WHITE
                            + (this.romanNumerals ? "II" : "2");
                        String emerald3 = (this.eventDynamicColor ? EnumChatFormatting.DARK_GREEN : EnumChatFormatting.WHITE)
                            + "Emerald "
                            + EnumChatFormatting.WHITE
                            + (this.romanNumerals ? "III" : "3");
                        String bedGone = (this.eventDynamicColor ? EnumChatFormatting.GOLD : EnumChatFormatting.WHITE) + "Bed Gone";
                        String suddenDeath = (this.eventDynamicColor ? EnumChatFormatting.DARK_PURPLE : EnumChatFormatting.WHITE) + "Sudden Death";
                        String gameEnd = (this.eventDynamicColor ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + "Game End";
                        EventTimers.EventEntry[] schedule = new EventTimers.EventEntry[]{
                            new EventTimers.EventEntry(new ItemStack(Items.diamond), diamond2, 360, EventTimers.EventType.DIAMOND),
                            new EventTimers.EventEntry(new ItemStack(Items.emerald), emerald2, 720, EventTimers.EventType.EMERALD),
                            new EventTimers.EventEntry(new ItemStack(Items.diamond), diamond3, 1080, EventTimers.EventType.DIAMOND),
                            new EventTimers.EventEntry(new ItemStack(Items.emerald), emerald3, 1440, EventTimers.EventType.EMERALD),
                            new EventTimers.EventEntry(new ItemStack(Items.bed), bedGone, 1800, EventTimers.EventType.BED_GONE),
                            new EventTimers.EventEntry(new ItemStack(Blocks.dragon_egg), suddenDeath, 2400, EventTimers.EventType.SUDDEN_DEATH),
                            new EventTimers.EventEntry(new ItemStack(Blocks.bedrock), gameEnd, 3000, EventTimers.EventType.GAME_END)
                        };

                        for (EventTimers.EventEntry entry : schedule) {
                            if (this.shouldShow(entry.type)) {
                                int remainingSeconds = entry.targetSeconds - elapsedSeconds;
                                if (remainingSeconds > 0) {
                                    if (entry.type == EventTimers.EventType.DIAMOND) {
                                        if (diamondShown) {
                                            continue;
                                        }

                                        diamondShown = true;
                                    }

                                    if (entry.type == EventTimers.EventType.EMERALD) {
                                        if (emeraldShown) {
                                            continue;
                                        }

                                        emeraldShown = true;
                                    }

                                    this.renderEventHud(entry.icon, entry.title, Util.formatTime(remainingSeconds), x, y, this.scale);
                                    y += Math.max((int)((this.mc.fontRendererObj.FONT_HEIGHT * 2 + 4) * this.scale), (int)(16.0F * this.scale))
                                        + (int)(4.0F * this.scale);
                                    if (this.onlyNext) {
                                        if (++shown >= 2) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (this.emeraldTime) {
                        EventTimers.EmeraldEntry modeData = this.getModeData();
                        if (modeData == null) {
                            return;
                        }

                        int nextSpawnTime = this.getNextSpawnTime(elapsedSeconds, modeData);
                        int nextEmeraldSpawn = Math.max(0, nextSpawnTime - elapsedSeconds);
                        int totalEmeralds = this.getTotalEmeralds(elapsedSeconds, modeData);
                        this.renderEmeraldHud(this.emeraldPosX, this.emeraldPosY, nextEmeraldSpawn, totalEmeralds);
                    }
                }
            }
        }
    }

    private void renderEventHud(ItemStack icon, String title, String time, int x, int y, float scale) {
        Render.renderItemIcon(icon, x, y, scale);
        Meowtils.drawString(title, (int)(x + 18.0F * scale), y, scale, -1);
        Meowtils.drawString(EnumChatFormatting.GRAY + time, (int)(x + 18.0F * scale), (int)(y + (this.mc.fontRendererObj.FONT_HEIGHT + 2) * scale), scale, -1);
    }

    private void renderEmeraldHud(int x, int y, int secondsUntilSpawn, int totalEmeralds) {
        int textX = (int)(x + 18.0F * this.scale);
        int lineTwoY = (int)(y + (this.mc.fontRendererObj.FONT_HEIGHT + 2) * this.scale);
        int textBlockHeight = (int)((this.mc.fontRendererObj.FONT_HEIGHT * 2 + 2) * this.scale);
        int iconSize = (int)(16.0F * this.scale);
        int iconY = y + Math.max(0, (textBlockHeight - iconSize) / 2);
        EnumChatFormatting timeColor = !this.emeraldDynamicColor
            ? EnumChatFormatting.GRAY
            : (
                secondsUntilSpawn < 3
                    ? EnumChatFormatting.DARK_RED
                    : (
                        secondsUntilSpawn < 5
                            ? EnumChatFormatting.RED
                            : (
                                secondsUntilSpawn < 9
                                    ? EnumChatFormatting.GOLD
                                    : (secondsUntilSpawn < 12 ? EnumChatFormatting.YELLOW : EnumChatFormatting.DARK_GREEN)
                            )
                    )
            );
        String mainText = "Next Emerald: " + timeColor + secondsUntilSpawn + EnumChatFormatting.GRAY + " s";
        String secondText = EnumChatFormatting.WHITE + "Total: " + EnumChatFormatting.DARK_GREEN + totalEmeralds;
        Render.renderItemIcon(EMERALD_ICON, x, iconY, this.scale);
        Meowtils.drawString(mainText, textX, y, this.scale, -1);
        Meowtils.drawString(secondText, textX, lineTwoY, this.scale, -1);
    }

    private boolean shouldShow(EventTimers.EventType type) {
        switch (type) {
            case DIAMOND:
                return this.diamondTimer;
            case EMERALD:
                return this.emeraldTimer;
            case BED_GONE:
                return this.bedGoneTimer;
            case SUDDEN_DEATH:
                return this.suddenDeathTimer;
            case GAME_END:
                return this.gameEndTimer;
            default:
                return true;
        }
    }

    private int getNextSpawnTime(int elapsedSeconds, EventTimers.EmeraldEntry modeData) {
        int nextSpawnTime = 31;

        while (elapsedSeconds >= nextSpawnTime) {
            nextSpawnTime += modeData.getSpawnInterval(nextSpawnTime);
        }

        return nextSpawnTime;
    }

    private int getTotalEmeralds(int elapsedSeconds, EventTimers.EmeraldEntry modeData) {
        int totalEmeralds = 0;
        int spawnTime = 31;

        while (elapsedSeconds >= spawnTime) {
            totalEmeralds += modeData.emeraldsPerSpawn;
            spawnTime += modeData.getSpawnInterval(spawnTime);
        }

        return totalEmeralds;
    }

    private EventTimers.EmeraldEntry getModeData() {
        if (Bedwars.THREES.isActive() || Bedwars.FOURS.isActive()) {
            return FOUR_TEAMS_MODE_DATA;
        } else {
            return !Bedwars.SOLOS.isActive() && !Bedwars.DOUBLES.isActive() ? FOUR_TEAMS_MODE_DATA : EIGHT_TEAMS_MODE_DATA;
        }
    }

    @Override
    public List<HudEntry> hudEditor() {
        return Arrays.asList(
            new HudEntry("EventTimers (Events)", this, "eventPosX", "eventPosY", () -> GuiUtil.getHudBounds("XXX Sudden Death", 11, this.scale)),
            new HudEntry("EventTimers (Emeralds)", this, "emeraldPosX", "emeraldPosY", () -> GuiUtil.getHudBounds("XXX Next Emerald: 99", 2, this.scale))
        );
    }

    @Override
    public void onReset() {
        gameStarted = false;
        gameStartTime = 0L;
    }

    private static class EmeraldEntry {
        final int tierOneInterval;
        final int tierTwoInterval;
        final int tierThreeInterval;
        final int emeraldsPerSpawn;

        EmeraldEntry(int tierOneInterval, int tierTwoInterval, int tierThreeInterval, int emeraldsPerSpawn) {
            this.tierOneInterval = tierOneInterval;
            this.tierTwoInterval = tierTwoInterval;
            this.tierThreeInterval = tierThreeInterval;
            this.emeraldsPerSpawn = emeraldsPerSpawn;
        }

        int getSpawnInterval(int elapsedSeconds) {
            if (elapsedSeconds >= 1440) {
                return this.tierThreeInterval;
            } else {
                return elapsedSeconds >= 720 ? this.tierTwoInterval : this.tierOneInterval;
            }
        }
    }

    private static class EventEntry {
        final ItemStack icon;
        final String title;
        final int targetSeconds;
        final EventTimers.EventType type;

        private EventEntry(ItemStack icon, String title, int targetSeconds, EventTimers.EventType type) {
            this.icon = icon;
            this.title = title;
            this.targetSeconds = targetSeconds;
            this.type = type;
        }
    }

    private static enum EventType {
        DIAMOND,
        EMERALD,
        BED_GONE,
        SUDDEN_DEATH,
        GAME_END;
    }
}
