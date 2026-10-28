package wtf.tatp.meowtils.stats;

import java.util.concurrent.ConcurrentHashMap;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.hypixel.Stats;

public class StatsCache {
    private static final ConcurrentHashMap<String, StatsContainer> CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> FAILED = new ConcurrentHashMap<>();
    private static final int RETRY_DELAY = 300000;

    public static StatsContainer getValid(String key) {
        StatsContainer stats = CACHE.get(key);
        if (stats == null) {
            return null;
        } else {
            long ttl = Module.get(Stats.class).cache * 60000L;
            if (stats.isExpired(ttl)) {
                CACHE.remove(key);
                return null;
            } else {
                return stats;
            }
        }
    }

    public static boolean canRetry(String name) {
        Long last = FAILED.get(name);
        if (last == null) {
            return true;
        } else if (System.currentTimeMillis() - last > 300000L) {
            FAILED.remove(name);
            return true;
        } else {
            return false;
        }
    }

    public static void put(String key, StatsContainer stats) {
        CACHE.put(key, stats);
    }

    public static void markFailed(String key) {
        FAILED.put(key, System.currentTimeMillis());
    }

    public static void clearCache() {
        CACHE.clear();
        FAILED.clear();
    }
}
