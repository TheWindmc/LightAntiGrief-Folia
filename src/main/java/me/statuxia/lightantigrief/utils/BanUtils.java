package me.statuxia.lightantigrief.utils;

import me.statuxia.lightantigrief.LAG;
import me.statuxia.lightantigrief.config.LAGConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class BanUtils {

    public static void ban(String playerName) {
        String banReason = LAGConfig.getBanReason();

        long delayTicks = 100L + ThreadLocalRandom.current().nextInt(100, 200);
        long delayMillis = delayTicks * 50;

        if (LAG.isFolia()) {
            Bukkit.getAsyncScheduler().runDelayed(LAG.getInstance(), task -> {
                Bukkit.getGlobalRegionScheduler().run(LAG.getInstance(), globalTask -> {
                    executeBanCommands(playerName, banReason);
                });
            }, delayMillis, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskLater(LAG.getInstance(), () -> {
                executeBanCommands(playerName, banReason);
            }, delayTicks);
        }
    }

    private static void executeBanCommands(String playerName, String banReason) {
        LAG.log("Banning player " + playerName + " for: " + banReason);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + playerName + " " + banReason);

        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null && player.getAddress() != null) {
            String ip = player.getAddress().getAddress().getHostAddress();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban-ip " + ip + " " + banReason);
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban-ip " + playerName + " " + banReason);
        }
    }

//    public static void banImmediate(String playerName) {
//        String banReason = LAGConfig.getBanReason();
//
//        if (LAG.isFolia()) {
//            Bukkit.getGlobalRegionScheduler().run(LAG.getInstance(), task -> {
//                executeBanCommands(playerName, banReason);
//            });
//        } else {
//            executeBanCommands(playerName, banReason);
//        }
//    }

//    public static void kick(Player player, String reason) {
//        if (LAG.isFolia()) {
//            player.getScheduler().run(LAG.getInstance(), task -> {
//                player.kick(net.kyori.adventure.text.Component.text("§c" + reason));
//            }, null);
//        } else {
//            player.kick(net.kyori.adventure.text.Component.text("§c" + reason));
//        }
//    }
}