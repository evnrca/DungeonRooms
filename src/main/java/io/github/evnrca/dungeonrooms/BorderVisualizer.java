package io.github.evnrca.dungeonrooms;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Renders private particle borders for dungeon rooms and dungeon boundaries.
 * <p>
 * Each enabled player has one repeating task. Normal mode renders only the room
 * the player is currently inside. All mode renders every registered dungeon and
 * room border in the same task.
 *
 * @author evnrca
 */
public final class BorderVisualizer {

    private enum Mode {
        ROOM,
        ALL
    }

    private final DungeonRooms plugin;
    private final ConfigManager config;
    private final DungeonManager dungeonManager;
    private final WorldGuardHook worldGuardHook;

    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    private final Map<UUID, Mode> modes = new HashMap<>();

    public BorderVisualizer(DungeonRooms plugin, ConfigManager config,
                            DungeonManager dungeonManager, WorldGuardHook worldGuardHook) {
        this.plugin = plugin;
        this.config = config;
        this.dungeonManager = dungeonManager;
        this.worldGuardHook = worldGuardHook;
    }

    /**
     * Toggles visualization for the current room only.
     */
    public void toggle(Player player) {
        if (!config.isBorderVisualizerEnabled()) {
            player.sendMessage(color(config.getPrefix() + config.getBorderFeatureDisabled()));
            return;
        }
        if (isEnabled(player) && modes.get(player.getUniqueId()) == Mode.ROOM) {
            disable(player);
            player.sendMessage(color(config.getPrefix() + config.getBorderToggledOff()));
            return;
        }
        if (dungeonManager.getRoomByLocation(player.getLocation()) == null) {
            player.sendMessage(color(config.getPrefix() + config.getBorderNotInRegion()));
            return;
        }
        startTask(player, Mode.ROOM);
        player.sendMessage(color(config.getPrefix() + config.getBorderToggledOn()));
    }

    /**
     * Toggles visualization for all registered dungeon and room borders.
     */
    public void toggleAll(Player player) {
        if (!config.isBorderVisualizerEnabled()) {
            player.sendMessage(color(config.getPrefix() + config.getBorderFeatureDisabled()));
            return;
        }
        if (isEnabled(player) && modes.get(player.getUniqueId()) == Mode.ALL) {
            disable(player);
            player.sendMessage(color(config.getPrefix() + config.getShowBorderAllOff()));
            return;
        }
        startTask(player, Mode.ALL);
        player.sendMessage(color(config.getPrefix() + config.getShowBorderAllOn()));
    }

    public void disable(Player player) {
        BukkitTask task = tasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        modes.remove(player.getUniqueId());
    }

    public void disableAll() {
        for (BukkitTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();
        modes.clear();
    }

    public boolean isEnabled(Player player) {
        return tasks.containsKey(player.getUniqueId());
    }

    /**
     * Refreshes the player's task after movement or world changes.
     */
    public void refreshRegion(Player player) {
        if (!isEnabled(player)) {
            return;
        }
        Mode mode = modes.get(player.getUniqueId());
        if (mode == Mode.ROOM && dungeonManager.getRoomByLocation(player.getLocation()) == null) {
            disable(player);
            return;
        }
        startTask(player, mode == null ? Mode.ROOM : mode);
    }

    /**
     * Refreshes tasks after dungeon/room data changes.
     */
    public void refreshRegion(String ignoredKey) {
        for (UUID id : new java.util.ArrayList<>(tasks.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player == null) {
                BukkitTask task = tasks.remove(id);
                if (task != null) {
                    task.cancel();
                }
                modes.remove(id);
                continue;
            }
            refreshRegion(player);
        }
    }

    private void startTask(Player player, Mode mode) {
        BukkitTask existing = tasks.remove(player.getUniqueId());
        if (existing != null) {
            existing.cancel();
        }
        UUID id = player.getUniqueId();
        modes.put(id, mode);
        tasks.put(id, Bukkit.getScheduler().runTaskTimer(plugin, () -> render(id),
                0L, config.getBorderIntervalTicks()));
    }

    private void render(UUID id) {
        Player player = Bukkit.getPlayer(id);
        if (player == null) {
            BukkitTask task = tasks.remove(id);
            if (task != null) {
                task.cancel();
            }
            modes.remove(id);
            return;
        }

        Mode mode = modes.get(id);
        if (mode == Mode.ALL) {
            renderAll(player);
        } else {
            renderCurrentRoom(player);
        }
    }

    private void renderCurrentRoom(Player player) {
        DungeonManager.RoomData room = dungeonManager.getRoomByLocation(player.getLocation());
        if (room == null) {
            return;
        }
        renderRegion(player, room.world, room.region, config.getRoomBorderParticleType());
    }

    private void renderAll(Player player) {
        for (DungeonManager.DungeonData dungeon : dungeonManager.getDungeons().values()) {
            renderRegion(player, dungeon.world, dungeon.region, config.getDungeonBorderParticleType());
            for (DungeonManager.RoomData room : dungeon.rooms.values()) {
                renderRegion(player, room.world, room.region, config.getRoomBorderParticleType());
            }
        }
    }

    private void renderRegion(Player player, String worldName, String regionName, String particleName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return;
        }
        BoundingBox box = worldGuardHook.getRegionBoundingBox(world, regionName);
        Particle particle = parseParticle(particleName);
        if (box == null || particle == null) {
            return;
        }
        drawEdges(player, particle, box, Math.max(0.1, config.getBorderParticleDensity()));
    }

    private void drawEdges(Player player, Particle particle, BoundingBox box, double step) {
        double x1 = box.getMinX();
        double y1 = box.getMinY();
        double z1 = box.getMinZ();
        double x2 = box.getMaxX();
        double z2 = box.getMaxZ();
        double yTop = box.getMaxY() - 1.0;

        drawLine(player, particle, x1, y1, z1, x2, y1, z1, step);
        drawLine(player, particle, x2, y1, z1, x2, y1, z2, step);
        drawLine(player, particle, x2, y1, z2, x1, y1, z2, step);
        drawLine(player, particle, x1, y1, z2, x1, y1, z1, step);

        drawLine(player, particle, x1, yTop, z1, x2, yTop, z1, step);
        drawLine(player, particle, x2, yTop, z1, x2, yTop, z2, step);
        drawLine(player, particle, x2, yTop, z2, x1, yTop, z2, step);
        drawLine(player, particle, x1, yTop, z2, x1, yTop, z1, step);

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
            return Particle.valueOf(name.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String color(String message) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }
}
