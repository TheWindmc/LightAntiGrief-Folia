package me.statuxia.lightantigrief.listener;

import me.statuxia.lightantigrief.LAG;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ModeratorListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (LAG.isFolia()) {
            player.getScheduler().run(LAG.getInstance(), task -> {
                checkAndAddModerator(player);
            }, null);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(LAG.getInstance(), () -> {
                checkAndAddModerator(player);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeft(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        LAG.removeModerator(player);

        if (player.hasPermission("lag.moder")) {
            LAG.log("Moderator " + player.getName() + " has left the server");
        }
    }

    private void checkAndAddModerator(Player player) {
        if (player.hasPermission("lag.moder")) {
            LAG.addModerator(player);
            LAG.log("Moderator " + player.getName() + " has joined the server");

            if (LAG.isFolia()) {
                player.getScheduler().run(LAG.getInstance(), task -> {
                    player.sendMessage(LAG.getPrefix().append(
                            net.kyori.adventure.text.Component.text("§aYou are now in moderator mode!")
                    ));
                }, null);
            } else {
                player.sendMessage(LAG.getPrefix().append(
                        net.kyori.adventure.text.Component.text("§aYou are now in moderator mode!")
                ));
            }
        }
    }
}