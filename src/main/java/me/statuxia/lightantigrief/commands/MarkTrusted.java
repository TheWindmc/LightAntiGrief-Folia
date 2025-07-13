package me.statuxia.lightantigrief.commands;

import net.kyori.adventure.text.Component;
import me.statuxia.lightantigrief.LAG;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MarkTrusted implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("§7This command can only be used by players!"));
            return false;
        }

        if (!player.hasPermission("lag.moder")) {
            player.sendMessage(Component.text("§7Not enough permissions!"));
            return false;
        }

        if (strings.length == 0) {
            player.sendMessage(Component.text("§7Usage: /marktrusted <player>"));
            return false;
        }

        String playerName = strings[0];
        if (playerName.length() > 16) {
            player.sendMessage(Component.text("§7Nickname must not be more than 16 characters"));
            return false;
        }

        if (LAG.isFolia()) {
            Bukkit.getAsyncScheduler().runNow(LAG.getInstance(), task -> {
                boolean added = LAG.addTrustedPlayer(playerName);
                if (!added) {
                    LAG.removeTrustedPlayer(playerName);
                    player.sendMessage(Component.text("§7Player §e" + playerName + " §7removed from trusted list"));
                } else {
                    player.sendMessage(Component.text("§7Player §e" + playerName + " §7added to trusted list"));
                }
            });
        } else {
            boolean added = LAG.addTrustedPlayer(playerName);
            if (!added) {
                LAG.removeTrustedPlayer(playerName);
                player.sendMessage(Component.text("§7Player §e" + playerName + " §7removed from trusted list"));
            } else {
                player.sendMessage(Component.text("§7Player §e" + playerName + " §7added to trusted list"));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length != 1) {
            return new ArrayList<>();
        }

        String prefix = strings[0].toLowerCase(Locale.ROOT);
        List<String> suggestions = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String name = player.getName();
            if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                suggestions.add(name);
            }
        }

        return suggestions;
    }
}