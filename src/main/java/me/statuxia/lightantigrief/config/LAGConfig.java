package me.statuxia.lightantigrief.config;

import me.statuxia.lightantigrief.LightAntiGrief;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LAGConfig {

    private static volatile ConfigManager manager;
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static volatile Integer cachedTrustedTime;
    private static volatile Integer cachedFireCharge;
    private static volatile Integer cachedGetItem;
    private static volatile Integer cachedPutItem;
    private static volatile Integer cachedBreakBlock;
    private static volatile Integer cachedPlaceBlock;
    private static volatile Integer cachedMinecart;
    private static volatile Integer cachedExplode;
    private static volatile String cachedBanReason;
    private static volatile Boolean cachedTriggerRandomBonus;

    public static void getConfig() {
        lock.writeLock().lock();
        try {
            if (manager != null) {
                return;
            }

            Path pluginsPath = LightAntiGrief.getInstance().getDataFolder().toPath();
            manager = ConfigManager.of(Path.of(pluginsPath.toString(), "config.json").toString());

            if (manager.isCreated()) {
                JSONObject config = new JSONObject();
                config.put("trustedTime", 21600);
                config.put("fireCharge", 7);
                config.put("getItem", 12);
                config.put("putItem", 12);
                config.put("breakBlock", 5);
                config.put("placeBlock", 6);
                config.put("minecart", 3);
                config.put("explode", 4);
                config.put("triggerRandomBonus", true);
                config.put("banReason", "You have been banned for suspected griefing");

                manager.updateFile(config, true);
            }

            // Загружаем значения в кэш
            loadCache();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static void loadCache() {
        JSONObject json = manager.getJsonObject();
        cachedTrustedTime = json.optInt("trustedTime", 21600);
        cachedFireCharge = json.optInt("fireCharge", 7);
        cachedGetItem = json.optInt("getItem", 12);
        cachedPutItem = json.optInt("putItem", 12);
        cachedBreakBlock = json.optInt("breakBlock", 5);
        cachedPlaceBlock = json.optInt("placeBlock", 6);
        cachedMinecart = json.optInt("minecart", 3);
        cachedExplode = json.optInt("explode", 4);
        cachedBanReason = json.optString("banReason", "You have been banned for suspected griefing");
        cachedTriggerRandomBonus = json.optBoolean("triggerRandomBonus", true);
    }

    public static void reloadConfig() {
        lock.writeLock().lock();
        try {
            if (manager != null) {
                manager.reloadConfig();
                loadCache();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static void ensureLoaded() {
        if (manager == null) {
            getConfig();
        }
    }

    public static int getTrustedTime() {
        ensureLoaded();
        return cachedTrustedTime != null ? cachedTrustedTime : 21600;
    }

    public static int getFireCharge() {
        ensureLoaded();
        return cachedFireCharge != null ? cachedFireCharge : 7;
    }

    public static int getGetItem() {
        ensureLoaded();
        return cachedGetItem != null ? cachedGetItem : 12;
    }

    public static int getPutItem() {
        ensureLoaded();
        return cachedPutItem != null ? cachedPutItem : 12;
    }

    public static int getBreakBlock() {
        ensureLoaded();
        return cachedBreakBlock != null ? cachedBreakBlock : 5;
    }

    public static int getPlaceBlock() {
        ensureLoaded();
        return cachedPlaceBlock != null ? cachedPlaceBlock : 6;
    }

    public static int getMinecart() {
        ensureLoaded();
        return cachedMinecart != null ? cachedMinecart : 3;
    }

    public static int getExplode() {
        ensureLoaded();
        return cachedExplode != null ? cachedExplode : 4;
    }

    public static String getBanReason() {
        ensureLoaded();
        return cachedBanReason != null ? cachedBanReason : "You have been banned for suspected griefing";
    }

    public static boolean getTriggerRandomBonus() {
        ensureLoaded();
        return cachedTriggerRandomBonus != null ? cachedTriggerRandomBonus : true;
    }
}