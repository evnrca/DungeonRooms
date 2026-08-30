package io.github.evnrca.dungeonrooms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
    private final LinkedHashMap<String, DungeonData> dungeons = new LinkedHashMap<>();
    private final Set<String> dungeonWorlds = new HashSet<>();

    public DungeonManager(WorldGuardHook worldGuardHook) {
        this.worldGuardHook = worldGuardHook;
    }

    /**
     * Keeps the old async-load entry point while dungeon definitions are managed in memory.
     */
    public void loadFromDatabase(Runnable callback) {
        rebuildWorldCache();
        if (callback != null) {
            callback.run();
        }
    }

    public boolean createDungeon(String world, String region, String dungeonName) {
        if (dungeons.containsKey(dungeonName)) {
            return false;
        }
        if (!isValidRegion(world, region)) {
            return false;
        }
        DungeonData dungeon = new DungeonData(dungeonName, world, region, null, null, null);
        dungeons.put(dungeonName, dungeon);
        rebuildWorldCache();
        return true;
    }

    public boolean removeDungeon(String dungeonName) {
        DungeonData removed = dungeons.remove(dungeonName);
        if (removed == null) {
            return false;
        }
        rebuildWorldCache();
        return true;
    }

    public DungeonData getDungeon(String dungeonName) {
        return dungeons.get(dungeonName);
    }

    public Map<String, DungeonData> getDungeons() {
        return Collections.unmodifiableMap(dungeons);
    }

    public boolean addRoom(String dungeonName, String region, int kills) {
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
        return true;
    }

    public boolean removeRoom(String dungeonName, String region) {
        DungeonData dungeon = dungeons.get(dungeonName);
        if (dungeon == null || dungeon.rooms.remove(region) == null) {
            return false;
        }
        resequence(dungeon);
        rebuildWorldCache();
        return true;
    }

    public boolean editRoomKills(String dungeonName, String region, int kills) {
        DungeonData dungeon = dungeons.get(dungeonName);
        if (dungeon == null) {
            return false;
        }
        RoomData room = dungeon.rooms.get(region);
        if (room == null) {
            return false;
        }
        room.requiredKills = kills;
        return true;
    }

    public boolean setSpawnRegion(String dungeonName, String world, String region) {
        DungeonData dungeon = dungeons.get(dungeonName);
        if (dungeon == null || !isValidRegion(world, region)) {
            return false;
        }
        dungeon.spawnWorld = world;
        dungeon.spawnRegion = region;
        rebuildWorldCache();
        return true;
    }

    public boolean setSpawnLocation(String dungeonName, Location location) {
        DungeonData dungeon = dungeons.get(dungeonName);
        if (dungeon == null || location.getWorld() == null || dungeon.spawnRegion == null) {
            return false;
        }
        if (!isSpawnRegion(location, dungeonName)) {
            return false;
        }
        dungeon.spawnWorld = location.getWorld().getName();
        dungeon.spawnLocation = location.clone();
        return true;
    }

    public RoomData getRoomByLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        for (DungeonData dungeon : dungeons.values()) {
            for (RoomData room : dungeon.rooms.values()) {
                if (room.world.equals(location.getWorld().getName())
                        && worldGuardHook.isInRegion(location, room.region)) {
                    return room;
                }
            }
        }
        return null;
    }

    public DungeonData getDungeonByLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        for (DungeonData dungeon : dungeons.values()) {
            if (dungeon.world.equals(location.getWorld().getName())
                    && worldGuardHook.isInRegion(location, dungeon.region)) {
                return dungeon;
            }
        }
        return null;
    }

    public boolean isSpawnRegion(Location location, String dungeonName) {
        DungeonData dungeon = dungeons.get(dungeonName);
        if (dungeon == null || dungeon.spawnWorld == null || dungeon.spawnRegion == null
                || location == null || location.getWorld() == null) {
            return false;
        }
        return dungeon.spawnWorld.equals(location.getWorld().getName())
                && worldGuardHook.isInRegion(location, dungeon.spawnRegion);
    }

    public boolean isDungeonWorld(String world) {
        return dungeonWorlds.contains(world);
    }

    public Set<String> getDungeonWorlds() {
        return Collections.unmodifiableSet(dungeonWorlds);
    }

    public RoomData getPreviousRoom(RoomData room) {
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
    }

    public void refreshRegions() {
        rebuildWorldCache();
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
