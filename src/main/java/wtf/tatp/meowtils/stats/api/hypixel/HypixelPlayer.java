package wtf.tatp.meowtils.stats.api.hypixel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.hypixel.Stats;
import wtf.tatp.meowtils.stats.StatsContainer;
import wtf.tatp.meowtils.stats.StatsSource;
import wtf.tatp.meowtils.stats.util.StatsUtil;

public class HypixelPlayer implements StatsSource {
    private static final String PREFIX = "(Hypixel API) ";

    @Override
    public String getId() {
        return "hypixel-player";
    }

    @Override
    public StatsContainer fetch(String name) {
        Stats s = Module.get(Stats.class);
        if (s.apiKey != null && !s.apiKey.isEmpty()) {
            HttpURLConnection conn = null;

            try {
                String url = "https://api.hypixel.net/v2/player?key=" + s.apiKey.replace(" ", "") + "&name=" + name;
                conn = (HttpURLConnection)new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                int code = conn.getResponseCode();
                if (code == 200) {
                    String response;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        response = reader.lines().collect(Collectors.joining());
                    }

                    JsonObject json = new JsonParser().parse(response).getAsJsonObject();
                    if (!json.has("success") || !json.get("success").getAsBoolean()) {
                        String cause = json.has("cause") ? json.get("cause").getAsString() : "Unknown reason";
                        Meowtils.error("(Hypixel API) error: " + cause);
                        return null;
                    } else if (!json.has("player") || json.get("player").isJsonNull()) {
                        Meowtils.error("(Hypixel API) missing player field");
                        return null;
                    } else {
                        JsonObject playerData = json.getAsJsonObject("player");
                        JsonObject lastExp = StatsUtil.getObj(playerData, "eugene");
                        long firstLogin = StatsUtil.getLong(playerData, "firstLogin", 0L);
                        long lastLogin = StatsUtil.getLong(playerData, "lastLogin", 0L);
                        long lastLogout = StatsUtil.getLong(playerData, "lastLogout", 0L);
                        int networkExp = StatsUtil.getInt(playerData, "networkExp", 0);
                        int karma = StatsUtil.getInt(playerData, "karma", 0);
                        int achievementPoints = StatsUtil.getInt(playerData, "achievementPoints", 0);
                        long lastClaimedReward = StatsUtil.getLong(playerData, "lastClaimedReward", 0L);
                        long lastClaimedExp = StatsUtil.getLong(lastExp, "dailyTwoKExp", 0L);
                        String channel = StatsUtil.getString(playerData, "channel", "Unknown");
                        String language = StatsUtil.getString(playerData, "userLanguage", "Unknown");
                        StatsContainer.GeneralStats generalStats = new StatsContainer.GeneralStats(
                            StatsUtil.getRank(playerData),
                            StatsUtil.getPlusColor(playerData),
                            firstLogin,
                            lastLogin,
                            lastLogout,
                            networkExp,
                            karma,
                            achievementPoints,
                            lastClaimedReward,
                            lastClaimedExp,
                            channel,
                            language
                        );
                        JsonObject bw = StatsUtil.getObj(StatsUtil.getObj(playerData, "stats"), "Bedwars");
                        int finalKills = StatsUtil.getInt(bw, "final_kills_bedwars", 0);
                        int finalDeaths = StatsUtil.getInt(bw, "final_deaths_bedwars", 1);
                        int beds = StatsUtil.getInt(bw, "beds_broken_bedwars", 0);
                        int bedsLost = StatsUtil.getInt(bw, "beds_lost_bedwars", 0);
                        double fkdr = (double)finalKills / finalDeaths;
                        int wins = StatsUtil.getInt(bw, "wins_bedwars", 0);
                        int losses = StatsUtil.getInt(bw, "losses_bedwars", 1);
                        double wlr = (double)wins / losses;
                        int ws = StatsUtil.getInt(bw, "winstreak", 0);
                        int level = StatsUtil.getInt(StatsUtil.getObj(playerData, "achievements"), "bedwars_level", 0);
                        double clutchRatio = bedsLost == 0 ? 0.0 : 1.0 - (double)finalDeaths / bedsLost;
                        StatsContainer.BedwarsStats bedwarsStats = new StatsContainer.BedwarsStats(
                            level, finalKills, beds, bedsLost, fkdr, wlr, ws, clutchRatio
                        );
                        JsonObject sw = StatsUtil.getObj(StatsUtil.getObj(playerData, "stats"), "SkyWars");
                        String skywarsLevel = StatsUtil.getString(sw, "levelFormatted", "0");
                        int skywarsKills = StatsUtil.getInt(sw, "kills", 0);
                        int skywarsDeaths = StatsUtil.getInt(sw, "deaths", 1);
                        double skywarsKdr = (double)skywarsKills / skywarsDeaths;
                        int skywarsWins = StatsUtil.getInt(sw, "wins", 0);
                        int skywarsLosses = StatsUtil.getInt(sw, "losses", 1);
                        double skywarsWlr = (double)skywarsWins / skywarsLosses;
                        StatsContainer.SkywarsStats skywarsStats = new StatsContainer.SkywarsStats(
                            skywarsLevel, skywarsKills, skywarsKdr, skywarsWins, skywarsWlr
                        );
                        StatsContainer container = new StatsContainer();
                        container.updateGeneral(generalStats);
                        container.updateBedwars(bedwarsStats);
                        container.updateSkywars(skywarsStats);
                        Meowtils.info("(Hypixel API) Fetched stats for " + name);
                        return container;
                    }
                } else {
                    if (code == 400) {
                        Meowtils.error("(Hypixel API) Some data is missing");
                    } else if (code == 403) {
                        Meowtils.error("(Hypixel API) Access forbidden");
                    } else if (code == 429) {
                        Meowtils.error("(Hypixel API) Request limit has been reached");
                    } else {
                        Meowtils.error("(Hypixel API) Unsigned error: " + code);
                    }

                    try (InputStream err = conn.getErrorStream()) {
                        while (err != null && err.read() != -1) {
                        }
                    } catch (Exception var94) {
                    }

                    return null;
                }
            } catch (Exception var95) {
                Meowtils.error("Fetching stats from Hypixel API failed: " + var95.getMessage());
                return null;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } else {
            Meowtils.error("(Hypixel API) API key is missing");
            return null;
        }
    }
}
