package wtf.tatp.meowtils.module.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.gui.values.TextValue;
import wtf.tatp.meowtils.util.FileUtil;
import wtf.tatp.meowtils.util.ScoreboardUtil;
import wtf.tatp.meowtils.util.Util;

public class ChatFilter extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public String selectedFilter = "default";
    private static final File FILTER_DIR = Meowtils.CHAT_FILTER_DIR;
    private static final File DEFAULT_FILTER = Meowtils.DEFAULT_CHAT_FILTER;
    private static final Pattern AND_SPLIT = Pattern.compile(" & (?=[?=<>!])");
    private static List<ChatFilter.FilterRule> activeRules = Collections.emptyList();
    private static String allowedServer = null;
    private static long lastRefreshTime = 0L;
    private static boolean shouldFilter = false;
    private static final List<String> DEFAULT = Arrays.asList(
        "# Visit docs.tatp.wtf/modules/utility/chatfilter for full documentation.",
        "# # - comment, will ignore any text after this",
        "# ?meow - contains meow",
        "# =meow - equals meow",
        "# <meow - starts with meow",
        "# >meow - ends with meow",
        "# !<any operator> - reversed condition",
        "# ? - combines conditions",
        "# -server - server/game name in scoreboard, limits filter to this only",
        "-hypixel",
        "?You are still radiating with Generosity!",
        "?Your game was boosted by",
        "<You tipped",
        ">'s Network Booster)",
        "=You are AFK. Move around to return from AFK.",
        "?joined the lobby!",
        "=If you get disconnected use /rejoin to join back in the game.",
        "?Click here to watch the Replay!",
        "<Teaming is not allowed",
        "=[WATCHDOG ANNOUNCEMENT]",
        "?Watchdog has banned",
        "=Blacklisted modifications are a bannable offense!",
        "=Rate this map by clicking: [5] [4] [3] [2] [1]",
        "=Prepare your defenses!",
        "=Click with any sword or bow to activate your skill!",
        "=Resource pack not working? Type /resource to fix it!",
        "=All games in this lobby are currently in development.",
        "<Click here to leave feedback!",
        "<PIT! Latest update:",
        "<>>> [MVP++] & ?joined the lobby!",
        "<Staff have banned an additional & >in the last 7 days.",
        "=Cross-teaming is not allowed! Report cross-teamers using /report.",
        "=Teaming with the Murderer is not allowed!",
        "?Gain XP and coins by » CLICKING HERE! «",
        "=Command Failed: This command is on cooldown! Try again in about a second!",
        "=Buy Network Boosters at https://store.hypixel.net"
    );

    public ChatFilter() {
        super("ChatFilter", Module.Category.Utility);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Removes filtered messages.\n§d/meowfilter §f- Show commands");
        this.addText(new TextValue(null, "Filter name", "selectedFilter", this));
        this.addButton(new ButtonValue("Open folder", 5.0F, () -> Util.openFolder(FILTER_DIR, "filter")));
        this.addButton(new ButtonValue("Reload", 5.0F, () -> {
            reloadFilter();
            Meowtils.addMessage("Reloaded filter: " + EnumChatFormatting.GREEN.toString() + EnumChatFormatting.ITALIC + this.selectedFilter);
        }));
    }

    public static void init() {
        createDefault();
        reloadFilter();
    }

    public static void reloadFilter() {
        activeRules = parseFilter(loadFilter());
    }

    @EventTarget(
        priority = EventPriority.LOWEST
    )
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.getType() != 2) {
            if (!activeRules.isEmpty()) {
                if (shouldFilter) {
                    String msg = event.getComponent().getUnformattedText();

                    try {
                        for (ChatFilter.FilterRule rule : activeRules) {
                            if (rule.matches(msg)) {
                                event.setCancelled(true);
                            }
                        }
                    } catch (Exception var5) {
                        var5.printStackTrace();
                        Meowtils.addMessage(EnumChatFormatting.RED + "ChatFilter error, using: " + EnumChatFormatting.WHITE + this.selectedFilter);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onReceivePacket(ReceivePacketEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof S3BPacketScoreboardObjective
            || packet instanceof S3CPacketUpdateScore
            || packet instanceof S3DPacketDisplayScoreboard
            || packet instanceof S3EPacketTeams) {
            long now = System.currentTimeMillis();
            if (now - lastRefreshTime < 2000L) {
                return;
            }

            lastRefreshTime = now;

            try {
                if (allowedServer != null && !allowedServer.isEmpty() && !allowedServer.equalsIgnoreCase("all")) {
                    shouldFilter = ScoreboardUtil.lineContains(allowedServer) || ScoreboardUtil.titleContains(allowedServer);
                } else {
                    shouldFilter = true;
                }
            } catch (Exception var6) {
                var6.printStackTrace();
                Meowtils.addMessage(EnumChatFormatting.RED + "ChatFilter error, using: " + EnumChatFormatting.WHITE + this.selectedFilter);
            }
        }
    }

    private static File loadFilter() {
        ChatFilter c = Module.get(ChatFilter.class);
        if (c.selectedFilter == null) {
            c.selectedFilter = "default";
            ConfigManager.save();
        }

        File filter = new File(FILTER_DIR, c.selectedFilter.replace(".txt", "") + ".txt");
        if (filter.exists() && filter.isFile()) {
            return filter;
        } else {
            Meowtils.warn("Filter: " + c.selectedFilter + " does not exist, changing to default.");
            Meowtils.addMessage(
                EnumChatFormatting.RED
                    + "Filter does not exist. Changed to "
                    + EnumChatFormatting.WHITE.toString()
                    + EnumChatFormatting.ITALIC
                    + "default"
                    + EnumChatFormatting.RED
                    + " filter."
            );
            c.selectedFilter = "default";
            ConfigManager.save();
            createDefault();
            return DEFAULT_FILTER;
        }
    }

    private static ChatFilter.FilterRule parseRule(String line) {
        String[] parts = AND_SPLIT.split(line);
        List<ChatFilter.Condition> conditions = new ArrayList<>();

        for (String part : parts) {
            ChatFilter.Condition c = parseCondition(part.trim());
            if (c != null) {
                conditions.add(c);
            }
        }

        return conditions.isEmpty() ? null : new ChatFilter.FilterRule(conditions);
    }

    private static List<ChatFilter.FilterRule> parseFilter(File file) {
        List<ChatFilter.FilterRule> rules = new ArrayList<>();
        allowedServer = null;

        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    if (line.startsWith("-")) {
                        allowedServer = line.substring(1).trim();
                    } else if (!line.isEmpty()) {
                        ChatFilter.FilterRule rule = parseRule(line);
                        if (rule != null) {
                            rules.add(rule);
                        }
                    }
                }
            }
        } catch (IOException var16) {
            var16.printStackTrace();
        }

        return rules;
    }

    private static void createDefault() {
        if (!DEFAULT_FILTER.exists() || DEFAULT_FILTER.length() == 0L) {
            try {
                FileUtil.createDir(FILTER_DIR);

                try (BufferedWriter w = new BufferedWriter(new FileWriter(DEFAULT_FILTER))) {
                    for (String line : DEFAULT) {
                        w.write(line);
                        w.newLine();
                    }
                }
            } catch (IOException var14) {
                var14.printStackTrace();
            }
        }
    }

    private static ChatFilter.Condition parseCondition(String part) {
        if (part.isEmpty()) {
            return null;
        } else {
            boolean logicalNot = part.charAt(0) == '!';
            if (logicalNot) {
                part = part.substring(1);
            }

            if (part.isEmpty()) {
                return null;
            } else {
                char operator = part.charAt(0);
                String value = part.substring(1);
                switch (operator) {
                    case '<':
                        return new ChatFilter.Condition(ChatFilter.Condition.Type.STARTS_WITH, value, logicalNot);
                    case '=':
                        return new ChatFilter.Condition(ChatFilter.Condition.Type.EQUALS, value, logicalNot);
                    case '>':
                        return new ChatFilter.Condition(ChatFilter.Condition.Type.ENDS_WITH, value, logicalNot);
                    case '?':
                        return new ChatFilter.Condition(ChatFilter.Condition.Type.CONTAINS, value, logicalNot);
                    default:
                        return null;
                }
            }
        }
    }

    @Override
    public void onEnable() {
        reloadFilter();
    }

    private static class Condition {
        private final ChatFilter.Condition.Type type;
        private final String value;
        private final boolean logicalNot;

        Condition(ChatFilter.Condition.Type type, String value, boolean logicalNot) {
            this.type = type;
            this.value = value.toLowerCase();
            this.logicalNot = logicalNot;
        }

        final boolean matches(String message) {
            String msg = message.toLowerCase();
            boolean result;
            switch (this.type) {
                case CONTAINS:
                    result = msg.contains(this.value);
                    break;
                case EQUALS:
                    result = msg.equals(this.value);
                    break;
                case STARTS_WITH:
                    result = msg.startsWith(this.value);
                    break;
                case ENDS_WITH:
                    result = msg.endsWith(this.value);
                    break;
                default:
                    result = false;
            }

            return this.logicalNot != result;
        }

        static enum Type {
            CONTAINS,
            EQUALS,
            STARTS_WITH,
            ENDS_WITH;
        }
    }

    private static class FilterRule {
        private final List<ChatFilter.Condition> conditions;

        FilterRule(List<ChatFilter.Condition> conditions) {
            this.conditions = conditions;
        }

        final boolean matches(String message) {
            for (ChatFilter.Condition c : this.conditions) {
                if (!c.matches(message)) {
                    return false;
                }
            }

            return true;
        }
    }
}
