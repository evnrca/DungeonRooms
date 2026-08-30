package io.github.evnrca.dungeonrooms;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Handles room-entry denial for players who have not met the requirements.
 * <p>
 * Behavior is driven by {@code denial.action} which can be one of:
 * {@code CANCEL}, {@code VELOCITY}, {@code TELEPORT}, or {@code KNOCKBACK}.
 *
 * @author evnrca
 */
public final class DenialHandler {

    private final DungeonRooms plugin;
    private final ConfigManager config;
    private final ProgressManager progress;

    public DenialHandler(DungeonRooms plugin, ConfigManager config, ProgressManager progress) {
        this.plugin = plugin;
        this.config = config;
        this.progress = progress;
    }

    /**
     * Applies the configured denial behavior to the player.
     *
     * @param player    the player attempting to enter
     * @param to        where the player is trying to move
     * @param remaining remaining kills required
     * @param region    the target region name (for messages)
     */
    public void deny(Player player, Location to, int remaining, String region) {
        sendFeedback(player, remaining, region);

        String action = config.getDenialAction();
        switch (action) {
            case "VELOCITY":
                teleportBack(player);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        applyVelocity(player);
                    }
                });
                break;
            case "KNOCKBACK":
                teleportBack(player);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        applyKnockback(player, to);
                    }
                });
                break;
            case "TELEPORT":
            case "CANCEL":
            default:
                teleportBack(player);
                break;
        }
    }

    private void teleportBack(Player player) {
        Location last = progress.getLastLocation(player.getUniqueId());
        if (last != null) {
            player.teleport(last);
        }
    }

    private void applyVelocity(Player player) {
        Vector dir = player.getLocation().getDirection().multiply(-1);
        dir.setY(0);
        if (dir.lengthSquared() < 0.0001) {
            dir = new Vector(0, 0, 1);
        }
        dir.normalize().multiply(config.getDenialVelocityHorizontal());
        dir.setY(config.getDenialVelocityVertical());
        player.setVelocity(dir);
    }

    private void applyKnockback(Player player, Location attemptedTo) {
        Location safe = progress.getLastLocation(player.getUniqueId());
        if (safe == null) {
            safe = player.getLocation();
        }
        Vector knockback = safe.toVector().subtract(attemptedTo.toVector()).setY(0);
        if (knockback.lengthSquared() < 0.0001) {
            knockback = player.getLocation().getDirection().multiply(-1).setY(0);
        }
        knockback.normalize().multiply(config.getDenialVelocityHorizontal());
        knockback.setY(config.getDenialVelocityVertical());
        player.setVelocity(knockback);
    }

    private void sendFeedback(Player player, int remaining, String region) {
        player.sendTitle(
                color(config.getDenialTitle()),
                color(config.getDenialSubtitle().replace("{remaining}", String.valueOf(remaining))),
                10, 40, 10);

        Sound sound = config.getDenialSound();
        player.playSound(player.getLocation(), sound, config.getDenialSoundVolume(), config.getDenialSoundPitch());

        player.sendMessage(color(config.getPrefix() + config.getRequirementNotMet()
                .replace("{remaining}", String.valueOf(remaining))
                .replace("{region}", region)));
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
