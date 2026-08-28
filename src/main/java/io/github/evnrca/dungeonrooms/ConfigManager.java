package io.github.evnrca.dungeonrooms;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * Config representation of one registered room.
     */
    public static final class StoredRoom {
        public final String world;
        public final String region;
        public final int requiredKills;

        public StoredRoom(String world, String region, int requiredKills) {
            this.world = world;
            this.region = region;
            this.requiredKills = requiredKills;
        }

        public String key() {
            return world + ":" + region;
        }
    }

    /**
     * Loads rooms from {@code rooms} while preserving config order.
     */
    public List<StoredRoom> loadRooms() {
        ConfigurationSection section = cfg().getConfigurationSection("rooms");
        if (section == null) {
            return List.of();
        }

        List<StoredRoom> loaded = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection roomSection = section.getConfigurationSection(key);
            if (roomSection == null) {
                continue;
            }
            String world = roomSection.getString("world", "");
            String region = roomSection.getString("region", "");
            int kills = roomSection.getInt("required-kills", 1);
            if (!world.isBlank() && !region.isBlank()) {
                loaded.add(new StoredRoom(world, region, kills));
            }
        }
        return loaded;
    }

    /**
     * Saves the full registered-room map to config.yml.
     */
    public void saveRooms(Map<String, RoomManager.RoomData> rooms) {
        cfg().set("rooms", null);
        for (Map.Entry<String, RoomManager.RoomData> entry : rooms.entrySet()) {
            RoomManager.RoomData room = entry.getValue();
            String path = "rooms." + entry.getKey();
            cfg().set(path + ".world", room.world);
            cfg().set(path + ".region", room.region);
            cfg().set(path + ".required-kills", room.requiredKills);
        }
        plugin.saveConfig();
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
                "&bPROGRESS: &3{current}/{required} &bMOBS KILLED");
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

    public String getNoPermission() {
        return cfg().getString("messages.command.no-permission", "&cNo permission.");
    }

    public String getUsageAdd() {
        return cfg().getString("messages.command.usage-add", "&cUsage: /dr add <world> <region> <kills>");
    }

    public String getUsageRemove() {
        return cfg().getString("messages.command.usage-remove", "&cUsage: /dr remove <world> <region>");
    }

    public String getUsageReset() {
        return cfg().getString("messages.command.usage-reset", "&cUsage: /dr reset <player> [region]");
    }

    public String getKillsMustBeNumber() {
        return cfg().getString("messages.command.kills-must-be-number", "&cKills must be a number.");
    }

    public String getPlayerNotFound() {
        return cfg().getString("messages.command.player-not-found", "&cPlayer not found.");
    }

    public String getConsoleSpecifyPlayer() {
        return cfg().getString("messages.command.console-specify-player", "&cConsole must specify a player.");
    }

    public String getOnlyPlayers() {
        return cfg().getString("messages.command.only-players", "&cOnly players can use this command.");
    }

    public String getListHeader() {
        return cfg().getString("messages.command.list-header", "&bRegistered dungeon rooms:");
    }

    public String getListEmpty() {
        return cfg().getString("messages.command.list-empty", "&7(none)");
    }

    public String getListEntry() {
        return cfg().getString("messages.command.list-entry",
                "&3{index}. &b{world}:{region} &7- &b{kills} &7kills");
    }

    public String getStatusHeader() {
        return cfg().getString("messages.command.status-header", "&bDungeon status for &3{player}&b:");
    }

    public String getStatusEntry() {
        return cfg().getString("messages.command.status-entry",
                "&b{region} &7- &3{current}/{required} &7({state}&7)");
    }

    public String getStatusUnlocked() {
        return cfg().getString("messages.command.status-unlocked", "&aUNLOCKED");
    }

    public String getStatusLocked() {
        return cfg().getString("messages.command.status-locked", "&cLOCKED");
    }

    public String getResetRegionFormatRequired() {
        return cfg().getString("messages.command.reset-region-format-required", "&cRegion must be in world:region format.");
    }

    public String getResetRegionDone() {
        return cfg().getString("messages.command.reset-region-done", "&bReset &3{player}&b's progress for &3{region}&b.");
    }

    public String getResetAllDone() {
        return cfg().getString("messages.command.reset-all-done", "&bReset &3{player}&b's all dungeon progress.");
    }

    public String getReloadDone() {
        return cfg().getString("messages.command.reload-done", "&bConfig reloaded and regions refreshed.");
    }

    public List<String> getVersionLines() {
        return cfg().getStringList("messages.command.version-lines").isEmpty()
                ? List.of(
                "&bDungeonRooms &7v&3{version}",
                "&bAuthor: &3evnrca",
                "&bGitHub: &3https://github.com/evnrca/DungeonRooms")
                : cfg().getStringList("messages.command.version-lines");
    }

    public String getHelpHeader() {
        return cfg().getString("messages.command.help-header", "&bDungeonRooms &7- &3Commands:");
    }

    public List<String> getHelpLines() {
        return cfg().getStringList("messages.command.help-lines").isEmpty()
                ? List.of(
                "&3/dr add <world> <region> <kills> &7- Register a dungeon room",
                "&3/dr remove <world> <region> &7- Unregister a room",
                "&3/dr list &7- List all registered rooms",
                "&3/dr status [player] &7- Check dungeon progress",
                "&3/dr reset <player> [region] &7- Reset player progress",
                "&3/dr reload &7- Reload config and refresh regions",
                "&3/dr showborder &7- Toggle border visualization",
                "&3/dr version &7- Show plugin version and links")
                : cfg().getStringList("messages.command.help-lines");
    }
}
