package me.statuxia.lightantigrief.listener;

import me.statuxia.lightantigrief.LAG;
import me.statuxia.lightantigrief.config.LAGConfig;
import me.statuxia.lightantigrief.utils.PlayTime;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Locale;

public class CheckPlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (LAG.isFolia()) {
            player.getScheduler().run(LAG.getInstance(), task -> {
                checkAndTrustPlayer(player);
            }, null);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(LAG.getInstance(), () -> {
                checkAndTrustPlayer(player);
            });
        }
    }

    private void checkAndTrustPlayer(Player player) {
        String name = player.getName();

        if (player.getAddress() == null || player.getAddress().getAddress() == null) {
            return;
        }

        String playerIp = player.getAddress().getAddress().getHostAddress();

        if (LAG.getTrustedIPs().contains(playerIp)) {
            return;
        }

        if (LAG.getTrustedPlayers().contains(name.toLowerCase(Locale.ROOT))) {
            return;
        }

        long playTimeSeconds = PlayTime.ofSeconds(player);
        int trustedTime = LAGConfig.getTrustedTime();

        if (playTimeSeconds > trustedTime) {
            LAG.addTrustedPlayer(name);
            LAG.addTrustedIp(playerIp);

            LAG.log("Player " + name + " (IP: " + playerIp + ") has been automatically trusted after " +
                    (playTimeSeconds / 3600) + " hours of playtime.");
        }
    }
}