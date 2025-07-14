package me.statuxia.lightantigrief.trigger;

import me.statuxia.lightantigrief.LAG;
import me.statuxia.lightantigrief.config.LAGConfig;
import me.statuxia.lightantigrief.trigger.actions.GriefAction;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public record BufferTrigger(UUID playerUUID, GriefAction action) {

    private static final Map<BufferTrigger, Trigger> BUFFER = new ConcurrentHashMap<>();
    private static final long CLEANUP_INTERVAL = 20L * 60 * 5; // 5 минут

    private static void scheduleCleanup() {
        if (LAG.isFolia()) {
            Bukkit.getAsyncScheduler().runAtFixedRate(LAG.getInstance(), task -> cleanupExpiredTriggers(), 0, CLEANUP_INTERVAL * 50, java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(LAG.getInstance(), BufferTrigger::cleanupExpiredTriggers, 0L, CLEANUP_INTERVAL);
        }
    }

    private static void cleanupExpiredTriggers() {
        BUFFER.entrySet().removeIf(entry -> entry.getValue().isExpired());
        LAG.log("Cleaned up expired grief triggers. Current size: " + BUFFER.size());
    }

    public static void trigger(BufferTrigger buffer) {
        BUFFER.compute(buffer, (key, existingTrigger) -> {
            if (existingTrigger == null) {
                return new Trigger(buffer);
            }
            return existingTrigger.incrementTriggers();
        });
    }

    public static int getTotal(BufferTrigger buffer) {
        Trigger trigger = BUFFER.get(buffer);
        return trigger != null ? trigger.getTotalTriggered() : 0;
    }

    public static boolean isLimitReached(BufferTrigger buffer) {
        Trigger trigger = BUFFER.get(buffer);
        if (trigger == null) {
            return false;
        }

        int limit = buffer.action.getLimitTriggers();
        int randomBonus = 0;

        if (LAGConfig.getTriggerRandomBonus()) {
            randomBonus = Math.abs(buffer.playerUUID.hashCode() % 7);
        }

        return trigger.getTotalTriggered() >= limit + randomBonus;
    }

    public static void clearPlayerTriggers(UUID playerUUID) {
        BUFFER.entrySet().removeIf(entry -> entry.getKey().playerUUID().equals(playerUUID));
    }

    public static void clearAllTriggers() {
        BUFFER.clear();
    }

    public static int getBufferSize() {
        return BUFFER.size();
    }

    public static Map<BufferTrigger, Integer> getCurrentTriggers() {
        Map<BufferTrigger, Integer> result = new ConcurrentHashMap<>();
        BUFFER.forEach((key, value) -> result.put(key, value.getTotalTriggered()));
        return result;
    }

    public static void reload() {
        if (LAG.getInstance() != null) {
            if (LAG.isFolia()) {
                Bukkit.getAsyncScheduler().cancelTasks(LAG.getInstance());
            } else {
                Bukkit.getScheduler().cancelTasks(LAG.getInstance());
            }
            scheduleCleanup();
        }
    }
}