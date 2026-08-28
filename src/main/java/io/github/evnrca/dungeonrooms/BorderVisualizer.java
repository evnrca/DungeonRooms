package io.github.evnrca.dungeonrooms;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Renders particle borders along the edges of a WorldGuard region's bounding box.
 * <p>
 * Visualization is per-player: each enabled player gets their own repeating task
 * drawing particles visible only to that player. Tasks are cancelled cleanly on
 * toggle-off, logout, world change, and plugin disable.
 *
 * @author evnrca
 */
public final class BorderVisualizer {

    private final DungeonRooms plugin;
    private final ConfigManager config;
    private final RoomManager roomManager;
    private final WorldGuardHook worldGuardHook;

    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    public BorderVisualizer(DungeonRooms plugin, ConfigManager config,
                            RoomManager roomManager, WorldGuardHook worldGuardHook) {
        this.plugin = plugin;
        this.config = config;
        this.roomManager = roomManager;
        this.worldGuardHook = worldGuardHook;
    }

    /**
     * Toggles border visualization for a player.
     */
    public void toggle(Player player) {
        if (!config.isBorderVisualizerEnabled()) {
            player.sendMessage(color(config.getPrefix() + config.getBorderFeatureDisabled()));
            return;
        }
        if (isEnabled(player)) {
            disable(player);
            player.sendMessage(color(config.getPrefix() + config.getBorderToggledOff()));
            return;
        }

        String roomKey = findRoomKey(player);
        if (roomKey == null) {
            player.sendMessage(color(config.getPrefix() + config.getBorderNotInRegion()));
            return;
        }

        startTask(player, roomKey);
        player.sendMessage(color(config.getPrefix() + config.getBorderToggledOn()));
    }

    /**
     * Disables visualization for a player, cancelling their task.
     */
    public void disable(Player player) {
        BukkitTask task = tasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Disables visualization for all players. Called on plugin disable.
     */
    public void disableAll() {
        for (BukkitTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
    }

    /**
     * @return {@code true} if the player currently has visualization enabled
     */
    public boolean isEnabled(Player player) {
        return tasks.containsKey(player.getUniqueId());
    }

    /**
     * Refreshes the border for a player after they cross a region boundary.
     */
    public void refreshRegion(Player player) {
        if (!isEnabled(player)) {
            return;
        }
        String roomKey = findRoomKey(player);
        if (roomKey == null) {
            disable(player);
            return;
        }
        restartTask(player, roomKey);
    }

    /**
     * Refreshes the border for all players visualizing the given room key.
     * Called by {@link RoomManager} after a reload so stale bounding boxes are corrected.
     */
    public void refreshRegion(String roomKey) {
        for (Map.Entry<UUID, BukkitTask> entry : new java.util.ArrayList<>(tasks.entrySet())) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                entry.getValue().cancel();
                tasks.remove(entry.getKey());
                continue;
            }
            String current = findRoomKey(player);
            if (roomKey.equals(current)) {
                restartTask(player, current);
            }
        }
    }

    private String findRoomKey(Player player) {
        Location loc = player.getLocation();
        for (Map.Entry<String, RoomManager.RoomData> entry : roomManager.getRooms().entrySet()) {
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

    private void startTask(Player player, String roomKey) {
        BukkitTask existing = tasks.get(player.getUniqueId());
        if (existing != null) {
            existing.cancel();
        }
        UUID id = player.getUniqueId();
        tasks.put(id, startRenderTask(id, roomKey));
    }

    private void restartTask(Player player, String roomKey) {
        BukkitTask existing = tasks.get(player.getUniqueId());
        if (existing != null) {
            existing.cancel();
        }
        UUID id = player.getUniqueId();
        tasks.put(id, startRenderTask(id, roomKey));
    }

    private BukkitTask startRenderTask(UUID id, String roomKey) {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player player = Bukkit.getPlayer(id);
            if (player == null) {
                BukkitTask task = tasks.remove(id);
                if (task != null) {
                    task.cancel();
                }
                return;
            }
            if (!player.getWorld().getName().equals(roomKey.split(":", 2)[0])) {
                return;
            }
            renderRoom(player, roomKey);
        }, 0L, config.getBorderIntervalTicks());
    }

    private void renderRoom(Player player, String roomKey) {
        String worldName = roomKey.split(":", 2)[0];
        String regionName = roomKey.split(":", 2)[1];

        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return;
        }
        BoundingBox box = worldGuardHook.getRegionBoundingBox(world, regionName);
        if (box == null) {
            return;
        }

        Particle particle = parseParticle(config.getBorderParticleType());
        if (particle == null) {
            return;
        }
        double density = config.getBorderParticleDensity();
        double step = Math.max(0.1, density);

        drawEdges(player, particle, box, step);
    }

    private void drawEdges(Player player, Particle particle, BoundingBox box, double step) {
        double x1 = box.getMinX();
        double y1 = box.getMinY();
        double z1 = box.getMinZ();
        double x2 = box.getMaxX();
        double y2 = box.getMaxY();
        double z2 = box.getMaxZ();

        // bottom face (y1)
        drawLine(player, particle, x1, y1, z1, x2, y1, z1, step);
        drawLine(player, particle, x2, y1, z1, x2, y1, z2, step);
        drawLine(player, particle, x2, y1, z2, x1, y1, z2, step);
        drawLine(player, particle, x1, y1, z2, x1, y1, z1, step);

        // top face (y2 - 1, top block edge)
        double yTop = y2 - 1.0;
        drawLine(player, particle, x1, yTop, z1, x2, yTop, z1, step);
        drawLine(player, particle, x2, yTop, z1, x2, yTop, z2, step);
        drawLine(player, particle, x2, yTop, z2, x1, yTop, z2, step);
        drawLine(player, particle, x1, yTop, z2, x1, yTop, z1, step);

        // vertical corners
        drawLine(player, particle, x1, y1, z1, x1, yTop, z1, step);
        drawLine(player, particle, x2, y1, z1, x2, yTop, z1, step);
        drawLine(player, particle, x2, y1, z2, x2, yTop, z2, step);
        drawLine(player, particle, x1, y1, z2, x1, yTop, z2, step);
    }

    private void drawLine(Player player, Particle particle,
                          double x1, double y1, double z1,
                          double x2, double y2, double z2, double step) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int points = Math.max(1, (int) Math.ceil(dist / step));
        for (int i = 0; i <= points; i++) {
            double t = (double) i / points;
            player.spawnParticle(particle, x1 + dx * t, y1 + dy * t, z1 + dz * t, 1, 0, 0, 0, 0);
        }
    }

    private Particle parseParticle(String name) {
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String color(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }
}