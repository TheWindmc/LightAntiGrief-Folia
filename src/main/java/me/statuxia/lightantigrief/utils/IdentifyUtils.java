package me.statuxia.lightantigrief.utils;

import me.statuxia.lightantigrief.LAG;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class IdentifyUtils {

    private static final ConcurrentHashMap<String, CachedOwner> ownerCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = TimeUnit.MINUTES.toMillis(5); // 5 минут
    private static final int LOOKUP_TIME = 13150080 * 4; // ~2 года

    private record CachedOwner(String owner, long timestamp) {
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }

    public static String getOwner(Block block) {
        CoreProtectAPI coreProtect = LAG.getCoreProtectAPI();
        if (coreProtect == null) {
            LAG.log("CoreProtect is not available, cannot identify block owner");
            return "";
        }

        String cacheKey = getCacheKey(block);

        CachedOwner cached = ownerCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.owner();
        }

        String owner = performLookup(coreProtect, block);

        ownerCache.put(cacheKey, new CachedOwner(owner, System.currentTimeMillis()));

        cleanupCache();

        return owner;
    }

    public static CompletableFuture<String> getOwnerAsync(Block block) {
        return CompletableFuture.supplyAsync(() -> getOwner(block));
    }

    private static String performLookup(CoreProtectAPI coreProtect, Block block) {
        try {
            List<String[]> results = coreProtect.blockLookup(block, LOOKUP_TIME);

            if (results == null || results.isEmpty()) {
                return "";
            }

            Material blockType = block.getType();

            for (int i = results.size() - 1; i >= 0; i--) {
                String[] result = results.get(i);
                CoreProtectAPI.ParseResult parseResult = coreProtect.parseResult(result);

                // ActionId 1 = блок размещен
                if (parseResult.getActionId() == 1 && parseResult.getType() == blockType) {
                    return parseResult.getPlayer();
                }
            }
        } catch (Exception e) {
            LAG.log("Error during CoreProtect lookup: " + e.getMessage());
        }

        return "";
    }

    private static String getCacheKey(Block block) {
        return block.getWorld().getName() + ":" +
                block.getX() + ":" +
                block.getY() + ":" +
                block.getZ();
    }

    private static void cleanupCache() {
        if (ownerCache.size() > 1000) {
            ownerCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
    }

    public static void clearCache() {
        ownerCache.clear();
    }

//    public static int getCacheSize() {
//        return ownerCache.size();
//    }
}