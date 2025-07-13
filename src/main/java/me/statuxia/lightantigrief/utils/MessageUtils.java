package me.statuxia.lightantigrief.utils;

import me.statuxia.lightantigrief.LAG;
import me.statuxia.lightantigrief.trigger.actions.GriefAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageUtils {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    public static Component generateMessage(Block block, GriefAction action, Material type) {
        return generateMessage(block.getType().toString(), block.getLocation(), action, type.toString());
    }

    public static Component generateMessage(Player player, Location location, GriefAction action, Material type) {
        return generateMessage(player.getName(), location, action, type.toString());
    }

    public static Component generateMessage(String name, Location location, GriefAction action, String type) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        return LAG.getPrefix()
                .append(Component.text("[" + timestamp + "] ", NamedTextColor.GRAY))
                .append(Component.text(name, NamedTextColor.RED))
                .append(Component.text("", NamedTextColor.YELLOW))
                .append(action.getMessage().color(NamedTextColor.YELLOW))
                .append(Component.text(formatMaterialName(type), NamedTextColor.GOLD))
                .append(Component.text(" at ", NamedTextColor.WHITE))
                .append(generateTeleport(location));
    }

    public static Component generateTeleport(Location location) {
        String coordinates = location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
        String worldName = getWorldDisplayName(location.getWorld());

        TextComponent.Builder builder = Component.text()
                .append(Component.text("(", NamedTextColor.WHITE))
                .append(Component.text(coordinates, NamedTextColor.AQUA)
                        .decorate(TextDecoration.UNDERLINED))
                .append(Component.text(" in ", NamedTextColor.WHITE))
                .append(Component.text(worldName, getWorldColor(location.getWorld())))
                .append(Component.text(")", NamedTextColor.WHITE));

        return builder.build()
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/tpworld " + coordinates + " " + location.getWorld().getName()))
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.text("Click to teleport to ", NamedTextColor.GREEN)
                                .append(Component.text(coordinates, NamedTextColor.AQUA))
                                .append(Component.text(" in ", NamedTextColor.GREEN))
                                .append(Component.text(worldName, getWorldColor(location.getWorld())))));
    }

    public static Component generateSimpleMessage(String message, NamedTextColor color) {
        return LAG.getPrefix().append(Component.text(message, color));
    }

    public static Component generateWarningMessage(String message) {
        return LAG.getPrefix()
                .append(Component.text("⚠ ", NamedTextColor.YELLOW))
                .append(Component.text(message, NamedTextColor.YELLOW));
    }

    public static Component generateErrorMessage(String message) {
        return LAG.getPrefix()
                .append(Component.text("✖ ", NamedTextColor.RED))
                .append(Component.text(message, NamedTextColor.RED));
    }

    public static Component generateSuccessMessage(String message) {
        return LAG.getPrefix()
                .append(Component.text("✔ ", NamedTextColor.GREEN))
                .append(Component.text(message, NamedTextColor.GREEN));
    }

    public static String plainText(Component component) {
        return PLAIN_SERIALIZER.serialize(component);
    }

    private static String formatMaterialName(String material) {
        String[] parts = material.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    private static String getWorldDisplayName(World world) {
        String name = world.getName();
        return switch (name) {
            case "world" -> "Overworld";
            case "world_nether" -> "Nether";
            case "world_the_end" -> "The End";
            default -> name;
        };
    }

    private static NamedTextColor getWorldColor(World world) {
        String name = world.getName();
        return switch (name) {
            case "world" -> NamedTextColor.GREEN;
            case "world_nether" -> NamedTextColor.RED;
            case "world_the_end" -> NamedTextColor.LIGHT_PURPLE;
            default -> NamedTextColor.WHITE;
        };
    }
}