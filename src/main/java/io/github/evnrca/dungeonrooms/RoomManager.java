package io.github.evnrca.dungeonrooms;

import org.bukkit.World;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages dungeon rooms across all worlds.
 * <p>
 * Rooms are stored in a {@link LinkedHashMap} keyed by {@code worldName:regionName}
 * so the same region name can exist in different worlds. Registration order is
 * preserved and used to drive room progression.
 *
 * @author evnrca
 */
public final class RoomManager {

    /**
     * Internal representation of a single registered room.
     */
    public static final class RoomData {
        public final String world;
        public final String region;
        public final int requiredKills;

        public RoomData(String world, String region, int requiredKills) {
            this.world = world;
            this.region = region;
            this.requiredKills = requiredKills;
        }

        public String key() {
            return world + ":" + region;
        }
    }

    private final WorldGuardHook worldGuardHook;
    private final ConfigManager config;
    private final Map<String, RoomData> rooms = new LinkedHashMap<>();
    private final Map<String, Set<String>> worldToRooms = new LinkedHashMap<>();
    private final Set<String> dungeonWorlds = new java.util.HashSet<>();

    public RoomManager(WorldGuardHook worldGuardHook, ConfigManager config) {
        this.worldGuardHook = worldGuardHook;
        this.config = config;
        loadRooms();
    }

    /**
     * Registers a room. Validates that the world and region both exist.
     *
     * @return the registered room, or {@code null} if validation failed
     */
    public RoomData addRoom(String world, String region, int kills) {
        World w = org.bukkit.Bukkit.getWorld(world);
        if (w == null) {
            return null;
        }
        if (worldGuardHook.getRegion(w, region) == null) {
            return null;
        }

        RoomData data = new RoomData(world, region, kills);
        rooms.put(data.key(), data);
        worldToRooms.computeIfAbsent(world, k -> new java.util.HashSet<>()).add(region);
        rebuildWorldCache();
        config.saveRooms(rooms);
        return data;
    }

    /**
     * Removes a room by world and region name.
     *
     * @return {@code true} if a room was removed
     */
    public boolean removeRoom(String world, String region) {
        String key = world + ":" + region;
        RoomData removed = rooms.remove(key);
        if (removed == null) {
            return false;
        }
        Set<String> set = worldToRooms.get(world);
        if (set != null) {
            set.remove(region);
            if (set.isEmpty()) {
                worldToRooms.remove(world);
            }
        }
        rebuildWorldCache();
        config.saveRooms(rooms);
        return true;
    }

    /**
     * @return a live, ordered view of all registered rooms
     */
    public Map<String, RoomData> getRooms() {
        return rooms;
    }

    /**
     * @return the index of the room in that world's registration order, or {@code -1} if not registered
     */
    public int getRoomIndex(String world, String region) {
        String key = world + ":" + region;
        int index = 0;
        for (Map.Entry<String, RoomData> entry : rooms.entrySet()) {
            if (!entry.getValue().world.equals(world)) {
                continue;
            }
            if (entry.getKey().equals(key)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * @return the previous room in the same world's registration order, or {@code null}
     */
    public RoomData getPreviousRoom(String world, String region) {
        RoomData previous = null;
        String key = world + ":" + region;
        for (Map.Entry<String, RoomData> entry : rooms.entrySet()) {
            RoomData current = entry.getValue();
            if (!current.world.equals(world)) {
                continue;
            }
            if (entry.getKey().equals(key)) {
                return previous;
            }
            previous = current;
        }
        return null;
    }

    /**
     * @return the required kill count for a room, or {@code -1} if not registered
     */
    public int getRequired(String world, String region) {
        RoomData data = rooms.get(world + ":" + region);
        return data == null ? -1 : data.requiredKills;
    }

    /**
     * @return the room data for a key, or {@code null}
     */
    public RoomData getRoom(String world, String region) {
        return rooms.get(world + ":" + region);
    }

    /**
     * @return {@code true} if the given world contains at least one registered room
     */
    public boolean isDungeonWorld(String world) {
        return dungeonWorlds.contains(world);
    }

    /**
     * @return an unmodifiable snapshot of the cached dungeon world names
     */
    public Set<String> getDungeonWorlds() {
        return java.util.Collections.unmodifiableSet(dungeonWorlds);
    }

    /**
     * Re-fetches fresh WorldGuard region references from each room's stored world.
     * Does NOT remove and re-add rooms, preserving registration order.
     */
    public void refreshRegions() {
        if (rooms.isEmpty()) {
            loadRooms();
        }
        for (RoomData data : rooms.values()) {
            World w = org.bukkit.Bukkit.getWorld(data.world);
            if (w == null) {
                continue;
            }
            worldGuardHook.getRegion(w, data.region);
        }
        rebuildWorldCache();
    }

    /**
     * Reloads rooms from config.yml, preserving the order stored in the file.
     */
    public void loadRooms() {
        rooms.clear();
        worldToRooms.clear();
        for (ConfigManager.StoredRoom stored : config.loadRooms()) {
            RoomData data = new RoomData(stored.world, stored.region, stored.requiredKills);
            rooms.put(data.key(), data);
            worldToRooms.computeIfAbsent(stored.world, k -> new java.util.HashSet<>()).add(stored.region);
        }
        rebuildWorldCache();
    }

    private void rebuildWorldCache() {
        dungeonWorlds.clear();
        for (String world : worldToRooms.keySet()) {
            dungeonWorlds.add(world);
        }
    }
}
