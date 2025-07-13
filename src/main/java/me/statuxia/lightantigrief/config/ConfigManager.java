package me.statuxia.lightantigrief.config;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConfigManager {
    private final Path path;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Getter
    private volatile JSONObject jsonObject;

    @Getter
    private volatile boolean created = false;

    public ConfigManager(@NotNull String path) throws IOException {
        this.path = getPath(path);
        create();
        this.jsonObject = getObject();
    }

    public static @NotNull ConfigManager of(@NotNull String path) throws IOException {
        return new ConfigManager(path);
    }

    private @NotNull Path getPath(@NotNull String stringPath) {
        return Paths.get(stringPath);
    }

    private void create() throws IOException {
        lock.writeLock().lock();
        try {
            if (!Files.isDirectory(path.getParent())) {
                Files.createDirectories(path.getParent());
                created = true;
            }
            if (Files.notExists(path)) {
                Files.createFile(path);
                Files.writeString(path, "{}", StandardCharsets.UTF_8);
                created = true;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void updateFile(@NotNull JSONObject jsonObject, boolean saveInObject) throws IOException {
        lock.writeLock().lock();
        try {
            if (saveInObject) {
                this.jsonObject = jsonObject;
            }
            Files.writeString(path, jsonObject.toString(2), StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public @NotNull JSONObject getObject() throws IOException {
        lock.readLock().lock();
        try {
            if (Files.notExists(path)) {
                return new JSONObject();
            }

            String content = Files.readString(path, StandardCharsets.UTF_8);
            if (content.isEmpty()) {
                return new JSONObject();
            }

            try {
                return new JSONObject(content);
            } catch (JSONException e) {
                return new JSONObject();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public void reloadConfig() throws IOException {
        lock.writeLock().lock();
        try {
            this.jsonObject = getObject();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean has(@NotNull String key) {
        lock.readLock().lock();
        try {
            return jsonObject.has(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Object get(@NotNull String key) {
        lock.readLock().lock();
        try {
            return jsonObject.opt(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void set(@NotNull String key, Object value) throws IOException {
        lock.writeLock().lock();
        try {
            jsonObject.put(key, value);
            updateFile(jsonObject, false);
        } finally {
            lock.writeLock().unlock();
        }
    }
}