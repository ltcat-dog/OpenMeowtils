package wtf.tatp.meowtils.stats.util;

import net.minecraft.util.EnumChatFormatting;

public class BedwarsStatsUtil {
    public static EnumChatFormatting getFKDRColor(double fkdr) {
        if (fkdr == 0.0) {
            return EnumChatFormatting.BLUE;
        } else if (fkdr < 1.0) {
            return EnumChatFormatting.GRAY;
        } else if (fkdr < 3.0) {
            return EnumChatFormatting.WHITE;
        } else if (fkdr < 5.0) {
            return EnumChatFormatting.GREEN;
        } else if (fkdr < 7.0) {
            return EnumChatFormatting.DARK_GREEN;
        } else if (fkdr < 10.0) {
            return EnumChatFormatting.YELLOW;
        } else if (fkdr < 20.0) {
            return EnumChatFormatting.GOLD;
        } else if (fkdr < 30.0) {
            return EnumChatFormatting.RED;
        } else if (fkdr < 50.0) {
            return EnumChatFormatting.DARK_RED;
        } else {
            return fkdr < 100.0 ? EnumChatFormatting.LIGHT_PURPLE : EnumChatFormatting.DARK_PURPLE;
        }
    }

    public static EnumChatFormatting getWLRColor(double wlr) {
        if (wlr == 0.0) {
            return EnumChatFormatting.BLUE;
        } else if (wlr < 0.5) {
            return EnumChatFormatting.GRAY;
        } else if (wlr < 0.9) {
            return EnumChatFormatting.WHITE;
        } else if (wlr < 1.5) {
            return EnumChatFormatting.GREEN;
        } else if (wlr < 2.0) {
            return EnumChatFormatting.DARK_GREEN;
        } else if (wlr < 3.0) {
            return EnumChatFormatting.YELLOW;
        } else if (wlr < 6.0) {
            return EnumChatFormatting.GOLD;
        } else if (wlr < 9.0) {
            return EnumChatFormatting.RED;
        } else if (wlr < 15.0) {
            return EnumChatFormatting.DARK_RED;
        } else {
            return wlr < 30.0 ? EnumChatFormatting.LIGHT_PURPLE : EnumChatFormatting.DARK_PURPLE;
        }
    }

    public static EnumChatFormatting getWSColor(int ws) {
        if (ws == 0) {
            return EnumChatFormatting.BLUE;
        } else if (ws < 3) {
            return EnumChatFormatting.GRAY;
        } else if (ws < 5) {
            return EnumChatFormatting.WHITE;
        } else if (ws < 15) {
            return EnumChatFormatting.GREEN;
        } else if (ws < 20) {
            return EnumChatFormatting.DARK_GREEN;
        } else if (ws < 30) {
            return EnumChatFormatting.YELLOW;
        } else if (ws < 40) {
            return EnumChatFormatting.GOLD;
        } else if (ws < 50) {
            return EnumChatFormatting.RED;
        } else {
            return ws < 80 ? EnumChatFormatting.DARK_RED : EnumChatFormatting.LIGHT_PURPLE;
        }
    }

    public static EnumChatFormatting getFinalsColor(int finals) {
        if (finals == 0) {
            return EnumChatFormatting.BLUE;
        } else if (finals < 1000) {
            return EnumChatFormatting.GRAY;
        } else if (finals < 2000) {
            return EnumChatFormatting.WHITE;
        } else if (finals < 3000) {
            return EnumChatFormatting.GREEN;
        } else if (finals < 5000) {
            return EnumChatFormatting.DARK_GREEN;
        } else if (finals < 15000) {
            return EnumChatFormatting.YELLOW;
        } else if (finals < 30000) {
            return EnumChatFormatting.GOLD;
        } else if (finals < 50000) {
            return EnumChatFormatting.RED;
        } else if (finals < 70000) {
            return EnumChatFormatting.DARK_RED;
        } else {
            return finals < 100000 ? EnumChatFormatting.LIGHT_PURPLE : EnumChatFormatting.DARK_PURPLE;
        }
    }

