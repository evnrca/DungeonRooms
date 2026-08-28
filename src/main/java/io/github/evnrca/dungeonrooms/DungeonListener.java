package io.github.evnrca.dungeonrooms;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Handles all movement, death, and lifecycle events related to dungeon XP.
 * <p>
 * Every entry point returns immediately unless the relevant world is a dungeon world.
 * This guarantees zero overhead in non-dungeon worlds.
 *
 * @author evnrca
 */
public final class DungeonListener implements Listener {

    private final ConfigManager config;
    private final RoomManager roomManager;
    private final ProgressManager progress;
    private final WorldGuardHook worldGuardHook;
    private final MythicMobsHook mythicMobsHook;
    private final DenialHandler denialHandler;
    private final BorderVisualizer borderVisualizer;
    private final Map<java.util.UUID, Long> lastChatProgress = new java.util.HashMap<>();

    public DungeonListener(ConfigManager config, RoomManager roomManager,
                           ProgressManager progress, WorldGuardHook worldGuardHook,
                           MythicMobsHook mythicMobsHook, DenialHandler denialHandler,
                           BorderVisualizer borderVisualizer) {
        this.config = config;
        this.roomManager = roomManager;
        this.progress = progress;
        this.worldGuardHook = worldGuardHook;
        this.mythicMobsHook = mythicMobsHook;
        this.denialHandler = denialHandler;
        this.borderVisualizer = borderVisualizer;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!roomManager.isDungeonWorld(player.getWorld().getName())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        String fromRoom = findRoomKey(from);
        String toRoom = findRoomKey(to);

        boolean changed = !java.util.Objects.equals(fromRoom, toRoom);

        if (changed) {
            if (toRoom != null && fromRoom == null) {
                handleEntry(player, to, toRoom);
            } else if (toRoom == null && fromRoom != null) {
                progress.setLastLocation(player, from);
            } else if (toRoom != null && fromRoom != null) {
                progress.setLastLocation(player, from);
                handleEntry(player, to, toRoom);
            }
            borderVisualizer.refreshRegion(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        String origin = event.getFrom().getName();
        if (!roomManager.isDungeonWorld(origin)) {
            return;
        }

        Player player = event.getPlayer();
        progress.resetPlayer(player.getUniqueId());
        borderVisualizer.disable(player);
        player.sendMessage(color(config.getPrefix() + config.getProgressResetWorldExit()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        if (!roomManager.isDungeonWorld(killer.getWorld().getName())) {
            return;
        }
        if (!mythicMobsHook.isMythicMob(event.getEntity())) {
            return;
        }

        Location loc = killer.getLocation();
        String roomKey = findRoomKey(loc);
        if (roomKey == null) {
            return;
        }

        RoomManager.RoomData room = roomManager.getRoom(
                roomKey.split(":", 2)[0], roomKey.split(":", 2)[1]);
        if (room == null) {
            return;
        }

        progress.addKill(killer.getUniqueId(), roomKey);
        int current = progress.getKills(killer.getUniqueId(), roomKey);
        int required = room.requiredKills;

        showProgress(killer, current, required);
        checkCompletion(killer, roomKey, room, current, required);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        resetOnDeath(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        progress.resetPlayer(player.getUniqueId());
        borderVisualizer.disable(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String fromWorld = event.getFrom().getWorld() == null ? null : event.getFrom().getWorld().getName();
        String toWorld = event.getTo() == null ? null : event.getTo().getWorld().getName();

        if (fromWorld == null || !roomManager.isDungeonWorld(fromWorld)) {
            return;
        }
        if (toWorld != null && toWorld.equals(fromWorld)) {
            return;
        }

        progress.resetPlayer(player.getUniqueId());
        borderVisualizer.disable(player);
        player.sendMessage(color(config.getPrefix() + config.getProgressResetTeleport()));
    }

    private void handleEntry(Player player, Location to, String toRoom) {
        String[] parts = toRoom.split(":", 2);
        if (parts.length < 2) {
            return;
        }
        String world = parts[0];
        String region = parts[1];

        RoomManager.RoomData room = roomManager.getRoom(world, region);
        if (room == null) {
            return;
        }

        int index = roomManager.getRoomIndex(world, region);

        if (index == 0 || progress.isUnlocked(player.getUniqueId(), toRoom)) {
            progress.setLastLocation(player, to);
            return;
        }

        int previousIndex = index - 1;
        RoomManager.RoomData previous = getRoomByIndex(previousIndex);
        if (previous == null) {
            return;
        }

        int previousKey = progress.getKills(player.getUniqueId(), previous.key());
        if (previousKey >= previous.requiredKills) {
            progress.unlock(player.getUniqueId(), toRoom);
            progress.setLastLocation(player, to);
            return;
        }

        int remaining = previous.requiredKills - previousKey;
        denialHandler.deny(player, to, remaining, region);
    }

    private RoomManager.RoomData getRoomByIndex(int index) {
        int i = 0;
        for (RoomManager.RoomData data : roomManager.getRooms().values()) {
            if (i == index) {
                return data;
            }
            i++;
        }
        return null;
    }

    private String findRoomKey(Location loc) {
        for (java.util.Map.Entry<String, RoomManager.RoomData> entry : roomManager.getRooms().entrySet()) {
            RoomManager.RoomData data = entry.getValue();
            if (!data.world.equals(loc.getWorld().getName())) {
                continue;
            }
            if (worldGuardHook.isInRegion(loc, data.region)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void showProgress(Player player, int current, int required) {
        if (config.isActionBarEnabled()) {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent(color(
                            config.getActionBarFormat()
                                    .replace("{current}", String.valueOf(current))
                                    .replace("{required}", String.valueOf(required)))));
        }
        if (config.isChatEnabled()) {
            long now = System.currentTimeMillis();
            Long last = lastChatProgress.get(player.getUniqueId());
            int cooldownMs = config.getChatCooldown() * 1000;
            if (last == null || now - last >= cooldownMs) {
                lastChatProgress.put(player.getUniqueId(), now);
                player.sendMessage(color(config.getPrefix() + config.getChatFormat()
                        .replace("{current}", String.valueOf(current))
                        .replace("{required}", String.valueOf(required))));
            }
        }
    }

    private void checkCompletion(Player player, String roomKey, RoomManager.RoomData room,
                                 int current, int required) {
        if (current >= required && !progress.isUnlocked(player.getUniqueId(), roomKey)) {
            progress.unlock(player.getUniqueId(), roomKey);
            player.sendMessage(color(config.getPrefix() + config.getCompleted()
                    .replace("{region}", room.region)));
        }
    }

    private void resetOnDeath(Player player) {
        progress.resetPlayer(player.getUniqueId());
        borderVisualizer.disable(player);
        player.sendMessage(color(config.getPrefix() + config.getProgressResetDeath()));
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}