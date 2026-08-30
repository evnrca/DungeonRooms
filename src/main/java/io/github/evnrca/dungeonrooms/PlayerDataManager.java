package io.github.evnrca.dungeonrooms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persists player progress to a flat Gson JSON file.
 * <p>
 * Data is stored at {@code plugins/DungeonRooms/playerdata.json}. Writes are
 * atomic by writing {@code playerdata.json.tmp} first, then replacing the real
 * file. Runtime writes are async; shutdown writes are sync.
 *
 * @author evnrca
 */
public final class PlayerDataManager {

    private static final Type DATA_TYPE = new TypeToken<Map<String, Map<String, Map<String, int[]>>>>() {
    }.getType();

    private final DungeonRooms plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataFile;
    private final Path tempFile;
    private final Object lock = new Object();

    private Map<String, Map<String, Map<String, int[]>>> data = new LinkedHashMap<>();

    public PlayerDataManager(DungeonRooms plugin) {
        this.plugin = plugin;
        this.dataFile = plugin.getDataFolder().toPath().resolve("playerdata.json");
        this.tempFile = plugin.getDataFolder().toPath().resolve("playerdata.json.tmp");
    }

    /**
     * Loads all progress synchronously during plugin startup.
     */
    public Map<UUID, Map<String, Map<String, int[]>>> loadAllSync() {
        ensureDataFolder();
        synchronized (lock) {
            if (!Files.exists(dataFile)) {
                data = new LinkedHashMap<>();
                return Map.of();
            }
            try (Reader reader = Files.newBufferedReader(dataFile)) {
                Map<String, Map<String, Map<String, int[]>>> loaded = gson.fromJson(reader, DATA_TYPE);
                data = loaded == null ? new LinkedHashMap<>() : loaded;
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to load playerdata.json: " + e.getMessage());
                data = new LinkedHashMap<>();
            }
            return snapshotWithUuidKeys();
        }
    }

    /**
     * Updates one room progress entry and saves asynchronously.
     */
    public void saveProgressAsync(UUID uuid, String dungeonName, String region, int kills, boolean unlocked) {
        synchronized (lock) {
            roomData(uuid, dungeonName).put(region, new int[]{kills, unlocked ? 1 : 0});
        }
        saveAsync();
    }

    /**
     * Replaces all cached progress for a player and saves asynchronously.
     */
    public void savePlayerAsync(UUID uuid, Map<String, Integer> kills, Map<String, Boolean> unlocked) {
        cachePlayer(uuid, kills, unlocked);
        saveAsync();
    }

    /**
     * Replaces cached progress for a player without scheduling a save.
     */
    public void cachePlayer(UUID uuid, Map<String, Integer> kills, Map<String, Boolean> unlocked) {
        synchronized (lock) {
            Map<String, Map<String, int[]>> playerData = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : kills.entrySet()) {
                String[] parts = splitKey(entry.getKey());
                playerData.computeIfAbsent(parts[0], key -> new LinkedHashMap<>())
                        .put(parts[1], new int[]{entry.getValue(), unlocked.getOrDefault(entry.getKey(), false) ? 1 : 0});
            }
            for (Map.Entry<String, Boolean> entry : unlocked.entrySet()) {
                if (!entry.getValue()) {
                    continue;
                }
                String[] parts = splitKey(entry.getKey());
                Map<String, int[]> dungeonData = playerData.computeIfAbsent(parts[0], key -> new LinkedHashMap<>());
                int[] current = dungeonData.getOrDefault(parts[1], new int[]{0, 0});
                current[1] = 1;
                dungeonData.put(parts[1], current);
            }
            data.put(uuid.toString(), playerData);
        }
    }

    public void resetPlayerAsync(UUID uuid) {
        synchronized (lock) {
            data.remove(uuid.toString());
        }
        saveAsync();
    }

    public void resetPlayerDungeonAsync(UUID uuid, String dungeonName) {
        synchronized (lock) {
            Map<String, Map<String, int[]>> playerData = data.get(uuid.toString());
            if (playerData != null) {
                playerData.remove(dungeonName);
                if (playerData.isEmpty()) {
                    data.remove(uuid.toString());
                }
            }
        }
        saveAsync();
    }

    /**
     * Saves synchronously. Used during plugin disable.
     */
    public void saveSync() {
        ensureDataFolder();
        synchronized (lock) {
            writeAtomic(data);
        }
    }

    private Map<String, int[]> roomData(UUID uuid, String dungeonName) {
        return data.computeIfAbsent(uuid.toString(), key -> new LinkedHashMap<>())
                .computeIfAbsent(dungeonName, key -> new LinkedHashMap<>());
    }

    private void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveSync);
    }

    private void writeAtomic(Map<String, Map<String, Map<String, int[]>>> snapshot) {
        try (Writer writer = Files.newBufferedWriter(tempFile)) {
            gson.toJson(snapshot, DATA_TYPE, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write playerdata.json.tmp: " + e.getMessage());
            return;
        }

        try {
            Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicFailure) {
            try {
                Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save playerdata.json: " + e.getMessage());
            }
        }
    }

    private Map<UUID, Map<String, Map<String, int[]>>> snapshotWithUuidKeys() {
        Map<UUID, Map<String, Map<String, int[]>>> snapshot = new HashMap<>();
        for (Map.Entry<String, Map<String, Map<String, int[]>>> entry : data.entrySet()) {
            try {
                snapshot.put(UUID.fromString(entry.getKey()), entry.getValue());
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Ignoring invalid UUID in playerdata.json: " + entry.getKey());
            }
        }
        return snapshot;
    }

    private String[] splitKey(String key) {
        String[] parts = key.split(":", 2);
        return parts.length == 2 ? parts : new String[]{key, ""};
    }

    private void ensureDataFolder() {
        try {
            Files.createDirectories(plugin.getDataFolder().toPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create plugin data folder: " + e.getMessage());
        }
    }
}
