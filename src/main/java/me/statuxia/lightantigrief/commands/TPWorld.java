package me.statuxia.lightantigrief.commands;

import me.statuxia.lightantigrief.LAG;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TPWorld implements TabExecutor {
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

        if (strings.length != 4) {
            player.sendMessage(Component.text("§7Usage: /tpworld <x> <y> <z> <world>"));
            return false;
        }

        World world = switch (strings[3]) {
            case "world_nether" -> Bukkit.getWorld("world_nether");
            case "world_the_end" -> Bukkit.getWorld("world_the_end");
            default -> Bukkit.getWorld("world");
        };

        if (world == null) {
            player.sendMessage(Component.text("§7World not found!"));
            return false;
        }

        int x, y, z;
        try {
            x = Integer.parseInt(strings[0]);
            y = Integer.parseInt(strings[1]);
            z = Integer.parseInt(strings[2]);
        } catch (NumberFormatException exception) {
            player.sendMessage(Component.text("§7Incorrect coordinate format!"));
            return false;
        }

        Location location = new Location(world, x + 0.5, y, z + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());

        if (LAG.isFolia()) {
            @NotNull CompletableFuture<Chunk> future = world.getChunkAtAsync(location);
            future.thenAccept(chunk -> {
                Bukkit.getRegionScheduler().run(LAG.getInstance(), location, task -> {
                    player.teleportAsync(location).thenAccept(success -> {
                        if (success) {
                            player.sendMessage(Component.text("§7Teleported to §e" + x + " " + y + " " + z + " §7in §e" + world.getName()));
                        } else {
                            player.sendMessage(Component.text("§7Failed to teleport!"));
                        }
                    });
                });
            });
        } else {
            player.teleport(location);
            player.sendMessage(Component.text("§7Teleported to §e" + x + " " + y + " " + z + " §7in §e" + world.getName()));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> completions = new ArrayList<>();

        switch (strings.length) {
            case 1 -> {
                completions.add("~");
                if (commandSender instanceof Player player) {
                    completions.add(String.valueOf(player.getLocation().getBlockX()));
                }
            }
            case 2 -> {
                completions.add("~");
                if (commandSender instanceof Player player) {
                    completions.add(String.valueOf(player.getLocation().getBlockY()));
                }
            }
            case 3 -> {
                completions.add("~");
                if (commandSender instanceof Player player) {
                    completions.add(String.valueOf(player.getLocation().getBlockZ()));
                }
            }
            case 4 -> {
                completions.add("world");
                completions.add("world_nether");
                completions.add("world_the_end");
            }
        }

        return completions;
    }
}