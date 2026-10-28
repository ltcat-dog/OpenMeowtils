package wtf.tatp.meowtils.stats;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.Meowtils;

public class StatsManager {
    private static final ArrayDeque<StatsManager.FetchTask> QUEUE = new ArrayDeque<>();
    private static final HashSet<String> QUEUED_KEYS = new HashSet<>();
    private static final ConcurrentHashMap<String, List<StatsManager.Callback>> PENDING_FETCHES = new ConcurrentHashMap<>();
    private static final int FETCH_TIMEOUT = 10000;
    private static boolean schedulerActive = false;
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Meowtils-Stats-Scheduler");
        t.setDaemon(true);
        return t;
    });
    private static final ExecutorService FETCH_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Meowtils-Stats-Fetch");
        t.setDaemon(true);
        return t;
    });

    public static void request(String name, StatsSource source, StatsManager.Callback callback) {
        if (name != null && source != null) {
            String lowerName = name.toLowerCase();
            String key = source.getId() + ":" + lowerName;
            StatsContainer cached = StatsCache.getValid(key);
            if (cached != null) {
                safeCallback(callback, cached);
            } else if (!StatsCache.canRetry(key)) {
                safeCallback(callback, null);
            } else {
                synchronized (QUEUE) {
                    if (PENDING_FETCHES.containsKey(key)) {
                        if (callback != null) {
                            PENDING_FETCHES.get(key).add(callback);
                        }
                    } else {
                        if (callback != null) {
                            PENDING_FETCHES.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>())).add(callback);
                        }

                        if (QUEUED_KEYS.add(key)) {
                            QUEUE.add(new StatsManager.FetchTask(lowerName, key, source));
                            if (!schedulerActive) {
                                schedulerActive = true;
                                SCHEDULER.execute(StatsManager::runNext);
                            }
                        }
                    }
                }
            }
        } else {
            safeCallback(callback, null);
        }
    }

    private static void runNext() {
        StatsManager.FetchTask t;
        synchronized (QUEUE) {
            t = QUEUE.poll();
            if (t == null) {
                schedulerActive = false;
                return;
            }
        }

        runFetch(t);
        SCHEDULER.schedule(StatsManager::runNext, t.source.getCooldown(), TimeUnit.MILLISECONDS);
    }

    private static void runFetch(StatsManager.FetchTask t) {
        CompletableFuture<StatsContainer> future = CompletableFuture.supplyAsync(() -> t.source.fetch(t.name), FETCH_EXECUTOR);
        ScheduledFuture<?> fallback = SCHEDULER.schedule(() -> future.cancel(true), 10000L, TimeUnit.MILLISECONDS);
        future.whenComplete((result, e) -> {
            fallback.cancel(false);
            synchronized (QUEUE) {
                QUEUED_KEYS.remove(t.key);
            }

            completedFetch(t.key, e != null ? null : result);
        });
    }

    private static void completedFetch(String key, StatsContainer stats) {
        if (stats != null) {
            StatsCache.put(key, stats);
        } else {
            StatsCache.markFailed(key);
        }

        notifyCallbacks(key, stats);
    }

    private static void notifyCallbacks(String key, StatsContainer stats) {
        List<StatsManager.Callback> callbacks;
        synchronized (QUEUE) {
            callbacks = PENDING_FETCHES.remove(key);
        }

        if (callbacks != null) {
            for (StatsManager.Callback c : callbacks) {
                safeCallback(c, stats);
            }
        }
    }

    private static void safeCallback(StatsManager.Callback c, StatsContainer stats) {
        if (c != null) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                try {
                    c.call(stats);
                } catch (Exception var3) {
                    Meowtils.error("Failed stats callback: " + var3);
                }
            });
        }
    }

    public interface Callback {
        void call(StatsContainer var1);
    }

    private static final class FetchTask {
        final String name;
        final String key;
        final StatsSource source;

        FetchTask(String name, String key, StatsSource source) {
            this.name = name;
            this.key = key;
            this.source = source;
        }
    }
}
