package io.github.evnrca.dungeonrooms;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks per-player dungeon progress entirely in memory.
 * <p>
 * No persistence across restarts is performed or intended. Room keys follow the
 * {@code worldName:regionName} format used by {@link RoomManager}.
 *
 * @author evnrca
 */
public final class ProgressManager {

    private final Map<UUID, Map<String, Integer>> killCounts = new HashMap<>();
    private final Map<UUID, Set<String>> unlockedRooms = new HashMap<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    /**
     * Increments the kill count for a player in a room.
     */
    public void addKill(UUID player, String roomKey) {
        killCounts.computeIfAbsent(player, k -> new HashMap<>())
                .merge(roomKey, 1, Integer::sum);
    }

    /**
     * @return the current kill count for a player in a room
     */
    public int getKills(UUID player, String roomKey) {
        Map<String, Integer> map = killCounts.get(player);
        return map == null ? 0 : map.getOrDefault(roomKey, 0);
    }

    /**
     * @return {@code true} if the player has unlocked the given room
     */
    public boolean isUnlocked(UUID player, String roomKey) {
        Set<String> set = unlockedRooms.get(player);
        return set != null && set.contains(roomKey);
    }

    /**
     * Permanently marks a room as unlocked for a player (until reset).
     */
    public void unlock(UUID player, String roomKey) {
        unlockedRooms.computeIfAbsent(player, k -> new HashSet<>()).add(roomKey);
    }

    /**
     * @return the player's last valid location, or {@code null}
     */
    public Location getLastLocation(UUID player) {
        return lastLocations.get(player);
    }

    public void setLastLocation(UUID player, Location location) {
        lastLocations.put(player, location == null ? null : location.clone());
    }

    /**
     * Clears all progress (kills, unlocks, last location) for a player.
     */
    public void resetPlayer(UUID player) {
        killCounts.remove(player);
        unlockedRooms.remove(player);
        lastLocations.remove(player);
    }

    /**
     * Resets only a single region's kill count and unlock state for a player.
     * Does not touch the last valid location or other regions.
     */
    public void resetPlayerRegion(UUID player, String roomKey) {
        Map<String, Integer> map = killCounts.get(player);
        if (map != null) {
            map.remove(roomKey);
        }
        Set<String> set = unlockedRooms.get(player);
        if (set != null) {
            set.remove(roomKey);
        }
    }

    /**
     * Clears all in-memory progress for all players.
     */
    public void resetAll() {
        killCounts.clear();
        unlockedRooms.clear();
        lastLocations.clear();
    }
}
