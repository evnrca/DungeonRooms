package io.github.evnrca.dungeonrooms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages the in-memory dungeon and room cache.
 *
 * @author evnrca
 */
public final class DungeonManager {

    /**
     * Represents one registered dungeon boundary and its ordered rooms.
     */
    public static final class DungeonData {
        public final String dungeonName;
        public final String world;
        public final String region;
        public String spawnRegion;
        public String spawnWorld;
        public Location spawnLocation;
        public final LinkedHashMap<String, RoomData> rooms = new LinkedHashMap<>();

        public DungeonData(String dungeonName, String world, String region,
                           String spawnRegion, String spawnWorld, Location spawnLocation) {
            this.dungeonName = dungeonName;
            this.world = world;
            this.region = region;
            this.spawnRegion = spawnRegion;
            this.spawnWorld = spawnWorld;
            this.spawnLocation = spawnLocation == null ? null : spawnLocation.clone();
        }
    }

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Represents one room inside a dungeon.
     */
    public static final class RoomData {
        public final String dungeonName;
        public final String world;
        public final String region;
        public int requiredKills;
        public int sequence;

        public RoomData(String dungeonName, String world, String region, int requiredKills, int sequence) {
            this.dungeonName = dungeonName;
            this.world = world;
            this.region = region;
            this.requiredKills = requiredKills;
            this.sequence = sequence;
        }

        public String key() {
            return dungeonName + ":" + region;
        }
    }

    private final WorldGuardHook worldGuardHook;
    private final DungeonDataManager dungeonData;
    private final LinkedHashMap<String, DungeonData> dungeons = new LinkedHashMap<>();
    private final Set<String> dungeonWorlds = new HashSet<>();

    public DungeonManager(WorldGuardHook worldGuardHook, DungeonDataManager dungeonData) {
        this.worldGuardHook = worldGuardHook;
        this.dungeonData = dungeonData;
    }

    /**
     * Loads dungeon definitions from dungeons.json into memory.
     */
    public void loadFromStorage(Runnable callback) {
        lock.writeLock().lock();
        try {
            dungeons.clear();
            for (Map.Entry<String, DungeonDataManager.StoredDungeon> entry : dungeonData.loadAllSync().entrySet()) {
                DungeonDataManager.StoredDungeon stored = entry.getValue();
                DungeonData dungeon = new DungeonData(entry.getKey(), stored.world, stored.region,
                        stored.spawnRegion, stored.spawnWorld, dungeonData.toLocation(stored.spawnLocation));
                if (stored.rooms != null) {
                    stored.rooms.stream()
                            .sorted(java.util.Comparator.comparingInt(room -> room.sequence))
                            .forEach(room -> dungeon.rooms.put(room.region, new RoomData(
                                    entry.getKey(), room.world, room.region, room.requiredKills, room.sequence)));
                }
                dungeons.put(entry.getKey(), dungeon);
            }
            rebuildWorldCache();
        } finally {
            lock.writeLock().unlock();
        }
        if (callback != null) {
            callback.run();
        }
    }

    public boolean createDungeon(String world, String region, String dungeonName) {
        lock.writeLock().lock();
        try {
            if (dungeons.containsKey(dungeonName)) {
                return false;
            }
            if (!isValidRegion(world, region)) {
                return false;
            }
            DungeonData dungeon = new DungeonData(dungeonName, world, region, null, null, null);
            dungeons.put(dungeonName, dungeon);
            rebuildWorldCache();
        } finally {
            lock.writeLock().unlock();
        }
        saveAsync();
        return true;
    }

    public boolean removeDungeon(String dungeonName) {
        lock.writeLock().lock();
        try {
            DungeonData removed = dungeons.remove(dungeonName);
            if (removed == null) {
                return false;
            }
            rebuildWorldCache();
        } finally {
            lock.writeLock().unlock();
        }
        saveAsync();
        return true;
    }

