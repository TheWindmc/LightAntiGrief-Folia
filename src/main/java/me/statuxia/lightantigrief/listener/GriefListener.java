package me.statuxia.lightantigrief.listener;

import me.statuxia.lightantigrief.LAG;
import me.statuxia.lightantigrief.config.LAGConfig;
import me.statuxia.lightantigrief.trigger.BufferTrigger;
import me.statuxia.lightantigrief.trigger.actions.GriefAction;
import me.statuxia.lightantigrief.utils.BanUtils;
import me.statuxia.lightantigrief.utils.IdentifyUtils;
import me.statuxia.lightantigrief.utils.MessageUtils;
import me.statuxia.lightantigrief.utils.PlayTime;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static me.statuxia.lightantigrief.LightAntiGrief.log;

public class GriefListener implements Listener {

    private static final List<Material> itemTriggers = List.of(
            Material.DIAMOND_BLOCK,
            Material.DIAMOND_AXE,
            Material.DIAMOND_HOE,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_SHOVEL,
            Material.DIAMOND_SWORD,
            Material.NETHERITE_AXE,
            Material.NETHERITE_HOE,
            Material.NETHERITE_PICKAXE,
            Material.NETHERITE_SHOVEL,
            Material.NETHERITE_SWORD,
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,
            Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS,
            Material.ELYTRA,
            Material.DIAMOND,
            Material.NETHERITE_INGOT,
            Material.TNT,
            Material.END_CRYSTAL,
            Material.BEACON,
            Material.GOLDEN_CARROT,
            Material.TRIDENT,
            Material.SHULKER_BOX,
            Material.NETHERITE_SCRAP,
            Material.NETHER_STAR,
            Material.TOTEM_OF_UNDYING,
            Material.SHULKER_SHELL,
            Material.ENCHANTED_GOLDEN_APPLE,
            Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
            Material.DIAMOND_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.WITHER_SKELETON_SKULL
    );

    private static final List<Material> blockTriggers = List.of(
            Material.CHEST,
            Material.BARREL,
            Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.TRAPPED_CHEST
    );

    private static final List<Material> railTriggers = List.of(
            Material.RAIL,
            Material.POWERED_RAIL,
            Material.DETECTOR_RAIL,
            Material.ACTIVATOR_RAIL
    );

    private static final List<Material> explosiveBlockTriggers = List.of(
            Material.TNT,
            Material.RESPAWN_ANCHOR,
            Material.WITHER_SKELETON_SKULL
    );

    private static final List<Material> explosiveBedTriggers = List.of(
            Material.RED_BED,
            Material.ORANGE_BED,
            Material.YELLOW_BED,
            Material.LIME_BED,
            Material.GREEN_BED,
            Material.LIGHT_BLUE_BED,
            Material.CYAN_BED,
            Material.BLUE_BED,
            Material.PURPLE_BED,
            Material.MAGENTA_BED,
            Material.PINK_BED,
            Material.BROWN_BED,
            Material.WHITE_BED,
            Material.LIGHT_GRAY_BED,
            Material.GRAY_BED,
            Material.BLACK_BED
    );

    private static final List<Material> ignoreBurnable = List.of(
            Material.VINE,
            Material.BAMBOO,
            Material.ACACIA_LEAVES,
            Material.BIRCH_LEAVES,
            Material.DARK_OAK_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.OAK_LEAVES,
            Material.SPRUCE_LEAVES,
            Material.AZALEA_LEAVES,
            Material.CHERRY_LEAVES,
            Material.FLOWERING_AZALEA_LEAVES,
            Material.MANGROVE_LEAVES,
            Material.MANGROVE_LOG,
            Material.JUNGLE_LOG
    );

    public static int getTrustedTime() {
        return LAGConfig.getTrustedTime();
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (isTrusted(player)) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        Location topLocation = top.getLocation();

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }

        if (PlayTime.ofSeconds(player) > getTrustedTime()) {
            return;
        }

        if (isEntityHolder(top.getHolder()) && clickedInventory instanceof PlayerInventory) {
            if (topLocation != null) {
                checkForTrigger(event, topLocation, player, GriefAction.PUT_ITEM);
            }
            return;
        }

        if (clickedInventory instanceof PlayerInventory) {
            return;
        }

        Location location = clickedInventory.getLocation();
        if (location == null) {
            return;
        }

        if (isEntityHolder(clickedInventory.getHolder())) {
            if (isPlacing(event)) {
                checkForTrigger(event, location, player, GriefAction.PUT_ITEM);
                return;
            }
            checkForTrigger(event, location, player, GriefAction.GET_ITEM);
            return;
        }

        Block block = location.getBlock();
        if (!blockTriggers.contains(block.getType()) || isPlacing(event)) {
            return;
        }