    public static EnumChatFormatting getClutchRatioColor(double cr) {
        if (cr == 0.0) {
            return EnumChatFormatting.BLUE;
        } else if (cr < 0.01) {
            return EnumChatFormatting.GRAY;
        } else if (cr < 0.05) {
            return EnumChatFormatting.WHITE;
        } else if (cr < 0.1) {
            return EnumChatFormatting.GREEN;
        } else if (cr < 0.2) {
            return EnumChatFormatting.DARK_GREEN;
        } else if (cr < 0.3) {
            return EnumChatFormatting.YELLOW;
        } else if (cr < 0.4) {
            return EnumChatFormatting.GOLD;
        } else if (cr < 0.5) {
            return EnumChatFormatting.RED;
        } else if (cr < 0.6) {
            return EnumChatFormatting.DARK_RED;
        } else {
            return cr < 0.7 ? EnumChatFormatting.LIGHT_PURPLE : EnumChatFormatting.DARK_PURPLE;
        }
    }

    public static String getFormattedLevel(int level) {
        BedwarsStatsUtil.LevelFormat format = getFormatForLevel(level);
        String lvl = String.valueOf(level);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(format.leftBracketColor).append("[");

        for (int i = 0; i < lvl.length(); i++) {
            EnumChatFormatting color = i < format.digitColors.length ? format.digitColors[i] : format.digitColors[format.digitColors.length - 1];
            stringBuilder.append(color).append(lvl.charAt(i));
        }

        if (format.starColor != null) {
            stringBuilder.append(format.starColor);
        }

        if (format.boldStar) {
            stringBuilder.append(EnumChatFormatting.BOLD);
        }

        stringBuilder.append(format.starIcon);
        stringBuilder.append(EnumChatFormatting.RESET);
        stringBuilder.append(format.rightBracketColor).append("]");
        return stringBuilder.toString() + EnumChatFormatting.RESET;
    }

