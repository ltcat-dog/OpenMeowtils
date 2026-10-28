package wtf.tatp.meowtils.manager.icons.impl;

import com.mojang.authlib.GameProfile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.icons.IconProvider;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.module.hypixel.Stats;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.stats.util.BedwarsStatsUtil;
import wtf.tatp.meowtils.stats.util.SkywarsStatsUtil;
import wtf.tatp.meowtils.util.PlayerUtil;

public class StatsIcon implements IconProvider {
    private static final String SEPARATOR = EnumChatFormatting.DARK_GRAY + " ● ";
    private static final String STARTER = " ▶ ";
    private final Stats s = Module.get(Stats.class);

    @Override
    public String getPrefix(GameProfile profile, boolean tablist, boolean nametag) {
        if (!this.s.enabled) {
            return "";
        } else if (Server.HYPIXEL.isNotActive()) {
            return "";
        } else if (profile == null) {
            return "";
        } else if (nametag && !this.s.nametag) {
            return "";
        } else if (tablist && !this.s.tablist) {
            return "";
        } else {
            String name = profile.getName();
            if (name == null || PlayerUtil.isNicked(profile)) {
                return "";
            } else if (profile.getId() == null) {
                return "";
            } else if (profile.getId().version() == 2) {
                return "";
            } else {
                StatsContainer stats = Stats.getCached(name);
                if (stats != null || !Bedwars.GAME.isActive() && !Skywars.GAME.isActive() && !Skywars.MINI.isActive()) {
                    if (Bedwars.GAME.isActive()) {
                        if (stats != null && stats.bedwars != null) {
                            return nametag
                                ? BedwarsStatsUtil.getFormattedLevel(stats.bedwars.level).replace("[", "").replace("]", "") + " "
                                : BedwarsStatsUtil.getFormattedLevel(stats.bedwars.level) + " ";
                        } else {
                            return "";
                        }
                    } else if (!Skywars.GAME.isActive() && !Skywars.MINI.isActive()) {
                        return "";
                    } else {
                        return stats != null && stats.skywars != null ? stats.skywars.level + "✯ " : "";
                    }
                } else {
                    if (!PlayerUtil.isNicked(profile)) {
                        Stats.getStats(name, s -> {});
                    }

                    return "";
                }
            }
        }
    }

    @Override
    public String getSuffix(GameProfile profile, boolean tablist, boolean nametag) {
        if (!this.s.enabled) {
            return "";
        } else if (Server.HYPIXEL.isNotActive()) {
            return "";
        } else if (profile == null) {
            return "";
        } else if (nametag && !this.s.nametag) {
            return "";
        } else if (tablist && !this.s.tablist) {
            return "";
        } else if (nametag && this.s.displayMode.equals("Compact")) {
            return "";
        } else {
            boolean lowercase = this.s.displayMode.equals("Lowercase");
            boolean compact = this.s.displayMode.equals("Compact");
            String name = profile.getName();
            if (name == null || PlayerUtil.isNicked(profile)) {
                return "";
            } else if (profile.getId() == null) {
                return "";
            } else if (profile.getId().version() == 2) {
                return "";
            } else {
                StatsContainer stats = Stats.getCached(name);
                if (stats != null || !Bedwars.GAME.isActive() && !Skywars.GAME.isActive() && !Skywars.MINI.isActive()) {
                    if (Bedwars.GAME.isActive()) {
                        if (stats != null && stats.bedwars != null) {
                            List<String> parts = new ArrayList<>();
                            if (this.s.bedwarsFinals) {
                                String statType = compact ? "" : (lowercase ? "f: " : "F: ");
                                parts.add(BedwarsStatsUtil.getFinalsColor(stats.bedwars.finals) + statType + stats.bedwars.finals);
                            }

                            if (this.s.bedwarsFkdr) {
                                String statType = compact ? "" : (lowercase ? "fkd: " : "FKD: ");
                                parts.add(BedwarsStatsUtil.getFKDRColor(stats.bedwars.fkdr) + statType + this.format(stats.bedwars.fkdr));
                            }

                            if (this.s.bedwarsWlr) {
                                String statType = compact ? "" : (lowercase ? "wl: " : "WL: ");
                                parts.add(BedwarsStatsUtil.getWLRColor(stats.bedwars.wlr) + statType + this.format(stats.bedwars.wlr));
                            }

                            if (this.s.bedwarsWs) {
                                String statType = compact ? "" : (lowercase ? "ws: " : "WS: ");
                                String winstreak = stats.bedwars.ws > 1 ? BedwarsStatsUtil.getWSColor(stats.bedwars.ws) + statType + stats.bedwars.ws : "";
                                if (!winstreak.isEmpty()) {
                                    parts.add(winstreak);
                                }
                            }

                            if (this.s.bedwarsCr) {
                                String statType = compact ? "" : (lowercase ? "cr: " : "CR: ");
                                parts.add(
                                    BedwarsStatsUtil.getClutchRatioColor(stats.bedwars.clutchRatio)
                                        + statType
                                        + BigDecimal.valueOf(stats.bedwars.clutchRatio).setScale(2, RoundingMode.DOWN).toPlainString().replace(",", ".")
                                );
                            }

                            return this.joinStats(parts);
                        } else {
                            return "";
                        }
                    } else if (!Skywars.GAME.isActive() && !Skywars.MINI.isActive()) {
                        return "";
                    } else if (stats != null && stats.skywars != null) {
                        List<String> partsx = new ArrayList<>();
                        if (this.s.skywarsKills) {
                            String statType = compact ? "" : (lowercase ? "k: " : "K: ");
                            partsx.add(SkywarsStatsUtil.getKillsColor(stats.skywars.kills) + statType + stats.skywars.kills);
                        }

                        if (this.s.skywarsWins) {
                            String statType = compact ? "" : (lowercase ? "w: " : "W: ");
                            partsx.add(SkywarsStatsUtil.getWinsColor(stats.skywars.wins) + statType + stats.skywars.wins);
                        }

                        if (this.s.skywarsKdr) {
                            String statType = compact ? "" : (lowercase ? "kd: " : "KD: ");
                            partsx.add(SkywarsStatsUtil.getKdrColor(stats.skywars.kdr) + statType + this.format(stats.skywars.kdr));
                        }

                        if (this.s.skywarsWlr) {
                            String statType = compact ? "" : (lowercase ? "wl: " : "WL: ");
                            partsx.add(SkywarsStatsUtil.getWlrColor(stats.skywars.wlr) + statType + this.format(stats.skywars.wlr));
                        }

                        return this.joinStats(partsx);
                    } else {
                        return "";
                    }
                } else {
                    if (!PlayerUtil.isNicked(profile)) {
                        Stats.getStats(name, s -> {});
                    }

                    return "";
                }
            }
        }
    }

    private String format(double d) {
        return String.format("%.1f", d).replace(",", ".");
    }

    private String joinStats(List<String> parts) {
        return parts.isEmpty() ? "" : " ▶ " + String.join(SEPARATOR, parts);
    }
}
