package io.github.evnrca.dungeonrooms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists dungeon setup data to a flat Gson JSON file.
 *
 * @author evnrca
 */
public final class DungeonDataManager {

    private static final Type DATA_TYPE = new TypeToken<Map<String, StoredDungeon>>() {
    }.getType();

    /**
     * JSON representation of one dungeon.
     */
    public static final class StoredDungeon {
        public String world;
        public String region;
        public String spawnWorld;
        public String spawnRegion;
        public StoredLocation spawnLocation;
        public List<StoredRoom> rooms = new ArrayList<>();
    }

    /**
     * JSON representation of one room.
     */
    public static final class StoredRoom {
        public String world;
        public String region;
        public int requiredKills;
        public int sequence;
    }

    /**
     * JSON representation of a Bukkit location.
     */
    public static final class StoredLocation {
        public String world;
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;
    }

    private final DungeonRooms plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataFile;
    private final Path tempFile;
    private final Object lock = new Object();

    private Map<String, StoredDungeon> data = new LinkedHashMap<>();

    public DungeonDataManager(DungeonRooms plugin) {
        this.plugin = plugin;
        this.dataFile = plugin.getDataFolder().toPath().resolve("dungeons.json");
        this.tempFile = plugin.getDataFolder().toPath().resolve("dungeons.json.tmp");
    }

    /**
     * Loads all dungeon setup data synchronously during plugin startup.
     */
    public Map<String, StoredDungeon> loadAllSync() {
        ensureDataFolder();
        synchronized (lock) {
            if (!Files.exists(dataFile)) {
                data = new LinkedHashMap<>();
                return new LinkedHashMap<>();
            }
            try (Reader reader = Files.newBufferedReader(dataFile)) {
                Map<String, StoredDungeon> loaded = gson.fromJson(reader, DATA_TYPE);
                data = loaded == null ? new LinkedHashMap<>() : loaded;
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to load dungeons.json: " + e.getMessage());
                data = new LinkedHashMap<>();
            }
            return new LinkedHashMap<>(data);
        }
    }

    /**
     * Replaces all cached dungeon data and saves asynchronously.
     */
    public void saveAsync(Map<String, DungeonManager.DungeonData> dungeons) {
        synchronized (lock) {
            data = toStored(dungeons);
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveSync());
    }

    /**
     * Replaces all cached dungeon data and writes synchronously.
     */
    public void saveSync(Map<String, DungeonManager.DungeonData> dungeons) {
        synchronized (lock) {
            data = toStored(dungeons);
            writeAtomic(data);
        }
    }

    private Map<String, StoredDungeon> toStored(Map<String, DungeonManager.DungeonData> dungeons) {
        Map<String, StoredDungeon> stored = new LinkedHashMap<>();
        for (DungeonManager.DungeonData dungeon : dungeons.values()) {
            StoredDungeon storedDungeon = new StoredDungeon();
            storedDungeon.world = dungeon.world;
            storedDungeon.region = dungeon.region;
            storedDungeon.spawnWorld = dungeon.spawnWorld;
            storedDungeon.spawnRegion = dungeon.spawnRegion;
            storedDungeon.spawnLocation = toStoredLocation(dungeon.spawnLocation);
            for (DungeonManager.RoomData room : dungeon.rooms.values()) {
                StoredRoom storedRoom = new StoredRoom();
                storedRoom.world = room.world;
                storedRoom.region = room.region;
                storedRoom.requiredKills = room.requiredKills;
                storedRoom.sequence = room.sequence;
                storedDungeon.rooms.add(storedRoom);
            }
            stored.put(dungeon.dungeonName, storedDungeon);
        }
        return stored;
    }

    public Location toLocation(StoredLocation stored) {
        if (stored == null || stored.world == null) {
            return null;
        }
        World world = Bukkit.getWorld(stored.world);
        return world == null ? null : new Location(world, stored.x, stored.y, stored.z, stored.yaw, stored.pitch);
    }

    private StoredLocation toStoredLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        StoredLocation stored = new StoredLocation();
        stored.world = location.getWorld().getName();
        stored.x = location.getX();
        stored.y = location.getY();
        stored.z = location.getZ();
        stored.yaw = location.getYaw();
        stored.pitch = location.getPitch();
        return stored;
    }

    private void saveSync() {
        ensureDataFolder();
        synchronized (lock) {
            writeAtomic(data);
        }
    }

    private void writeAtomic(Map<String, StoredDungeon> snapshot) {
        try (Writer writer = Files.newBufferedWriter(tempFile)) {
            gson.toJson(snapshot, DATA_TYPE, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write dungeons.json.tmp: " + e.getMessage());
            return;
        }

        try {
            Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicFailure) {
            try {
                Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save dungeons.json: " + e.getMessage());
            }
        }
    }

    private void ensureDataFolder() {
        try {
            Files.createDirectories(plugin.getDataFolder().toPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create plugin data folder: " + e.getMessage());
        }
    }
}
