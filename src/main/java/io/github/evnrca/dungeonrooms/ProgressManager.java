package io.github.evnrca.dungeonrooms;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks cached per-player dungeon progress backed by JSON.
 * <p>
 * Progress keys use {@code dungeonName:regionName}. All JSON data is loaded
 * synchronously on plugin enable and saved asynchronously on runtime changes.
 *
 * @author evnrca
 */
public final class ProgressManager {

    private final PlayerDataManager playerData;
    private final Map<UUID, Map<String, Integer>> killCounts = new HashMap<>();
    private final Map<UUID, Set<String>> unlockedRooms = new HashMap<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    public ProgressManager(PlayerDataManager playerData) {
        this.playerData = playerData;
    }

    /**
     * Loads all persisted player progress into the in-memory cache.
     */
    public void loadAll(Map<UUID, Map<String, Map<String, int[]>>> persisted) {
        killCounts.clear();
        unlockedRooms.clear();
        for (Map.Entry<UUID, Map<String, Map<String, int[]>>> playerEntry : persisted.entrySet()) {
            Map<String, Integer> kills = new HashMap<>();
            Set<String> unlocked = new HashSet<>();
            for (Map.Entry<String, Map<String, int[]>> dungeonEntry : playerEntry.getValue().entrySet()) {
                for (Map.Entry<String, int[]> roomEntry : dungeonEntry.getValue().entrySet()) {
                    int[] values = roomEntry.getValue();
                    String key = key(dungeonEntry.getKey(), roomEntry.getKey());
                    kills.put(key, values.length > 0 ? values[0] : 0);
                    if (values.length > 1 && values[1] == 1) {
                        unlocked.add(key);
                    }
                }
            }
            killCounts.put(playerEntry.getKey(), kills);
            unlockedRooms.put(playerEntry.getKey(), unlocked);
        }
    }

    public void loadPlayer(UUID player) {
        killCounts.computeIfAbsent(player, key -> new HashMap<>());
        unlockedRooms.computeIfAbsent(player, key -> new HashSet<>());
    }

    /**
     * Increments a player's kill count for a dungeon room and persists it.
     *
     * @return the new cached kill count
     */
    public int addKill(UUID player, String dungeonName, String region) {
        String key = key(dungeonName, region);
        int kills = killCounts.computeIfAbsent(player, k -> new HashMap<>())
                .merge(key, 1, Integer::sum);
        playerData.saveProgressAsync(player, dungeonName, region, kills,
                isUnlocked(player, dungeonName, region));
        return kills;
    }

    /**
     * Compatibility wrapper for key-based room progress.
     */
    public void addKill(UUID player, String roomKey) {
        String[] parts = splitKey(roomKey);
        addKill(player, parts[0], parts[1]);
    }

    public int getKills(UUID player, String dungeonName, String region) {
        return getKills(player, key(dungeonName, region));
    }

    public int getKills(UUID player, String roomKey) {
        Map<String, Integer> map = killCounts.get(player);
        return map == null ? 0 : map.getOrDefault(roomKey, 0);
    }

    public boolean isUnlocked(UUID player, String dungeonName, String region) {
        return isUnlocked(player, key(dungeonName, region));
    }

    public boolean isUnlocked(UUID player, String roomKey) {
        Set<String> set = unlockedRooms.get(player);
        return set != null && set.contains(roomKey);
    }

    public void unlock(UUID player, String dungeonName, String region) {
        String key = key(dungeonName, region);
        unlockedRooms.computeIfAbsent(player, k -> new HashSet<>()).add(key);
        playerData.saveProgressAsync(player, dungeonName, region,
                getKills(player, dungeonName, region), true);
    }

    /**
     * Compatibility wrapper for key-based unlocks.
     */
    public void unlock(UUID player, String roomKey) {
        String[] parts = splitKey(roomKey);
        unlock(player, parts[0], parts[1]);
    }

    public Location getLastLocation(UUID player) {
        return lastLocations.get(player);
    }

    public void setLastLocation(UUID player, Location location) {
        if (location == null) {
            lastLocations.remove(player);
            return;
        }
        lastLocations.put(player, location.clone());
    }

    /**
     * Flushes cached progress to JSON asynchronously.
     */
    public void flushPlayer(UUID player) {
        Map<String, Integer> kills = new HashMap<>(killCounts.getOrDefault(player, Map.of()));
        Set<String> unlockedSet = unlockedRooms.getOrDefault(player, Set.of());
        Map<String, Boolean> unlocked = new HashMap<>();
        for (String key : kills.keySet()) {
            unlocked.put(key, unlockedSet.contains(key));
        }
        for (String key : unlockedSet) {
            unlocked.putIfAbsent(key, true);
            kills.putIfAbsent(key, 0);
        }
        playerData.savePlayerAsync(player, kills, unlocked);
    }

    /**
     * Clears all progress for a player and persists the reset.
     */
    public void resetPlayer(UUID player) {
        killCounts.remove(player);
        unlockedRooms.remove(player);
        lastLocations.remove(player);
        playerData.resetPlayerAsync(player);
    }

    /**
     * Clears progress for one dungeon and persists the reset.
     */
    public void resetPlayerDungeon(UUID player, String dungeonName) {
        Map<String, Integer> kills = killCounts.get(player);
        if (kills != null) {
            kills.keySet().removeIf(key -> key.startsWith(dungeonName + ":"));
        }
        Set<String> unlocked = unlockedRooms.get(player);
        if (unlocked != null) {
            unlocked.removeIf(key -> key.startsWith(dungeonName + ":"));
        }
        playerData.resetPlayerDungeonAsync(player, dungeonName);
    }

    /**
     * Compatibility wrapper for old key-based region resets.
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
        String[] parts = splitKey(roomKey);
        playerData.saveProgressAsync(player, parts[0], parts[1], 0, false);
    }

    /**
     * Clears all cached progress from memory.
     */
    public void resetAll() {
        killCounts.clear();
        unlockedRooms.clear();
        lastLocations.clear();
    }

    /**
     * Writes all cached progress synchronously for plugin shutdown.
     */
    public void saveAllSync() {
        for (UUID player : new HashSet<>(killCounts.keySet())) {
            Map<String, Integer> kills = new HashMap<>(killCounts.getOrDefault(player, Map.of()));
            Set<String> unlockedSet = unlockedRooms.getOrDefault(player, Set.of());
            Map<String, Boolean> unlocked = new HashMap<>();
            for (String key : kills.keySet()) {
                unlocked.put(key, unlockedSet.contains(key));
            }
            for (String key : unlockedSet) {
                unlocked.putIfAbsent(key, true);
                kills.putIfAbsent(key, 0);
            }
            playerData.cachePlayer(player, kills, unlocked);
        }
        playerData.saveSync();
    }

    private String key(String dungeonName, String region) {
        return dungeonName + ":" + region;
    }

    private String[] splitKey(String key) {
        String[] parts = key.split(":", 2);
        if (parts.length == 2) {
            return parts;
        }
        return new String[]{key, ""};
    }
}
