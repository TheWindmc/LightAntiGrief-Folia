package me.statuxia.lightantigrief.utils;

import me.statuxia.lightantigrief.LAG;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayTime {

    /**
     * Получить время игры в тиках
     * @param player игрок
     * @return время в тиках
     */
    public static int ofTicks(Player player) {
        try {
            return player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        } catch (Exception e) {
            LAG.log("Failed to get playtime for " + player.getName() + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Получить время игры в секундах
     * @param player игрок
     * @return время в секундах
     */
    public static int ofSeconds(Player player) {
        return ofTicks(player) / 20;
    }

    /**
     * Получить время игры в минутах
     * @param player игрок
     * @return время в минутах
     */
    public static int ofMinutes(Player player) {
        return ofSeconds(player) / 60;
    }

    /**
     * Получить время игры в часах
     * @param player игрок
     * @return время в часах
     */
    public static int ofHours(Player player) {
        return ofMinutes(player) / 60;
    }

    /**
     * Получить время игры в днях
     * @param player игрок
     * @return время в днях
     */
    public static int ofDays(Player player) {
        return ofHours(player) / 24;
    }

    /**
     * Получить время игры асинхронно (для Folia)
     * @param player игрок
     * @param unit единица времени
     * @return CompletableFuture с временем игры
     */
    public static CompletableFuture<Long> getPlayTimeAsync(Player player, TimeUnit unit) {
        return CompletableFuture.supplyAsync(() -> {
            long ticks = ofTicks(player);
            long millis = ticks * 50; // 1 тик = 50мс
            return unit.convert(millis, TimeUnit.MILLISECONDS);
        });
    }

    /**
     * Получить отформатированное время игры
     * @param player игрок
     * @return отформатированная строка (например: "5d 3h 20m")
     */
    public static String getFormattedPlayTime(Player player) {
        int totalSeconds = ofSeconds(player);

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append("d ");
        }
        if (hours > 0) {
            result.append(hours).append("h ");
        }
        if (minutes > 0) {
            result.append(minutes).append("m ");
        }
        if (seconds > 0 || result.isEmpty()) {
            result.append(seconds).append("s");
        }

        return result.toString().trim();
    }

    /**
     * Проверить, достиг ли игрок минимального времени игры
     * @param player игрок
     * @param requiredSeconds требуемое время в секундах
     * @return true если игрок играл достаточно долго
     */
    public static boolean hasPlayedEnough(Player player, int requiredSeconds) {
        return ofSeconds(player) >= requiredSeconds;
    }

    /**
     * Получить процент от требуемого времени игры
     * @param player игрок
     * @param requiredSeconds требуемое время в секундах
     * @return процент (0-100)
     */
    public static int getProgressPercent(Player player, int requiredSeconds) {
        if (requiredSeconds <= 0) {
            return 100;
        }

        int currentSeconds = ofSeconds(player);
        int percent = (currentSeconds * 100) / requiredSeconds;

        return Math.min(percent, 100);
    }
}