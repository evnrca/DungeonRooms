package io.github.evnrca.dungeonrooms;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Centralized access to the plugin configuration.
 * <p>
 * This is the ONLY class that calls {@code getConfig()}. All other classes
 * receive the values they need through this wrapper. Every getter provides a
 * hardcoded default so the plugin never crashes if a config entry is missing.
 *
 * @author evnrca
 */
public final class ConfigManager {

    private final DungeonRooms plugin;

    public ConfigManager(DungeonRooms plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        reload();
    }

    /**
     * Reloads the config from disk and refreshes the default configuration
     * values in case new options were added between versions.
     */
    public void reload() {
        plugin.reloadConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    // ------------------------------------------------------------------
    // denial
    // ------------------------------------------------------------------

    public String getDenialAction() {
        return cfg().getString("denial.action", "CANCEL").toUpperCase();
    }

    public double getDenialVelocityHorizontal() {
        return cfg().getDouble("denial.velocity.horizontal", 1.5);
    }

    public double getDenialVelocityVertical() {
        return cfg().getDouble("denial.velocity.vertical", 0.4);
    }

    public String getDenialTitle() {
        return cfg().getString("denial.title", "&b&lROOM LOCKED!");
    }

    public String getDenialSubtitle() {
        return cfg().getString("denial.subtitle", "&bKILL &3{remaining} &bMORE MOBS TO PROCEED.");
    }

    public Sound getDenialSound() {
        String name = cfg().getString("denial.sound", "ENTITY_VILLAGER_NO");
        try {
            return Sound.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return Sound.ENTITY_VILLAGER_NO;
        }
    }

    public float getDenialSoundVolume() {
        return (float) cfg().getDouble("denial.sound-volume", 1.0);
    }

    public float getDenialSoundPitch() {
        return (float) cfg().getDouble("denial.sound-pitch", 1.0);
    }

    // ------------------------------------------------------------------
    // progress-display
    // ------------------------------------------------------------------

    public boolean isActionBarEnabled() {
        return cfg().getBoolean("progress-display.action-bar.enabled", true);
    }

    public String getActionBarFormat() {
        return cfg().getString("progress-display.action-bar.format",
                "&bPROGRESS: &3{current}/{required} &bMOBS KILLED");
    }

    public boolean isChatEnabled() {
        return cfg().getBoolean("progress-display.chat.enabled", true);
    }

    public String getChatFormat() {
        return cfg().getString("progress-display.chat.format",
                "&8[&bDUNGEONS&8] &bPROGRESS: &3{current}/{required} &bMOBS KILLED");
    }

    public int getChatCooldown() {
        return cfg().getInt("progress-display.chat.cooldown", 5);
    }

    // ------------------------------------------------------------------
    // border-visualizer
    // ------------------------------------------------------------------

    public boolean isBorderVisualizerEnabled() {
        return cfg().getBoolean("border-visualizer.enabled", true);
    }

    public String getBorderParticleType() {
        return cfg().getString("border-visualizer.particle-type", "FLAME");
    }

    public double getBorderParticleDensity() {
        return cfg().getDouble("border-visualizer.particle-density", 0.5);
    }

    public int getBorderIntervalTicks() {
        return cfg().getInt("border-visualizer.interval-ticks", 20);
    }

    public String getBorderToggledOn() {
        return cfg().getString("border-visualizer.messages.toggled-on",
                "&bBORDER VISUALIZATION &3ENABLED.");
    }

    public String getBorderToggledOff() {
        return cfg().getString("border-visualizer.messages.toggled-off",
                "&bBORDER VISUALIZATION &3DISABLED.");
    }

    public String getBorderNotInRegion() {
        return cfg().getString("border-visualizer.messages.not-in-region",
                "&cYOU ARE NOT INSIDE ANY REGISTERED DUNGEON ROOM.");
    }

    public String getBorderFeatureDisabled() {
        return cfg().getString("border-visualizer.messages.feature-disabled",
                "&cBORDER VISUALIZATION IS DISABLED BY THE SERVER.");
    }

    // ------------------------------------------------------------------
    // messages
    // ------------------------------------------------------------------

    public String getPrefix() {
        return cfg().getString("messages.prefix", "&8[&bDUNGEONROOMS&8] ");
    }

    public String getRequirementNotMet() {
        return cfg().getString("messages.requirement-not-met",
                "&cYOU NEED &4{remaining} &cMORE MOB KILLS TO ENTER &4{region}&c!");
    }

    public String getProgress() {
        return cfg().getString("messages.progress",
                "&bPROGRESS: &3{current}/{required} &bMOBS KILLED");
    }

    public String getCompleted() {
        return cfg().getString("messages.completed",
                "&6ROOM &e{region} &6COMPLETED! &eYOU MAY NOW PROCEED.");
    }

    public String getProgressResetDeath() {
        return cfg().getString("messages.progress-reset-death",
                "&cYOU DIED! &4YOUR DUNGEON PROGRESS HAS BEEN RESET.");
    }

    public String getProgressResetLogout() {
        return cfg().getString("messages.progress-reset-logout",
                "&cYOUR DUNGEON PROGRESS HAS BEEN RESET &4(LOGOUT).");
    }

    public String getProgressResetTeleport() {
        return cfg().getString("messages.progress-reset-teleport",
                "&cYOUR DUNGEON PROGRESS HAS BEEN RESET &4(TELEPORT).");
    }

    public String getProgressResetWorldExit() {
        return cfg().getString("messages.progress-reset-world-exit",
                "&cYOUR DUNGEON PROGRESS HAS BEEN RESET &4(LEFT WORLD).");
    }

    public String getWorldNotFound() {
        return cfg().getString("messages.world-not-found",
                "&cWORLD &4{world} &cDOES NOT EXIST.");
    }

    public String getRegionNotFound() {
        return cfg().getString("messages.region-not-found",
                "&cREGION &4{region} &cDOES NOT EXIST IN WORLD &4{world}&c.");
    }

    public String getRoomAlreadyExists() {
        return cfg().getString("messages.room-already-exists",
                "&cROOM &4{world}:{region} &cIS ALREADY REGISTERED.");
    }

    public String getRoomAdded() {
        return cfg().getString("messages.room-added",
                "&bROOM &3{world}:{region} &bREGISTERED WITH &3{kills} &bREQUIRED KILLS.");
    }

    public String getRoomRemoved() {
        return cfg().getString("messages.room-removed",
                "&bROOM &3{world}:{region} &bREMOVED.");
    }

    public String getRoomNotFound() {
        return cfg().getString("messages.room-not-found",
                "&cROOM &4{world}:{region} &cIS NOT REGISTERED.");
    }
}