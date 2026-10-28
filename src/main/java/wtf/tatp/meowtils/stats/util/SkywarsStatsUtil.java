package wtf.tatp.meowtils.stats.util;

import net.minecraft.util.EnumChatFormatting;

public class SkywarsStatsUtil {
    public static EnumChatFormatting getKillsColor(int kills) {
        if (kills == 0) {
            return EnumChatFormatting.BLUE;
        } else if (kills < 1000) {
            return EnumChatFormatting.GRAY;
        } else if (kills < 2000) {
            return EnumChatFormatting.WHITE;
        } else if (kills < 3000) {
            return EnumChatFormatting.GREEN;
        } else if (kills < 5000) {
            return EnumChatFormatting.DARK_GREEN;
        } else if (kills < 15000) {
            return EnumChatFormatting.YELLOW;
        } else if (kills < 30000) {
            return EnumChatFormatting.GOLD;
        } else if (kills < 50000) {
            return EnumChatFormatting.RED;
        } else if (kills < 70000) {
            return EnumChatFormatting.DARK_RED;
        } else {
            return kills < 100000 ? EnumChatFormatting.LIGHT_PURPLE : EnumChatFormatting.DARK_PURPLE;
        }
    }

    public static EnumChatFormatting getKdrColor(double kdr) {
        if (kdr == 0.0) {
            return EnumChatFormatting.BLUE;
        } else if (kdr < 0.3) {
            return EnumChatFormatting.GRAY;
        } else if (kdr < 0.9) {
            return EnumChatFormatting.WHITE;
        } else if (kdr < 1.5) {
            return EnumChatFormatting.GREEN;
        } else if (kdr < 2.0) {
            return EnumChatFormatting.DARK_GREEN;
        } else if (kdr < 3.0) {
            return EnumChatFormatting.YELLOW;
        } else if (kdr < 6.0) {
            return EnumChatFormatting.GOLD;
        } else if (kdr < 9.0) {
            return EnumChatFormatting.RED;
        } else if (kdr < 15.0) {
            return EnumChatFormatting.DARK_RED;
        } else {
            return kdr < 30.0 ? EnumChatFormatting.LIGHT_PURPLE : EnumChatFormatting.DARK_PURPLE;
        }
    }

    public static EnumChatFormatting getWinsColor(int wins) {
        if (wins == 0) {
            return EnumChatFormatting.BLUE;
        } else if (wins < 100) {
            return EnumChatFormatting.GRAY;
        } else if (wins < 500) {
            return EnumChatFormatting.WHITE;
        } else if (wins < 1000) {
            return EnumChatFormatting.GREEN;
        } else if (wins < 1500) {
            return EnumChatFormatting.DARK_GREEN;
        } else if (wins < 3000) {
            return EnumChatFormatting.YELLOW;
        } else if (wins < 5000) {
            return EnumChatFormatting.GOLD;
        } else if (wins < 8000) {
            return EnumChatFormatting.RED;
        } else {
            return wins < 10000 ? EnumChatFormatting.DARK_RED : EnumChatFormatting.LIGHT_PURPLE;
        }
    }

    public static EnumChatFormatting getWlrColor(double wlr) {
        if (wlr == 0.0) {
            return EnumChatFormatting.BLUE;
        } else if (wlr < 0.1) {
            return EnumChatFormatting.GRAY;
        } else if (wlr < 0.5) {
            return EnumChatFormatting.WHITE;
        } else if (wlr < 1.0) {
            return EnumChatFormatting.GREEN;
        } else if (wlr < 2.0) {
            return EnumChatFormatting.DARK_GREEN;
        } else if (wlr < 3.0) {
            return EnumChatFormatting.YELLOW;
        } else if (wlr < 4.0) {
            return EnumChatFormatting.GOLD;
        } else if (wlr < 7.0) {
            return EnumChatFormatting.RED;
        } else if (wlr < 10.0) {
            return EnumChatFormatting.DARK_RED;
        } else {
            return wlr < 15.0 ? EnumChatFormatting.LIGHT_PURPLE : EnumChatFormatting.DARK_PURPLE;
        }
    }
}