        checkOwnerAndTrigger(player, block, location, event);
    }

    private void checkOwnerAndTrigger(Player player, Block block, Location location, InventoryClickEvent event) {
        String ownerName = IdentifyUtils.getOwner(block);
        if (ownerName.equals(player.getName())) {
            return;
        }

        if (LAG.isFolia()) {
            CompletableFuture.runAsync(() -> {
                boolean ownerNearby = isOwnerNearby(player, ownerName);
                if (!ownerNearby) {
                    player.getScheduler().run(LAG.getInstance(), task -> {
                        checkForTrigger(event, location, player, GriefAction.GET_ITEM);
                    }, null);
                }
            });
        } else {
            Player owner = Bukkit.getPlayer(ownerName);
            if (owner == null || !player.getNearbyEntities(20, 20, 20).contains(owner)) {
                checkForTrigger(event, location, player, GriefAction.GET_ITEM);
            }
        }
    }

    private boolean isOwnerNearby(Player player, String ownerName) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().equals(ownerName)) {
                Location playerLoc = player.getLocation();
                Location ownerLoc = online.getLocation();
                if (playerLoc.getWorld().equals(ownerLoc.getWorld()) &&
                        playerLoc.distance(ownerLoc) <= 20) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isPlacing(InventoryClickEvent event) {
        return event.getAction() == InventoryAction.PLACE_ALL ||
                event.getAction() == InventoryAction.PLACE_ONE ||
                event.getAction() == InventoryAction.PLACE_SOME;
    }

    private boolean isEntityHolder(InventoryHolder holder) {
        return (holder instanceof HopperMinecart || holder instanceof AbstractHorse ||
                holder instanceof ChestBoat || holder instanceof StorageMinecart);
    }

    private void checkForTrigger(InventoryClickEvent event, Location location, Player player, GriefAction action) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null) {
            return;
        }

        if (!itemTriggers.contains(currentItem.getType())) {
            return;
        }

        processGriefAction(player, location, action, currentItem.getType());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!blockTriggers.contains(block.getType())) {
            return;
        }

        if (isTrusted(player)) {
            return;
        }

        if (PlayTime.ofSeconds(player) > getTrustedTime()) {
            return;
        }

        String ownerName = IdentifyUtils.getOwner(block);
        if (ownerName.equals(player.getName())) {
            return;
        }

        if (LAG.isFolia()) {
            CompletableFuture.runAsync(() -> {
                boolean ownerNearby = isOwnerNearby(player, ownerName);
                if (!ownerNearby) {
                    Bukkit.getScheduler().runTask(LAG.getInstance(), () -> {
                        processGriefAction(player, block.getLocation(), GriefAction.BREAK_BLOCK, block.getType());
                    });
                }
            });
        } else {
            Player owner = Bukkit.getPlayer(ownerName);
            if (owner == null || !player.getNearbyEntities(20, 20, 20).contains(owner)) {
                processGriefAction(player, block.getLocation(), GriefAction.BREAK_BLOCK, block.getType());
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        if (!explosiveBlockTriggers.contains(type) && !explosiveBedTriggers.contains(type)) {
            return;
        }

        if (isTrusted(player)) {
            return;
        }

        if (PlayTime.ofSeconds(player) > getTrustedTime()) {
            return;
        }

        if (explosiveBedTriggers.contains(type) && player.getWorld().equals(Bukkit.getWorld("world")) || player.getLocation().getBlockY() < 40) {
            return;
        }

        processGriefAction(player, block.getLocation(), GriefAction.PLACE_BLOCK, type);
    }

    @EventHandler
    public void onMineCartPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        if (!railTriggers.contains(clickedBlock.getType())) {
            return;
        }

        Player player = event.getPlayer();
        if (isTrusted(player)) {
            return;
        }

        if (PlayTime.ofSeconds(player) > getTrustedTime()) {
            return;
        }

        if (!(item.getType() == Material.HOPPER_MINECART || item.getType() == Material.TNT_MINECART)) {
            return;
        }

        processGriefAction(player, clickedBlock.getLocation(), GriefAction.MINECART, item.getType());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        Block explodedBlock = event.getBlock();

        if (!explosiveBlockTriggers.contains(explodedBlock.getType())) {
            return;
        }

        event.blockList().forEach(block -> {
            if (block.getType() == Material.AIR) {
                return;
            }
            if (!blockTriggers.contains(block.getType())) {
                return;
            }

            Component component = MessageUtils.generateMessage(block, GriefAction.EXPLODE, block.getType());
            String plainText = MessageUtils.plainText(component).replaceAll("§[0-9abcdefkmonlr]", "");
            log(plainText);
            sendToModerators(component);
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFireCharge(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        if (!clickedBlock.getType().isBurnable() || ignoreBurnable.contains(clickedBlock.getType())) {
            return;
        }

        Player player = event.getPlayer();
        if (isTrusted(player)) {
            return;
        }

        if (PlayTime.ofSeconds(player) > getTrustedTime()) {
            return;
        }

        ItemStack item = event.getItem();
        if (!(item != null && (item.getType() == Material.FLINT_AND_STEEL || item.getType() == Material.FIRE_CHARGE))) {
            return;
        }

        processGriefAction(player, clickedBlock.getLocation(), GriefAction.FIRE_CHARGE, clickedBlock.getType());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPlaceCrystal(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        if (clickedBlock.getType() != Material.OBSIDIAN) {
            return;
        }

        if (isTrusted(event.getPlayer())) {
            return;
        }

        if (PlayTime.ofSeconds(event.getPlayer()) > getTrustedTime()) {
            return;
        }

        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (!(item != null && item.getType() == Material.END_CRYSTAL)) {
            return;
        }

        processGriefAction(player, clickedBlock.getLocation(), GriefAction.PLACE_BLOCK, item.getType());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        Player responsiblePlayer = null;

        if (event.getEntity() instanceof TNTPrimed primed) {
            if (primed.getSource() instanceof Player player) {
                responsiblePlayer = player;
            } else if (primed.getSource() instanceof Projectile projectile) {
                ProjectileSource shooter = projectile.getShooter();
                if (shooter instanceof Player player) {
                    responsiblePlayer = player;
                } else if (shooter instanceof Mob mob && mob.getTarget() instanceof Player player) {
                    responsiblePlayer = player;
                }
            }
        } else if (entity instanceof EnderCrystal crystal) {
            EntityDamageEvent lastDamageCause = crystal.getLastDamageCause();
            if (lastDamageCause instanceof EntityDamageByEntityEvent damageByEntityEvent) {
                if (damageByEntityEvent.getDamager() instanceof Player player) {
                    responsiblePlayer = player;
                } else if (damageByEntityEvent.getDamager() instanceof Projectile projectile) {
                    ProjectileSource shooter = projectile.getShooter();
                    if (shooter instanceof Player player) {
                        responsiblePlayer = player;
                    } else if (shooter instanceof Mob mob && mob.getTarget() instanceof Player player) {
                        responsiblePlayer = player;
                    }
                }
            }
        } else if (entity instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                responsiblePlayer = player;
            } else if (shooter instanceof Mob mob && mob.getTarget() instanceof Player player) {
                responsiblePlayer = player;
            }
        } else if (entity instanceof Creeper creeper) {
            LivingEntity target = creeper.getTarget();
            if (target instanceof Player player) {
                responsiblePlayer = player;
            }
        }

        if (responsiblePlayer != null) {
            checkExplode(responsiblePlayer, event.blockList());
        }
    }

    private void checkExplode(Player player, List<Block> blockList) {
        if (isTrusted(player)) {
            return;
        }

        if (PlayTime.ofSeconds(player) > getTrustedTime()) {
            return;
        }

        blockList.forEach(block -> {
            if (block.getType() == Material.AIR) {
                return;
            }
            if (!blockTriggers.contains(block.getType())) {
                return;
            }

            processGriefAction(player, block.getLocation(), GriefAction.EXPLODE, block.getType());
        });
    }

    private void processGriefAction(Player player, Location location, GriefAction action, Material material) {
        Component component = MessageUtils.generateMessage(player, location, action, material);
        String plainText = MessageUtils.plainText(component).replaceAll("§[0-9abcdefkmonlr]", "");
        log(plainText);

        Set<Player> moderators = LAG.getModerators();
        if (moderators.isEmpty()) {
            BufferTrigger buffer = new BufferTrigger(player.getUniqueId(), action);
            BufferTrigger.trigger(buffer);

            if (BufferTrigger.isLimitReached(buffer)) {
                if (LAG.isFolia()) {
                    Bukkit.getAsyncScheduler().runNow(LAG.getInstance(), task -> {
                        BanUtils.ban(player.getName());
                    });
                } else {
                    BanUtils.ban(player.getName());
                }
            }
        } else {
            sendToModerators(component);
        }
    }

    private void sendToModerators(Component message) {
        Set<Player> moderators = LAG.getModerators();
        if (LAG.isFolia()) {
            moderators.forEach(moderator -> {
                moderator.getScheduler().run(LAG.getInstance(), task -> {
                    moderator.sendMessage(message);
                }, null);
            });
        } else {
            moderators.forEach(moderator -> moderator.sendMessage(message));
        }
    }

    public static boolean isTrusted(Player player) {
        if (player.getAddress() == null || player.getAddress().getAddress() == null) {
            return isTrusted(player.getName());
        }
        return isTrusted(player.getName()) || LAG.getTrustedIPs().contains(player.getAddress().getAddress().getHostAddress());
    }

    private static boolean isTrusted(String player) {
        return LAG.getTrustedPlayers().contains(player.toLowerCase(Locale.ROOT));
    }
}