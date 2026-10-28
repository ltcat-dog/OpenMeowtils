package wtf.tatp.meowtils.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.lists.FriendlistManager;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.module.meowtils.Teams;

public class TeamUtil {
    private static final Map<UUID, Long> TAB_JOIN_TIMES = new HashMap<>();
    private static final Set<UUID> CURRENT_TAB_LIST = new HashSet<>();
    private static final List<EntityPlayer> CACHED_TEAMMATES = new ArrayList<>();

    public static boolean ignoreFriends(String uuidOrName) {
        return FriendlistManager.isFriendlisted(uuidOrName) && Module.get(Teams.class).ignoreFriends;
    }

    public static boolean isTeam(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null || player == null) {
            return false;
        } else if (!Module.get(Teams.class).ignoreTeam) {
            return false;
        } else if (player == mc.thePlayer) {
            return false;
        } else if (player.capabilities.allowFlying) {
            return true;
        } else {
            try {
                if (CACHED_TEAMMATES.contains(player)) {
                    return true;
                }

                if (mc.thePlayer.isOnSameTeam(player)) {
                    return true;
                }

                if (Bedwars.ALL.isActive() && sameArmorColor(mc.thePlayer, player)) {
                    CACHED_TEAMMATES.add(player);
                    return true;
                }

                String selfFormatted = mc.thePlayer.getTeam() != null
                    ? mc.thePlayer.getTeam().formatString(mc.thePlayer.getName())
                    : mc.thePlayer.getName();
                String targetFormatted = player.getTeam() != null ? player.getTeam().formatString(player.getName()) : player.getName();
                String selfColor = getMostFrequentColor(selfFormatted);
                String targetColor = getMostFrequentColor(targetFormatted);
                if (selfColor != null && selfColor.equals(targetColor)) {
                    CACHED_TEAMMATES.add(player);
                    return true;
                }
            } catch (Exception var6) {
            }

            return false;
        }
    }

    private static String getMostFrequentColor(String text) {
        if (text == null) {
            return null;
        } else {
            Map<String, Integer> colorCounts = new HashMap<>();

            for (int i = 0; i < text.length() - 1; i++) {
                char c = text.charAt(i);
                if (c == 167) {
                    char code = text.charAt(i + 1);
                    if (code >= '0' && code <= '9' || code >= 'a' && code <= 'f') {
                        String currentColor = "§" + code;
                        colorCounts.put(currentColor, colorCounts.getOrDefault(currentColor, 0) + 1);
                    }
                }
            }

            return colorCounts.entrySet().stream().max(Entry.comparingByValue()).map(Entry::getKey).orElse(null);
        }
    }

    private static Integer getArmorColor(EntityPlayer player) {
        if (player == null) {
            return null;
        } else {
            ItemStack stack = player.getCurrentArmor(2);
            if (stack != null && stack.getItem() instanceof ItemArmor) {
                ItemArmor armor = (ItemArmor)stack.getItem();
                return armor.getArmorMaterial() != ArmorMaterial.LEATHER ? null : armor.getColor(stack);
            } else {
                return null;
            }
        }
    }

    private static boolean sameArmorColor(EntityPlayer a, EntityPlayer b) {
        Integer colorA = getArmorColor(a);
        Integer colorB = getArmorColor(b);
        return colorA != null && colorA.equals(colorB);
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.theWorld != null && event.getPhase() == ClientTickEvent.Phase.POST) {
            if (Module.get(Teams.class).ignoreBotMode.equals("Universal")) {
                updateTabList();
            }
        }
    }

    private static void updateTabList() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getNetHandler() != null) {
            Set<UUID> newTabList = new HashSet<>();

            for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
                newTabList.add(info.getGameProfile().getId());
            }

            long now = System.currentTimeMillis();

            for (UUID id : newTabList) {
                TAB_JOIN_TIMES.putIfAbsent(id, now);
            }

            TAB_JOIN_TIMES.keySet().removeIf(idx -> !newTabList.contains(idx));
            CURRENT_TAB_LIST.clear();
            CURRENT_TAB_LIST.addAll(newTabList);
        }
    }

    public static boolean isBot(EntityPlayer player) {
        Teams t = Module.get(Teams.class);
        if (player == null) {
            return true;
        } else if (t.ignoreBotMode.equals("None")) {
            return false;
        } else if (player == Minecraft.getMinecraft().thePlayer) {
            return false;
        } else {
            UUID id = player.getUniqueID();
            boolean useTabCheck = t.ignoreBotMode.equals("Universal") || t.ignoreBotMode.equals("Dynamic") && Server.HYPIXEL.isNotActive();
            boolean useUuidCheck = t.ignoreBotMode.equals("Hypixel") || t.ignoreBotMode.equals("Dynamic") && Server.HYPIXEL.isActive();
            if (useTabCheck) {
                if (!CURRENT_TAB_LIST.contains(id)) {
                    return true;
                } else {
                    Long joinTime = TAB_JOIN_TIMES.get(id);
                    if (joinTime == null) {
                        return true;
                    } else {
                        long timeInTab = System.currentTimeMillis() - joinTime;
                        return timeInTab < 10000L;
                    }
                }
            } else {
                return !useUuidCheck ? true : id.version() != 1 && id.version() != 4;
            }
        }
    }

    public static void reset() {
        CURRENT_TAB_LIST.clear();
        TAB_JOIN_TIMES.clear();
        CACHED_TEAMMATES.clear();
    }
}
