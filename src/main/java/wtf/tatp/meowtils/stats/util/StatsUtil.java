package wtf.tatp.meowtils.stats.util;

import com.google.gson.JsonObject;
import net.minecraft.util.EnumChatFormatting;

public class StatsUtil {
    public static String getRank(JsonObject playerData) {
        if (playerData.has("rank") && !playerData.get("rank").isJsonNull()) {
            String rank = playerData.get("rank").getAsString();
            if (!rank.equals("NORMAL")) {
                if (rank.equals("STAFF")) {
                    return "ዞ";
                }

                if (rank.equals("YOUTUBER")) {
                    return "YOUTUBE";
                }
            }
        }

        if (playerData.has("monthlyPackageRank")
            && !playerData.get("monthlyPackageRank").isJsonNull()
            && "SUPERSTAR".equals(playerData.get("monthlyPackageRank").getAsString())) {
            return "MVP++";
        } else if (playerData.has("newPackageRank") && !playerData.get("newPackageRank").isJsonNull()) {
            String newRank = playerData.get("newPackageRank").getAsString();
            if ("VIP_PLUS".equals(newRank)) {
                return "VIP+";
            } else {
                return "MVP_PLUS".equals(newRank) ? "MVP+" : newRank;
            }
        } else {
            return playerData.has("packageRank") && !playerData.get("packageRank").isJsonNull() ? playerData.get("packageRank").getAsString() : "NONE";
        }
    }

    public static EnumChatFormatting getPlusColor(JsonObject playerData) {
        if (playerData.has("rankPlusColor") && !playerData.get("rankPlusColor").isJsonNull()) {
            try {
                String color = playerData.get("rankPlusColor").getAsString().toUpperCase();
                return EnumChatFormatting.valueOf(color);
            } catch (IllegalArgumentException var2) {
                return EnumChatFormatting.RED;
            }
        } else {
            return EnumChatFormatting.RED;
        }
    }

    public static EnumChatFormatting getRankColor(String rank) {
        switch (rank) {
            case "MVP":
            case "MVP+":
                return EnumChatFormatting.AQUA;
            case "MVP++":
                return EnumChatFormatting.GOLD;
            case "VIP":
            case "VIP+":
                return EnumChatFormatting.GREEN;
            case "YOUTUBE":
            case "ዞ":
                return EnumChatFormatting.RED;
            default:
                return EnumChatFormatting.GRAY;
        }
    }

    public static String getFormattedRankDisplay(String rank, EnumChatFormatting plusColor) {
        if ("ዞ".equals(rank)) {
            return EnumChatFormatting.RED + "[" + EnumChatFormatting.GOLD + "ዞ" + EnumChatFormatting.RED + "] ";
        } else if ("YOUTUBE".equals(rank)) {
            return EnumChatFormatting.RED + "[" + EnumChatFormatting.WHITE + "YOUTUBE" + EnumChatFormatting.RED + "] ";
        } else if ("MVP++".equals(rank)) {
            return EnumChatFormatting.GOLD + "[MVP" + plusColor + "++" + EnumChatFormatting.GOLD + "] ";
        } else if ("MVP+".equals(rank)) {
            return EnumChatFormatting.AQUA + "[MVP" + plusColor + "+" + EnumChatFormatting.AQUA + "] ";
        } else if ("VIP+".equals(rank)) {
            return EnumChatFormatting.GREEN + "[VIP" + EnumChatFormatting.GOLD + "+" + EnumChatFormatting.GREEN + "] ";
        } else {
            return !"NONE".equals(rank) ? getRankColor(rank) + "[" + rank + "] " : EnumChatFormatting.GRAY + " ";
        }
    }

    public static String formattedChannel(String channel) {
        switch (channel) {
            case "PARTY":
                return EnumChatFormatting.BLUE + "Party";
            case "ALL":
                return EnumChatFormatting.GREEN + "All";
            case "GUILD":
                return EnumChatFormatting.DARK_GREEN + "Guild";
            case "OFFICER":
                return EnumChatFormatting.DARK_AQUA + "Officer";
            case "Unknown":
                return EnumChatFormatting.RED + "Unknown";
            default:
                return channel;
        }
    }

    public static boolean isUnknown(String stat) {
        return stat.equals("Unknown");
    }

    public static JsonObject getObj(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonObject() ? object.getAsJsonObject(key) : new JsonObject();
    }

    public static boolean getBoolean(JsonObject object, String key, boolean fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsBoolean() : fallback;
    }

    public static String getString(JsonObject object, String key, String fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : fallback;
    }

    public static int getInt(JsonObject object, String key, int fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsInt() : fallback;
    }

    public static long getLong(JsonObject object, String key, long fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsLong() : fallback;
    }

    public static double getDouble(JsonObject object, String key, double fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsDouble() : fallback;
    }

    public static String extractValue(String html, String start, String end) {
        int startIndex = html.indexOf(start);
        if (startIndex == -1) {
            return "";
        } else {
            startIndex += start.length();
            int endIndex = html.indexOf(end, startIndex);
            return endIndex == -1 ? "" : html.substring(startIndex, endIndex).trim();
        }
    }
}
