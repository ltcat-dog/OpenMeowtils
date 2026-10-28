package wtf.tatp.meowtils.module.hypixel;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.TextValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.lists.UrchinManager;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.stats.StatsCache;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.stats.StatsManager;
import wtf.tatp.meowtils.stats.StatsSource;
import wtf.tatp.meowtils.stats.api.abyss.AbyssPlayer;
import wtf.tatp.meowtils.stats.api.hypixel.HypixelPlayer;
import wtf.tatp.meowtils.stats.api.hypixel.HypixelRecent;
import wtf.tatp.meowtils.stats.api.hypixel.HypixelStatus;
import wtf.tatp.meowtils.stats.api.urchin.UrchinPlayer;
import wtf.tatp.meowtils.stats.util.ChatStats;
import wtf.tatp.meowtils.util.DelayedTask;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.PlayerUtil;

public class Stats extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public String api = "Abyss";
    @Config
    public String apiKey = "";
    @Config
    public int cache = 30;
    @Config
    public int cooldown = 100;
    @Config
    public String displayMode = "Compact";
    @Config
    public boolean tablist = true;
    @Config
    public boolean nametag = true;
    @Config
    public boolean chat = true;
    @Config
    public boolean autoCheck = true;
    @Config
    public boolean useTeamColor = false;
    @Config
    public boolean urchinApi = true;
    @Config
    public boolean urchinIgnoreSelf = false;
    @Config
    public boolean urchinIcon = true;
    @Config
    public boolean urchinChat = true;
    @Config
    public String urchinApiKey = "";
    @Config
    public boolean bedwars = true;
    @Config
    public boolean bedwarsLevel = true;
    @Config
    public boolean bedwarsKills = false;
    @Config
    public boolean bedwarsFinals = false;
    @Config
    public boolean bedwarsFkdr = true;
    @Config
    public boolean bedwarsWlr = false;
    @Config
    public boolean bedwarsWs = true;
    @Config
    public boolean bedwarsCr = false;
    @Config
    public boolean skywars = true;
    @Config
    public boolean skywarsLevel = true;
    @Config
    public boolean skywarsKills = false;
    @Config
    public boolean skywarsWins = false;
    @Config
    public boolean skywarsKdr = true;
    @Config
    public boolean skywarsWlr = true;
    private static final Pattern PLAYER_PATTERN = Pattern.compile("(?:\\[[^]]+]\\s+|§7)([A-Za-z0-9_]{1,16})");
    private static final StatsSource HYPIXEL_PLAYER = new HypixelPlayer();
    private static final StatsSource HYPIXEL_RECENT = new HypixelRecent();
    private static final StatsSource HYPIXEL_STATUS = new HypixelStatus();
    private static final StatsSource URCHIN_PLAYER = new UrchinPlayer();
    private static final StatsSource ABYSS = new AbyssPlayer();

    public Stats() {
        super("Stats", Module.Category.Hypixel);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Displays certain stats of players.");
        this.addMode(new ModeValue("API", Arrays.asList("Hypixel", "Abyss"), "api", this));
        this.addButton(new ButtonValue("Clear cache", 5.0F, () -> {
            StatsCache.clearCache();
            if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
                Meowtils.addMessage("Cleared stats cache.");
            }

            if (Notifications.getMode() != Notifications.Mode.CHAT) {
                NotificationManager.show("Stats", "Cleared cache.", NotificationManager.Type.INFO, 1500L);
            }
        }));
        this.addSlider(new SliderValue("Cache duration", 5.0, 60.0, 5.0, "min", "cache", this, int.class));
        this.addSlider(new SliderValue("Fetch cooldown", 0.0, 2000.0, 50.0, "ms", "cooldown", this, int.class));
        this.addMode(new ModeValue("Display mode", Arrays.asList("Full", "Compact", "Lowercase"), "displayMode", this));
        this.addExpand(new ExpandValue("Display", e -> {
            e.addToggle(new ToggleValue("Tablist", "tablist", this));
            e.addToggle(new ToggleValue("Nametags", "nametag", this));
            e.addToggle(new ToggleValue("Chat", "chat", this));
        }));
        this.addExpand(new ExpandValue("Chat options", e -> {
            e.addToggle(new ToggleValue("Auto-check certain players", "autoCheck", this));
            e.addToggle(new ToggleValue("Use team colors", "useTeamColor", this));
        }));
        this.addExpand(new ExpandValue("Urchin", e -> {
            e.addToggle(new ToggleValue("Check Urchin API", "urchinApi", this));
            e.addToggle(new ToggleValue("Ignore self", "urchinIgnoreSelf", this));
            e.addCheck(new CheckValue("Show name icons", "urchinIcon", this));
            e.addCheck(new CheckValue("Show in chat", "urchinChat", this));
            e.addText(new TextValue("API key", "Urchin API key", "urchinApiKey", this));
        }));
        this.addExpand(new ExpandValue("Bedwars", e -> {
            e.addToggle(new ToggleValue("Enabled", "bedwars", this));
            e.addCheck(new CheckValue("Level", "bedwarsLevel", this));
            e.addCheck(new CheckValue("Final kills", "bedwarsFinals", this));
            e.addCheck(new CheckValue("FKDR", "bedwarsFkdr", this));
            e.addCheck(new CheckValue("WLR", "bedwarsWlr", this));
            e.addCheck(new CheckValue("Winstreak", "bedwarsWs", this));
            e.addCheck(new CheckValue("Clutch ratio", "bedwarsCr", this));
        }));
        this.addExpand(new ExpandValue("Skywars", e -> {
            e.addToggle(new ToggleValue("Enabled", "skywars", this));
            e.addCheck(new CheckValue("Level", "skywarsLevel", this));
            e.addCheck(new CheckValue("Kills", "skywarsKills", this));
            e.addCheck(new CheckValue("Wins", "skywarsWins", this));
            e.addCheck(new CheckValue("KDR", "skywarsKdr", this));
            e.addCheck(new CheckValue("WLR", "skywarsWlr", this));
        }));
    }

    public static StatsSource getStatsSource() {
        String mode = Module.get(Stats.class).api;
        switch (mode) {
            case "Hypixel":
                return HYPIXEL_PLAYER;
            case "Abyss":
                return ABYSS;
            default:
                return null;
        }
    }

    public static void getStats(String name, StatsManager.Callback callback) {
        try {
            StatsSource source = getStatsSource();
            if (source != null) {
                StatsManager.request(name, source, callback);
            } else {
                Meowtils.addMessage(EnumChatFormatting.RED + "Stats source is invalid.");
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    public static void getRecent(String uuid, StatsManager.Callback callback) {
        try {
            StatsManager.request(uuid, HYPIXEL_RECENT, callback);
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    public static void getStatus(String uuid, StatsManager.Callback callback) {
        try {
            StatsManager.request(uuid, HYPIXEL_STATUS, callback);
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    public static StatsContainer getCached(String name) {
        StatsSource source = getStatsSource();
        if (source == null) {
            return null;
        } else {
            String key = source.getId() + ":" + name.toLowerCase();
            return StatsCache.getValid(key);
        }
    }

    public static void checkUrchin(String name, boolean manualCheck) {
        Minecraft mc = Minecraft.getMinecraft();
        Stats s = Module.get(Stats.class);
        if (s.urchinApi && name != null && !name.isEmpty() && !isNicked(name)) {
            if (!name.equals(mc.thePlayer.getName()) || !s.urchinIgnoreSelf || manualCheck) {
                StatsContainer cached = getCachedUrchin(name);
                String msg = "This player is not in the " + EnumChatFormatting.DARK_PURPLE + "Urchin" + EnumChatFormatting.WHITE + " blacklist.";
                if (cached == null) {
                    requestUrchin(name, stats -> {
                        if (stats != null) {
                            if (stats.urchinTags != null && !stats.urchinTags.isEmpty()) {
                                sendUrchinAlert(name, stats, manualCheck);
                            } else {
                                if (manualCheck) {
                                    Meowtils.addMessage(msg);
                                }
                            }
                        }
                    });
                } else if (cached.urchinTags != null && !cached.urchinTags.isEmpty()) {
                    sendUrchinAlert(name, cached, manualCheck);
                } else {
                    if (manualCheck) {
                        Meowtils.addMessage(msg);
                    }
                }
            }
        }
    }

    public static void requestUrchin(String name, StatsManager.Callback callback) {
        if (name != null && !name.isEmpty()) {
            try {
                StatsManager.request(name, URCHIN_PLAYER, callback);
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        } else {
            callback.call(null);
        }
    }

    public static StatsContainer getCachedUrchin(String name) {
        if (name != null && !name.isEmpty()) {
            StatsContainer cached = StatsCache.getValid(URCHIN_PLAYER.getId() + ":" + name.toLowerCase());
            if (cached != null) {
                return cached;
            } else {
                UrchinManager.Entry saved = UrchinManager.get(name);
                if (saved != null && saved.tags != null && !saved.tags.isEmpty()) {
                    StatsContainer container = new StatsContainer();
                    container.updateUrchinTags(saved.tags);
                    return container;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    @EventTarget
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.getType() == 0) {
            if (this.mc.thePlayer != null) {
                if (!Server.HYPIXEL.isNotActive()) {
                    String msg = event.getComponent().getUnformattedText();
                    if (Bedwars.GAME.isActive() && msg.startsWith("ONLINE: ")) {
                        try {
                            String[] players = msg.substring(8).split(", ");

                            for (String player : players) {
                                if (this.chat) {
                                    ChatStats.showBedwarsStats(player, false, true);
                                }

                                checkUrchin(player, false);
                            }
                        } catch (Exception var12) {
                            Meowtils.error("Unable to parse who message: " + var12);
                        }
                    }

                    if ((Skywars.GAME.isActive() || Skywars.MINI.isActive()) && msg.startsWith("Team #")) {
                        try {
                            String[] players = msg.substring(msg.indexOf(": ") + 2).split(", ");

                            for (String player : players) {
                                if (this.chat) {
                                    ChatStats.showSkywarsStats(player, false, true);
                                }

                                checkUrchin(player, false);
                            }
                        } catch (Exception var11) {
                            Meowtils.error("Unable to parse who message: " + var11);
                        }
                    }

                    if (this.autoCheck) {
                        if (msg.contains("Guild >>") || msg.contains("Party >>")) {
                            return;
                        }

                        Matcher playerMatcher = PLAYER_PATTERN.matcher(msg);
                        Matcher playerMatcherFirst = PLAYER_PATTERN.matcher(msg);
                        boolean isPartyFormat = (msg.contains("Party Members") || msg.contains("Party Leader") || msg.contains("Party Moderators"))
                            && !msg.contains("summoned");

                        try {
                            if (Bedwars.ALL.isActive() && isPartyFormat) {
                                while (playerMatcher.find()) {
                                    String name = playerMatcher.group(1);
                                    new DelayedTask(() -> ChatStats.showBedwarsStats(name, false, false), 10);
                                }
                            } else if (Skywars.ALL.isActive() && isPartyFormat) {
                                while (playerMatcher.find()) {
                                    String name = playerMatcher.group(1);
                                    new DelayedTask(() -> ChatStats.showSkywarsStats(name, false, false), 10);
                                }
                            }
                        } catch (Exception var10) {
                            var10.printStackTrace();
                        }

                        if (msg.contains(this.mc.thePlayer.getName()) && !msg.contains("*") && msg.contains(":")) {
                            try {
                                while (playerMatcherFirst.find()) {
                                    String name = playerMatcherFirst.group(1);
                                    if (!name.equalsIgnoreCase(this.mc.thePlayer.getName())) {
                                        if (isNicked(name)) {
                                            Meowtils.addMessage(
                                                EnumChatFormatting.GOLD + name + EnumChatFormatting.GRAY + " mentioned your name but is nicked!"
                                            );
                                        } else if (Bedwars.ALL.isActive()) {
                                            ChatStats.showBedwarsStats(name, false, false);
                                        } else if (Skywars.ALL.isActive()) {
                                            ChatStats.showSkywarsStats(name, false, false);
                                        }
                                    }
                                }
                            } catch (Exception var9) {
                                var9.printStackTrace();
                            }
                        }

                        if (Bedwars.PRE_GAME.isActive()) {
                            try {
                                while (playerMatcherFirst.find()) {
                                    String name = playerMatcherFirst.group(1);
                                    if (!name.equalsIgnoreCase(this.mc.thePlayer.getName())) {
                                        if (isNicked(name)) {
                                            Meowtils.addMessage(EnumChatFormatting.GOLD + name + EnumChatFormatting.GRAY + " talked in pre-game but is nicked!");
                                        } else {
                                            ChatStats.showBedwarsStats(name, false, true);
                                        }
                                    }
                                }
                            } catch (Exception var8) {
                                var8.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private static void sendUrchinAlert(String name, StatsContainer stats, boolean isManual) {
        String tagNames = stats.urchinTags.stream().map(tag -> formatUrchinTag(tag.type)).collect(Collectors.joining(", "));
        String reasons = stats.urchinTags
            .stream()
            .map(tag -> tag.reason != null && !tag.reason.isEmpty() ? tag.reason : "Unknown")
            .collect(Collectors.joining(", "));
        if (name != null && !name.isEmpty()) {
            Meowtils.addMessage(
                NameUtil.getTabDisplayName(name)
                    + EnumChatFormatting.GRAY
                    + " is tagged on "
                    + EnumChatFormatting.DARK_PURPLE
                    + "Urchin"
                    + EnumChatFormatting.GRAY
                    + " as "
                    + EnumChatFormatting.RED
                    + tagNames
            );
            Meowtils.addMessage(EnumChatFormatting.GRAY + "Reason: " + EnumChatFormatting.RED + reasons);
            if (!isManual) {
                PartyNotifier.urchin(name, tagNames);
            }
        } else {
            Meowtils.addMessage(EnumChatFormatting.RED + "Unable to get Urchin information.");
        }
    }

    private static String formatUrchinTag(String tag) {
        return tag == null ? "unknown" : tag.replace('_', ' ');
    }

    private static boolean isNicked(String name) {
        return PlayerUtil.isNicked(PlayerUtil.getProfile(name));
    }
}
