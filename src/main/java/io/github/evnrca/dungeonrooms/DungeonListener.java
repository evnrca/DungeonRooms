package io.github.evnrca.dungeonrooms;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles dungeon gameplay events for movement, progression, resets, and spawns.
 * <p>
 * High-frequency handlers return immediately when the relevant world is not in
 * the cached dungeon world set.
 *
 * @author evnrca
 */
public final class DungeonListener implements Listener {

    private final ConfigManager config;
    private final DungeonManager dungeonManager;
    private final ProgressManager progress;
    private final MythicMobsHook mythicMobsHook;
    private final DenialHandler denialHandler;
    private final BorderVisualizer borderVisualizer;
    private final Map<UUID, Long> lastChatProgress = new HashMap<>();

    public DungeonListener(ConfigManager config, DungeonManager dungeonManager,
                           ProgressManager progress, MythicMobsHook mythicMobsHook,
                           DenialHandler denialHandler, BorderVisualizer borderVisualizer) {
        this.config = config;
        this.dungeonManager = dungeonManager;
        this.progress = progress;
        this.mythicMobsHook = mythicMobsHook;
        this.denialHandler = denialHandler;
        this.borderVisualizer = borderVisualizer;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        progress.loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!dungeonManager.isDungeonWorld(event.getPlayer().getWorld().getName())) {
            return;
        }

        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null || to.getWorld() == null) {
            return;
        }

        DungeonManager.DungeonData fromDungeon = dungeonManager.getDungeonByLocation(event.getFrom());
        DungeonManager.DungeonData toDungeon = dungeonManager.getDungeonByLocation(to);
        DungeonManager.RoomData fromRoom = dungeonManager.getRoomByLocation(event.getFrom());
        DungeonManager.RoomData toRoom = dungeonManager.getRoomByLocation(to);

        if (fromDungeon != null && toDungeon == null) {
            progress.setLastLocation(player.getUniqueId(), event.getFrom());
            if (config.isResetOnDungeonExit()) {
                resetPlayer(player, config.getProgressResetWorldExit());
            }
            borderVisualizer.refreshRegion(player);
            return;
        }

        if (toRoom != null && !sameRoom(fromRoom, toRoom)) {
            if (fromRoom != null) {
                progress.setLastLocation(player.getUniqueId(), event.getFrom());
            }
            handleEntry(player, to, toRoom);
            borderVisualizer.refreshRegion(player);
            return;
        }

        if (toDungeon != null && fromDungeon == null) {
            progress.setLastLocation(player.getUniqueId(), to);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        if (!dungeonManager.isDungeonWorld(event.getFrom().getName())) {
            return;
        }

        Player player = event.getPlayer();
        if (config.isResetOnWorldChange()) {
            resetPlayer(player, config.getProgressResetWorldExit());
        }
        borderVisualizer.disable(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || !dungeonManager.isDungeonWorld(killer.getWorld().getName())) {
            return;
        }
        if (!mythicMobsHook.isMythicMob(event.getEntity())) {
            return;
        }

        DungeonManager.RoomData room = dungeonManager.getRoomByLocation(killer.getLocation());
        if (room == null) {
            return;
        }

        int current = progress.addKill(killer.getUniqueId(), room.dungeonName, room.region);
        showProgress(killer, current, room.requiredKills);
        if (current >= room.requiredKills && !progress.isUnlocked(killer.getUniqueId(), room.dungeonName, room.region)) {
            progress.unlock(killer.getUniqueId(), room.dungeonName, room.region);
            killer.sendMessage(color(config.getPrefix() + config.getCompleted()
                    .replace("{region}", room.region)));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!dungeonManager.isDungeonWorld(event.getPlayer().getWorld().getName())) {
            return;
        }

        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null || to.getWorld() == null) {
            return;
        }

        DungeonManager.RoomData fromRoom = dungeonManager.getRoomByLocation(event.getFrom());
        DungeonManager.RoomData toRoom = dungeonManager.getRoomByLocation(to);
        if (toRoom != null && !sameRoom(fromRoom, toRoom) && !handleEntry(player, to, toRoom)) {
            event.setCancelled(true);
            return;
        }

        if (config.isResetOnTeleport() && dungeonManager.getDungeonByLocation(event.getFrom()) != null
                && dungeonManager.getDungeonByLocation(to) == null) {
            resetPlayer(player, config.getProgressResetTeleport());
            borderVisualizer.disable(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !config.isDeathOverrideEnabled()) {
            return;
        }
        if (!dungeonManager.isDungeonWorld(player.getWorld().getName())) {
            return;
        }
        if (player.getHealth() - event.getFinalDamage() > 0.0) {
            return;
        }

        DungeonManager.DungeonData dungeon = dungeonAt(player.getLocation());
        if (dungeon == null || dungeon.spawnLocation == null || dungeon.spawnLocation.getWorld() == null) {
            return;
        }
        if (dungeon.spawnRegion == null || !dungeonManager.isSpawnRegion(dungeon.spawnLocation, dungeon.dungeonName)) {
            player.getServer().getLogger().warning("[DungeonRooms] Dungeon " + dungeon.dungeonName
                    + " has invalid spawn location; using vanilla death behavior.");
            return;
        }

        event.setCancelled(true);
        handleDungeonDeath(player, dungeon, player.getLocation().clone());
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        progress.flushPlayer(player.getUniqueId());
        borderVisualizer.disable(player);
    }

    private DungeonManager.DungeonData dungeonAt(Location location) {
        DungeonManager.DungeonData dungeon = dungeonManager.getDungeonByLocation(location);
        if (dungeon != null) {
            return dungeon;
        }

        DungeonManager.RoomData room = dungeonManager.getRoomByLocation(location);
        return room == null ? null : dungeonManager.getDungeon(room.dungeonName);
    }

    private void handleDungeonDeath(Player player, DungeonManager.DungeonData dungeon, Location deathLocation) {
        applyDeathPenalties(player, deathLocation);
        resetPlayerState(player);
        player.teleport(dungeon.spawnLocation.clone());

        int blindnessTicks = config.getDeathOverrideBlindnessSeconds() * 20;
        if (blindnessTicks > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessTicks, 0, false, false));
        }

        player.sendTitle(
                color(placeholders(config.getDeathOverrideTitle(), player, dungeon, deathLocation)),
                color(placeholders(config.getDeathOverrideSubtitle(), player, dungeon, deathLocation)),
                10, Math.max(40, blindnessTicks), 20);
        player.sendMessage(color(config.getPrefix()
                + placeholders(config.getDeathOverrideChatMessage(), player, dungeon, deathLocation)));

        String broadcast = config.getDeathOverrideBroadcastMessage();
        if (broadcast != null && !broadcast.isBlank()) {
            Bukkit.broadcastMessage(color(placeholders(broadcast, player, dungeon, deathLocation)));
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
        runDeathCommands(player, dungeon, deathLocation);

        if (config.isResetOnDeath()) {
            resetPlayer(player, config.getProgressResetDeath());
        }
        borderVisualizer.disable(player);
    }

    private void applyDeathPenalties(Player player, Location deathLocation) {
        if (config.shouldDropItemsOnDungeonDeath()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && !item.getType().isAir()) {
                    deathLocation.getWorld().dropItemNaturally(deathLocation, item.clone());
                }
            }
            player.getInventory().clear();
        }

        if (config.shouldDropExpOnDungeonDeath() && player.getTotalExperience() > 0) {
            deathLocation.getWorld().spawn(deathLocation, org.bukkit.entity.ExperienceOrb.class)
                    .setExperience(player.getTotalExperience());
            player.setTotalExperience(0);
            player.setLevel(0);
            player.setExp(0.0f);
        }
    }

    private void resetPlayerState(Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setRemainingAir(player.getMaximumAir());
    }

    private void runDeathCommands(Player player, DungeonManager.DungeonData dungeon, Location deathLocation) {
        for (String command : config.getDungeonDeathCommands()) {
            if (command == null || command.isBlank()) {
                continue;
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    placeholders(command, player, dungeon, deathLocation).replaceFirst("^/", ""));
        }
    }

    private String placeholders(String message, Player player, DungeonManager.DungeonData dungeon, Location location) {
        return message
                .replace("{player}", player.getName())
                .replace("{uuid}", player.getUniqueId().toString())
                .replace("{dungeon}", dungeon.dungeonName)
                .replace("{world}", location.getWorld().getName())
                .replace("{x}", String.valueOf(location.getBlockX()))
                .replace("{y}", String.valueOf(location.getBlockY()))
                .replace("{z}", String.valueOf(location.getBlockZ()));
    }

    private boolean handleEntry(Player player, Location to, DungeonManager.RoomData room) {
        if (room.sequence == 0 || progress.isUnlocked(player.getUniqueId(), room.dungeonName, room.region)
                || canBypass(player, room.dungeonName, room.region)) {
            progress.setLastLocation(player.getUniqueId(), to);
            return true;
        }

        DungeonManager.RoomData previous = dungeonManager.getPreviousRoom(room);
        if (previous == null) {
            return true;
        }

        int previousKills = progress.getKills(player.getUniqueId(), previous.dungeonName, previous.region);
        if (progress.isUnlocked(player.getUniqueId(), previous.dungeonName, previous.region)
                || previousKills >= previous.requiredKills) {
            progress.setLastLocation(player.getUniqueId(), to);
            return true;
        }

        int remaining = previous.requiredKills - previousKills;
        denialHandler.deny(player, to, remaining, room.region);
        return false;
    }

    private boolean sameRoom(DungeonManager.RoomData first, DungeonManager.RoomData second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.dungeonName.equals(second.dungeonName) && first.region.equals(second.region);
    }

    private boolean canBypass(Player player, String dungeonName, String region) {
        String scoped = "dungeonrooms.bypass."
                + normalizePermissionPart(dungeonName) + "."
                + normalizePermissionPart(region);
        return player.hasPermission("dungeonrooms.bypass") || player.hasPermission(scoped);
    }

    private String normalizePermissionPart(String value) {
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
    }

    private void showProgress(Player player, int current, int required) {
        if (config.isActionBarEnabled()) {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent(color(config.getActionBarFormat()
                            .replace("{current}", String.valueOf(current))
                            .replace("{required}", String.valueOf(required)))));
        }
        if (!config.isChatEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        Long last = lastChatProgress.get(player.getUniqueId());
        int cooldownMs = config.getChatCooldown() * 1000;
        if (last == null || now - last >= cooldownMs) {
            lastChatProgress.put(player.getUniqueId(), now);
            player.sendMessage(color(config.getChatFormat()
                    .replace("{current}", String.valueOf(current))
                    .replace("{required}", String.valueOf(required))));
        }
    }

    private void resetPlayer(Player player, String message) {
        progress.resetPlayer(player.getUniqueId());
        player.sendMessage(color(config.getPrefix() + message));
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