    public DungeonData getDungeon(String dungeonName) {
        lock.readLock().lock();
        try {
            return dungeons.get(dungeonName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<String, DungeonData> getDungeons() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableMap(new LinkedHashMap<>(dungeons));
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean addRoom(String dungeonName, String region, int kills) {
        lock.writeLock().lock();
        try {
            DungeonData dungeon = dungeons.get(dungeonName);
            if (dungeon == null || dungeon.spawnRegion == null || dungeon.spawnWorld == null) {
                return false;
            }
            if (dungeon.rooms.containsKey(region) || !isValidRegion(dungeon.world, region)) {
                return false;
            }
            int sequence = dungeon.rooms.size();
            RoomData room = new RoomData(dungeonName, dungeon.world, region, kills, sequence);
            dungeon.rooms.put(region, room);
            rebuildWorldCache();
        } finally {
            lock.writeLock().unlock();
        }
        saveAsync();
        return true;
    }

    public boolean removeRoom(String dungeonName, String region) {
        lock.writeLock().lock();
        try {
            DungeonData dungeon = dungeons.get(dungeonName);
            if (dungeon == null || dungeon.rooms.remove(region) == null) {
                return false;
            }
            resequence(dungeon);
            rebuildWorldCache();
        } finally {
            lock.writeLock().unlock();
        }
        saveAsync();
        return true;
    }

    public boolean editRoomKills(String dungeonName, String region, int kills) {
        lock.writeLock().lock();
        try {
            DungeonData dungeon = dungeons.get(dungeonName);
            if (dungeon == null) {
                return false;
            }
            RoomData room = dungeon.rooms.get(region);
            if (room == null) {
                return false;
            }
            room.requiredKills = kills;
        } finally {
            lock.writeLock().unlock();
        }
        saveAsync();
        return true;
    }

    public boolean setSpawnRegion(String dungeonName, String world, String region) {
        lock.writeLock().lock();
        try {
            DungeonData dungeon = dungeons.get(dungeonName);
            if (dungeon == null || !isValidRegion(world, region)) {
                return false;
            }
            dungeon.spawnWorld = world;
            dungeon.spawnRegion = region;
            rebuildWorldCache();
        } finally {
            lock.writeLock().unlock();
        }
        saveAsync();
        return true;
    }

    public boolean setSpawnLocation(String dungeonName, Location location) {
        lock.writeLock().lock();
        try {
            DungeonData dungeon = dungeons.get(dungeonName);
            if (dungeon == null || location.getWorld() == null || dungeon.spawnRegion == null) {
                return false;
            }
            if (!isSpawnRegion(location, dungeonName)) {
                return false;
            }
            dungeon.spawnWorld = location.getWorld().getName();
            dungeon.spawnLocation = location.clone();
        } finally {
            lock.writeLock().unlock();
        }
        saveAsync();
        return true;
    }

    public RoomData getRoomByLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        lock.readLock().lock();
        try {
            for (DungeonData dungeon : dungeons.values()) {
                for (RoomData room : dungeon.rooms.values()) {
                    if (room.world.equals(location.getWorld().getName())
                            && worldGuardHook.isInRegion(location, room.region)) {
                        return room;
                    }
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public DungeonData getDungeonByLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        lock.readLock().lock();
        try {
            for (DungeonData dungeon : dungeons.values()) {
                if (dungeon.world.equals(location.getWorld().getName())
                        && worldGuardHook.isInRegion(location, dungeon.region)) {
                    return dungeon;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isSpawnRegion(Location location, String dungeonName) {
        lock.readLock().lock();
        try {
            DungeonData dungeon = dungeons.get(dungeonName);
            if (dungeon == null || dungeon.spawnWorld == null || dungeon.spawnRegion == null
                    || location == null || location.getWorld() == null) {
                return false;
            }
            return dungeon.spawnWorld.equals(location.getWorld().getName())
                    && worldGuardHook.isInRegion(location, dungeon.spawnRegion);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isDungeonWorld(String world) {
        lock.readLock().lock();
        try {
            return dungeonWorlds.contains(world);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<String> getDungeonWorlds() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableSet(new HashSet<>(dungeonWorlds));
        } finally {
            lock.readLock().unlock();
        }
    }

    public RoomData getPreviousRoom(RoomData room) {
        lock.readLock().lock();
        try {
            DungeonData dungeon = dungeons.get(room.dungeonName);
            if (dungeon == null || room.sequence <= 0) {
                return null;
            }
            for (RoomData candidate : dungeon.rooms.values()) {
                if (candidate.sequence == room.sequence - 1) {
                    return candidate;
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void refreshRegions() {
        lock.writeLock().lock();
        try {
            rebuildWorldCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void saveSync() {
        lock.readLock().lock();
        try {
            dungeonData.saveSync(new LinkedHashMap<>(dungeons));
        } finally {
            lock.readLock().unlock();
        }
    }

    private void saveAsync() {
        lock.readLock().lock();
        try {
            dungeonData.saveAsync(new LinkedHashMap<>(dungeons));
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean isValidRegion(String worldName, String region) {
        World world = Bukkit.getWorld(worldName);
        return world != null && worldGuardHook.getRegion(world, region) != null;
    }

    private void resequence(DungeonData dungeon) {
        int sequence = 0;
        for (RoomData room : dungeon.rooms.values()) {
            room.sequence = sequence++;
        }
    }

    private void rebuildWorldCache() {
        dungeonWorlds.clear();
        for (DungeonData dungeon : dungeons.values()) {
            dungeonWorlds.add(dungeon.world);
            if (dungeon.spawnWorld != null) {
                dungeonWorlds.add(dungeon.spawnWorld);
            }
            for (RoomData room : dungeon.rooms.values()) {
                dungeonWorlds.add(room.world);
            }
        }
    }
}