    private static BedwarsStatsUtil.LevelFormat getFormatForLevel(int level) {
        if (level < 100) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GRAY, new EnumChatFormatting[]{EnumChatFormatting.GRAY}, EnumChatFormatting.GRAY, "✫", EnumChatFormatting.GRAY, false
            );
        } else if (level < 200) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.WHITE, new EnumChatFormatting[]{EnumChatFormatting.WHITE}, EnumChatFormatting.WHITE, "✫", EnumChatFormatting.WHITE, false
            );
        } else if (level < 300) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GOLD, new EnumChatFormatting[]{EnumChatFormatting.GOLD}, EnumChatFormatting.GOLD, "✫", EnumChatFormatting.GOLD, false
            );
        } else if (level < 400) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.AQUA, new EnumChatFormatting[]{EnumChatFormatting.AQUA}, EnumChatFormatting.AQUA, "✫", EnumChatFormatting.AQUA, false
            );
        } else if (level < 500) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_GREEN,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_GREEN},
                EnumChatFormatting.DARK_GREEN,
                "✫",
                EnumChatFormatting.DARK_GREEN,
                false
            );
        } else if (level < 600) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_AQUA,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_AQUA},
                EnumChatFormatting.DARK_AQUA,
                "✫",
                EnumChatFormatting.DARK_AQUA,
                false
            );
        } else if (level < 700) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_RED,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_RED},
                EnumChatFormatting.DARK_RED,
                "✫",
                EnumChatFormatting.DARK_RED,
                false
            );
        } else if (level < 800) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.LIGHT_PURPLE,
                new EnumChatFormatting[]{EnumChatFormatting.LIGHT_PURPLE},
                EnumChatFormatting.LIGHT_PURPLE,
                "✫",
                EnumChatFormatting.LIGHT_PURPLE,
                false
            );
        } else if (level < 900) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.BLUE, new EnumChatFormatting[]{EnumChatFormatting.BLUE}, EnumChatFormatting.BLUE, "✫", EnumChatFormatting.BLUE, false
            );
        } else if (level < 1000) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_PURPLE,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_PURPLE},
                EnumChatFormatting.DARK_PURPLE,
                "✫",
                EnumChatFormatting.DARK_PURPLE,
                false
            );
        } else if (level < 1100) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.RED,
                new EnumChatFormatting[]{EnumChatFormatting.GOLD, EnumChatFormatting.YELLOW, EnumChatFormatting.GREEN, EnumChatFormatting.AQUA},
                EnumChatFormatting.LIGHT_PURPLE,
                "✫",
                EnumChatFormatting.DARK_PURPLE,
                false
            );
        } else if (level < 1200) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GRAY, new EnumChatFormatting[]{EnumChatFormatting.WHITE}, EnumChatFormatting.GRAY, "✪", EnumChatFormatting.GRAY, false
            );
        } else if (level < 1300) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GRAY, new EnumChatFormatting[]{EnumChatFormatting.YELLOW}, EnumChatFormatting.GOLD, "✪", EnumChatFormatting.GRAY, false
            );
        } else if (level < 1400) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GRAY, new EnumChatFormatting[]{EnumChatFormatting.AQUA}, EnumChatFormatting.DARK_AQUA, "✪", EnumChatFormatting.GRAY, false
            );
        } else if (level < 1500) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GRAY, new EnumChatFormatting[]{EnumChatFormatting.GREEN}, EnumChatFormatting.DARK_GREEN, "✪", EnumChatFormatting.GRAY, false
            );
        } else if (level < 1600) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GRAY, new EnumChatFormatting[]{EnumChatFormatting.DARK_AQUA}, EnumChatFormatting.BLUE, "✪", EnumChatFormatting.GRAY, false
            );
        } else if (level < 1700) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GRAY, new EnumChatFormatting[]{EnumChatFormatting.RED}, EnumChatFormatting.DARK_RED, "✪", EnumChatFormatting.GRAY, false
            );
        } else if (level < 1800) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GRAY,
                new EnumChatFormatting[]{EnumChatFormatting.LIGHT_PURPLE},
                EnumChatFormatting.DARK_PURPLE,
                "✪",
                EnumChatFormatting.GRAY,
                false
            );
        } else if (level < 1900) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GRAY, new EnumChatFormatting[]{EnumChatFormatting.BLUE}, EnumChatFormatting.DARK_BLUE, "✪", EnumChatFormatting.GRAY, false
            );
        } else if (level < 2000) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GRAY,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_PURPLE},
                EnumChatFormatting.DARK_GRAY,
                "✪",
                EnumChatFormatting.GRAY,
                false
            );
        } else if (level < 2100) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_GRAY,
                new EnumChatFormatting[]{EnumChatFormatting.GRAY, EnumChatFormatting.WHITE, EnumChatFormatting.WHITE, EnumChatFormatting.GRAY},
                EnumChatFormatting.GRAY,
                "✪",
                EnumChatFormatting.DARK_GRAY,
                false
            );
        } else if (level < 2200) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.WHITE,
                new EnumChatFormatting[]{EnumChatFormatting.WHITE, EnumChatFormatting.YELLOW, EnumChatFormatting.YELLOW, EnumChatFormatting.GOLD},
                EnumChatFormatting.GOLD,
                "⚝",
                EnumChatFormatting.GOLD,
                true
            );
        } else if (level < 2300) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GOLD,
                new EnumChatFormatting[]{EnumChatFormatting.GOLD, EnumChatFormatting.WHITE, EnumChatFormatting.WHITE, EnumChatFormatting.AQUA},
                EnumChatFormatting.DARK_AQUA,
                "⚝",
                EnumChatFormatting.DARK_AQUA,
                true
            );
        } else if (level < 2400) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_PURPLE,
                new EnumChatFormatting[]{
                    EnumChatFormatting.DARK_PURPLE, EnumChatFormatting.LIGHT_PURPLE, EnumChatFormatting.LIGHT_PURPLE, EnumChatFormatting.GOLD
                },
                EnumChatFormatting.YELLOW,
                "⚝",
                EnumChatFormatting.YELLOW,
                true
            );
        } else if (level < 2500) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.AQUA,
                new EnumChatFormatting[]{EnumChatFormatting.AQUA, EnumChatFormatting.WHITE, EnumChatFormatting.WHITE, EnumChatFormatting.GRAY},
                EnumChatFormatting.GRAY,
                "⚝",
                EnumChatFormatting.DARK_GRAY,
                true
            );
        } else if (level < 2600) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.WHITE,
                new EnumChatFormatting[]{EnumChatFormatting.WHITE, EnumChatFormatting.GREEN, EnumChatFormatting.GREEN, EnumChatFormatting.DARK_GREEN},
                EnumChatFormatting.DARK_GREEN,
                "⚝",
                EnumChatFormatting.DARK_GREEN,
                true
            );
        } else if (level < 2700) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_RED,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_RED, EnumChatFormatting.RED, EnumChatFormatting.RED, EnumChatFormatting.LIGHT_PURPLE},
                EnumChatFormatting.LIGHT_PURPLE,
                "⚝",
                EnumChatFormatting.DARK_PURPLE,
                true
            );
        } else if (level < 2800) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.YELLOW,
                new EnumChatFormatting[]{EnumChatFormatting.YELLOW, EnumChatFormatting.WHITE, EnumChatFormatting.WHITE, EnumChatFormatting.DARK_GRAY},
                EnumChatFormatting.DARK_GRAY,
                "⚝",
                EnumChatFormatting.DARK_GRAY,
                true
            );
        } else if (level < 2900) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GREEN,
                new EnumChatFormatting[]{EnumChatFormatting.GREEN, EnumChatFormatting.DARK_GREEN, EnumChatFormatting.DARK_GREEN, EnumChatFormatting.GOLD},
                EnumChatFormatting.GOLD,
                "⚝",
                EnumChatFormatting.YELLOW,
                true
            );
        } else if (level < 3000) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.AQUA,
                new EnumChatFormatting[]{EnumChatFormatting.AQUA, EnumChatFormatting.DARK_AQUA, EnumChatFormatting.DARK_AQUA, EnumChatFormatting.BLUE},
                EnumChatFormatting.BLUE,
                "⚝",
                EnumChatFormatting.DARK_BLUE,
                true
            );
        } else if (level < 3100) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.YELLOW,
                new EnumChatFormatting[]{EnumChatFormatting.YELLOW, EnumChatFormatting.GOLD, EnumChatFormatting.GOLD, EnumChatFormatting.RED},
                EnumChatFormatting.RED,
                "⚝",
                EnumChatFormatting.DARK_RED,
                true
            );
        } else if (level < 3200) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.BLUE,
                new EnumChatFormatting[]{EnumChatFormatting.BLUE, EnumChatFormatting.DARK_AQUA, EnumChatFormatting.DARK_AQUA, EnumChatFormatting.GOLD},
                EnumChatFormatting.GOLD,
                "✥",
                EnumChatFormatting.YELLOW,
                false
            );
        } else if (level < 3300) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.RED,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_RED, EnumChatFormatting.GRAY, EnumChatFormatting.GRAY, EnumChatFormatting.DARK_RED},
                EnumChatFormatting.RED,
                "✥",
                EnumChatFormatting.RED,
                false
            );
        } else if (level < 3400) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.BLUE,
                new EnumChatFormatting[]{EnumChatFormatting.BLUE, EnumChatFormatting.BLUE, EnumChatFormatting.LIGHT_PURPLE, EnumChatFormatting.RED},
                EnumChatFormatting.RED,
                "✥",
                EnumChatFormatting.DARK_RED,
                false
            );
        } else if (level < 3500) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_GREEN,
                new EnumChatFormatting[]{
                    EnumChatFormatting.GREEN, EnumChatFormatting.LIGHT_PURPLE, EnumChatFormatting.LIGHT_PURPLE, EnumChatFormatting.DARK_PURPLE
                },
                EnumChatFormatting.DARK_PURPLE,
                "✥",
                EnumChatFormatting.DARK_GREEN,
                false
            );
        } else if (level < 3600) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.RED,
                new EnumChatFormatting[]{EnumChatFormatting.RED, EnumChatFormatting.DARK_RED, EnumChatFormatting.DARK_RED, EnumChatFormatting.DARK_GREEN},
                EnumChatFormatting.GREEN,
                "✥",
                EnumChatFormatting.GREEN,
                false
            );
        } else if (level < 3700) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.GREEN,
                new EnumChatFormatting[]{EnumChatFormatting.GREEN, EnumChatFormatting.GREEN, EnumChatFormatting.AQUA, EnumChatFormatting.BLUE},
                EnumChatFormatting.BLUE,
                "✥",
                EnumChatFormatting.DARK_BLUE,
                false
            );
        } else if (level < 3800) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_RED,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_RED, EnumChatFormatting.RED, EnumChatFormatting.RED, EnumChatFormatting.AQUA},
                EnumChatFormatting.DARK_AQUA,
                "✥",
                EnumChatFormatting.DARK_AQUA,
                false
            );
        } else if (level < 3900) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_BLUE,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_BLUE, EnumChatFormatting.BLUE, EnumChatFormatting.DARK_PURPLE, EnumChatFormatting.DARK_PURPLE},
                EnumChatFormatting.LIGHT_PURPLE,
                "✥",
                EnumChatFormatting.DARK_BLUE,
                false
            );
        } else if (level < 4000) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.RED,
                new EnumChatFormatting[]{EnumChatFormatting.RED, EnumChatFormatting.GREEN, EnumChatFormatting.GREEN, EnumChatFormatting.DARK_AQUA},
                EnumChatFormatting.BLUE,
                "✥",
                EnumChatFormatting.BLUE,
                false
            );
        } else if (level < 4100) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_PURPLE,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_PURPLE, EnumChatFormatting.RED, EnumChatFormatting.RED, EnumChatFormatting.GOLD},
                EnumChatFormatting.GOLD,
                "✥",
                EnumChatFormatting.YELLOW,
                false
            );
        } else if (level < 4200) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.YELLOW,
                new EnumChatFormatting[]{EnumChatFormatting.YELLOW, EnumChatFormatting.GOLD, EnumChatFormatting.RED, EnumChatFormatting.LIGHT_PURPLE},
                EnumChatFormatting.LIGHT_PURPLE,
                "✥",
                EnumChatFormatting.DARK_PURPLE,
                false
            );
        } else if (level < 4300) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_BLUE,
                new EnumChatFormatting[]{EnumChatFormatting.BLUE, EnumChatFormatting.DARK_AQUA, EnumChatFormatting.AQUA, EnumChatFormatting.WHITE},
                EnumChatFormatting.GRAY,
                "✥",
                EnumChatFormatting.GRAY,
                false
            );
        } else if (level < 4400) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.BLACK,
                new EnumChatFormatting[]{
                    EnumChatFormatting.DARK_PURPLE, EnumChatFormatting.DARK_GRAY, EnumChatFormatting.DARK_GRAY, EnumChatFormatting.DARK_PURPLE
                },
                EnumChatFormatting.DARK_PURPLE,
                "✥",
                EnumChatFormatting.BLACK,
                false
            );
        } else if (level < 4500) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_GREEN,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_GREEN, EnumChatFormatting.GREEN, EnumChatFormatting.YELLOW, EnumChatFormatting.GOLD},
                EnumChatFormatting.DARK_PURPLE,
                "✥",
                EnumChatFormatting.LIGHT_PURPLE,
                false
            );
        } else if (level < 4600) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.WHITE,
                new EnumChatFormatting[]{EnumChatFormatting.WHITE, EnumChatFormatting.AQUA, EnumChatFormatting.AQUA, EnumChatFormatting.DARK_AQUA},
                EnumChatFormatting.DARK_AQUA,
                "✥",
                EnumChatFormatting.DARK_AQUA,
                false
            );
        } else if (level < 4700) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_AQUA,
                new EnumChatFormatting[]{EnumChatFormatting.AQUA, EnumChatFormatting.YELLOW, EnumChatFormatting.YELLOW, EnumChatFormatting.GOLD},
                EnumChatFormatting.LIGHT_PURPLE,
                "✥",
                EnumChatFormatting.DARK_PURPLE,
                false
            );
        } else if (level < 4800) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.WHITE,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_RED, EnumChatFormatting.RED, EnumChatFormatting.RED, EnumChatFormatting.BLUE},
                EnumChatFormatting.DARK_BLUE,
                "✥",
                EnumChatFormatting.BLUE,
                false
            );
        } else if (level < 4900) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_PURPLE,
                new EnumChatFormatting[]{EnumChatFormatting.DARK_PURPLE, EnumChatFormatting.RED, EnumChatFormatting.GOLD, EnumChatFormatting.YELLOW},
                EnumChatFormatting.AQUA,
                "✥",
                EnumChatFormatting.DARK_AQUA,
                false
            );
        } else if (level < 5000) {
            return new BedwarsStatsUtil.LevelFormat(
                EnumChatFormatting.DARK_GREEN,
                new EnumChatFormatting[]{EnumChatFormatting.GREEN, EnumChatFormatting.WHITE, EnumChatFormatting.WHITE, EnumChatFormatting.GREEN},
                EnumChatFormatting.GREEN,
                "✥",
                EnumChatFormatting.DARK_GREEN,
                false
            );
        } else {
            return level < 5100
                ? new BedwarsStatsUtil.LevelFormat(
                    EnumChatFormatting.DARK_RED,
                    new EnumChatFormatting[]{EnumChatFormatting.DARK_RED, EnumChatFormatting.DARK_PURPLE, EnumChatFormatting.BLUE, EnumChatFormatting.BLUE},
                    EnumChatFormatting.DARK_BLUE,
                    "✥",
                    EnumChatFormatting.BLACK,
                    false
                )
                : new BedwarsStatsUtil.LevelFormat(
                    EnumChatFormatting.DARK_RED,
                    new EnumChatFormatting[]{EnumChatFormatting.DARK_RED, EnumChatFormatting.DARK_PURPLE, EnumChatFormatting.BLUE, EnumChatFormatting.BLUE},
                    EnumChatFormatting.DARK_BLUE,
                    "✥",
                    EnumChatFormatting.BLACK,
                    false
                );
        }
    }

    static class LevelFormat {
        public final boolean boldStar;
        EnumChatFormatting leftBracketColor;
        EnumChatFormatting[] digitColors;
        EnumChatFormatting starColor;
        String starIcon;
        EnumChatFormatting rightBracketColor;

        public LevelFormat(
            EnumChatFormatting leftBracketColor,
            EnumChatFormatting[] digitColors,
            EnumChatFormatting starColor,
            String starIcon,
            EnumChatFormatting rightBracketColor,
            boolean boldStar
        ) {
            this.leftBracketColor = leftBracketColor;
            this.digitColors = digitColors;
            this.starColor = starColor;
            this.starIcon = starIcon;
            this.rightBracketColor = rightBracketColor;
            this.boldStar = boldStar;
        }
    }
}
