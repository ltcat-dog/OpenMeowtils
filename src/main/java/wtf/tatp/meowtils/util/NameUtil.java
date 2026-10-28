package wtf.tatp.meowtils.util;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;

public class NameUtil {
    public static String getTabDisplayName(String playerName) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null && mc.theWorld.getScoreboard() != null) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScorePlayerTeam team = scoreboard.getPlayersTeam(playerName);
            return team != null ? team.getColorPrefix() + playerName + team.getColorSuffix() : playerName;
        } else {
            return playerName;
        }
    }

    public static EnumChatFormatting getNameColor(String formattedName) {
        if (formattedName != null && !formattedName.isEmpty()) {
            for (int i = 0; i < formattedName.length() - 1; i++) {
                if (formattedName.charAt(i) == 167) {
                    char colorCode = formattedName.charAt(i + 1);
                    return ColorUtil.getColorFromCode(colorCode);
                }
            }

            return EnumChatFormatting.WHITE;
        } else {
            return EnumChatFormatting.GRAY;
        }
    }
}
