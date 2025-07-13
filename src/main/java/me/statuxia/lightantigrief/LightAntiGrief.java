package me.statuxia.lightantigrief;

import lombok.Getter;
import me.statuxia.lightantigrief.commands.MarkTrusted;
import me.statuxia.lightantigrief.commands.TPWorld;
import me.statuxia.lightantigrief.config.LAGConfig;
import me.statuxia.lightantigrief.listener.CheckPlayerListener;
import me.statuxia.lightantigrief.listener.GriefListener;
import me.statuxia.lightantigrief.listener.ModeratorListener;
import me.statuxia.lightantigrief.trigger.BufferTrigger;
import me.statuxia.lightantigrief.utils.IdentifyUtils;
import me.statuxia.lightantigrief.utils.PlayTime;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class LightAntiGrief extends JavaPlugin {

    private static final Set<UUID> moderators = ConcurrentHashMap.newKeySet();
    @Getter
    private static final Component prefix = Component.text("§6[LAG] §r");
    @Getter
    private static final Set<String> trustedPlayers = ConcurrentHashMap.newKeySet();
    @Getter
    private static final Set<String> trustedIPs = ConcurrentHashMap.newKeySet();
    private static LightAntiGrief INSTANCE;
    @Getter
    private static CoreProtectAPI coreProtectAPI;
    private static boolean isFolia = false;

    public static LightAntiGrief getInstance() {
        return INSTANCE;
    }

    public static void addModerator(Player player) {
        moderators.add(player.getUniqueId());
    }

    public static void removeModerator(Player player) {
        moderators.remove(player.getUniqueId());
    }

    public static Set<Player> getModerators() {
        Set<Player> players = ConcurrentHashMap.newKeySet();
        Set<UUID> toRemove = ConcurrentHashMap.newKeySet();

        if (isFolia) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (moderators.contains(player.getUniqueId())) {
                    players.add(player);
                }
            });
        } else {
            moderators.forEach(moderator -> {
                Player player = Bukkit.getPlayer(moderator);
                if (player != null) {
                    players.add(player);
                } else {
                    toRemove.add(moderator);
                }
            });
            moderators.removeAll(toRemove);
        }

        return new HashSet<>(players);
    }

    public static void removeTrustedPlayer(String playerName) {
        trustedPlayers.remove(playerName.toLowerCase(Locale.ROOT));
    }

    public static boolean addTrustedPlayer(String playerName) {
        return trustedPlayers.add(playerName.toLowerCase(Locale.ROOT));
    }

    public static void addTrustedIp(String ip) {
        trustedIPs.add(ip);
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
            log("Detected Folia server", Level.INFO);
        } catch (ClassNotFoundException e) {
            isFolia = false;
            log("Running on standard Paper/Spigot", Level.INFO);
        }

        coreProtectAPI = getCoreProtect();
        if (coreProtectAPI == null) {
            log("CoreProtect not found. Some features will be disabled.", Level.WARNING);
            // Не отключаем плагин, просто предупреждаем
        } else {
            log("CoreProtect found and loaded successfully");
        }

        log("It is not recommended to use at the beginning of the game. It is advisable to wait 2-3 days irl", Level.WARNING);

        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().run(this, task -> {
                processOnlinePlayers();
            });
        } else {
            processOnlinePlayers();
        }

        Objects.requireNonNull(Bukkit.getPluginCommand("tpworld")).setExecutor(new TPWorld());
        Objects.requireNonNull(Bukkit.getPluginCommand("marktrusted")).setExecutor(new MarkTrusted());
        Bukkit.getPluginManager().registerEvents(new GriefListener(), this);
        Bukkit.getPluginManager().registerEvents(new ModeratorListener(), this);
        Bukkit.getPluginManager().registerEvents(new CheckPlayerListener(), this);
    }

    private void processOnlinePlayers() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for (Player player : players) {
            if (player.hasPermission("lag.moder")) {
                moderators.add(player.getUniqueId());
            }

            if (isFolia) {
                player.getScheduler().run(this, task -> {
                    checkAndAddTrustedPlayer(player);
                }, null);
            } else {
                checkAndAddTrustedPlayer(player);
            }
        }
    }

    private void checkAndAddTrustedPlayer(Player player) {
        if (!GriefListener.isTrusted(player) && PlayTime.ofSeconds(player) > LAGConfig.getTrustedTime()) {
            addTrustedPlayer(player.getName());
            if (player.getAddress() != null && player.getAddress().getAddress() != null) {
                addTrustedIp(player.getAddress().getAddress().getHostAddress());
            }
        }
    }

    private CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        if (!(plugin instanceof CoreProtect)) {
            return null;
        }

        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (!CoreProtect.isEnabled()) {
            return null;
        }

        if (CoreProtect.APIVersion() < 9) {
            return null;
        }

        log("CoreProtect Loaded");
        return CoreProtect;
    }

    public static void log(String message) {
        log(message, Level.INFO);
    }

    public static void log(String message, Level level) {
        Bukkit.getLogger().log(level, "[LAG] " + message);
    }

    public static boolean isFolia() {
        return isFolia;
    }

    @Override
    public void onDisable() {
        if (isFolia) {
            Bukkit.getAsyncScheduler().cancelTasks(this);
            Bukkit.getGlobalRegionScheduler().cancelTasks(this);
        } else {
            Bukkit.getScheduler().cancelTasks(this);
        }

        IdentifyUtils.clearCache();
        BufferTrigger.clearAllTriggers();

        log("LightAntiGrief disabled");
    }
}