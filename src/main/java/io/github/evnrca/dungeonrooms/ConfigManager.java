package io.github.evnrca.dungeonrooms;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Centralized access to DungeonRooms configuration and config migration.
 * <p>
 * This is the only class that calls {@code getConfig()}. Migration writes only
 * missing keys and never overwrites existing user values.
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
     * Reloads config.yml from disk and writes any missing v2 defaults.
     */
    public void reload() {
        plugin.reloadConfig();
        migrate();
    }

/**
     * Writes missing defaults without touching existing values.
     */
    public void migrate() {
        boolean changed = false;
        changed |= setDefault("denial.action", "KNOCKBACK");
        changed |= setDefault("denial.velocity.horizontal", 1.5);
        changed |= setDefault("denial.velocity.vertical", 0.4);
        changed |= setDefault("denial.title", "&b&lROOM LOCKED!");
        changed |= setDefault("denial.subtitle", "&bKill &3{remaining} &bmore mobs to proceed.");
        changed |= setDefault("denial.sound", "ENTITY_VILLAGER_NO");
        changed |= setDefault("denial.sound-volume", 1.0);
        changed |= setDefault("denial.sound-pitch", 1.0);

        changed |= setDefault("progress-reset.death", false);
        changed |= setDefault("progress-reset.dungeon-exit", true);
        changed |= setDefault("progress-reset.world-change", true);
        changed |= setDefault("progress-reset.teleport", true);

        changed |= setDefault("death-override.enabled", true);
        changed |= setDefault("death-override.blindness-seconds", 5);
        changed |= setDefault("death-override.title", "&c&lYOU DIED");
        changed |= setDefault("death-override.subtitle", "&7Returning to dungeon spawn...");
        changed |= setDefault("death-override.chat-message", "&cYou died in &4{dungeon}&c and were returned to the dungeon spawn.");
        changed |= setDefault("death-override.broadcast-message", "&7{player} died in dungeon &c{dungeon}&7.");
        changed |= setDefault("death-override.penalties.drop-items", false);
        changed |= setDefault("death-override.penalties.drop-exp", false);
        changed |= setDefault("death-override.commands", List.of());

        changed |= setDefault("progress-display.action-bar.enabled", true);
        changed |= setDefault("progress-display.action-bar.format", "&bProgress: &3{current}/{required} &bmobs killed");
        changed |= setDefault("progress-display.chat.enabled", true);
        changed |= setDefault("progress-display.chat.format", "&8[&bDungeons&8] &bProgress: &3{current}/{required} &bmobs killed");
        changed |= setDefault("progress-display.chat.cooldown", 5);

        changed |= setDefault("border-visualizer.enabled", true);
        changed |= setDefault("border-visualizer.room-particle-type", "FLAME");
        changed |= setDefault("border-visualizer.dungeon-particle-type", "END_ROD");
        changed |= setDefault("border-visualizer.spawn-particle-type", "END_ROD");
        changed |= setDefault("border-visualizer.particle-density", 0.5);
        changed |= setDefault("border-visualizer.interval-ticks", 20);
        changed |= setDefault("border-visualizer.messages.toggled-on", "&bBorder visualization &3enabled.");
        changed |= setDefault("border-visualizer.messages.toggled-off", "&bBorder visualization &3disabled.");
        changed |= setDefault("border-visualizer.messages.not-in-region", "&cYou are not inside any registered dungeon room.");
        changed |= setDefault("border-visualizer.messages.feature-disabled", "&cBorder visualization is disabled by the server.");
        changed |= setDefault("border-visualizer.messages.spawn-toggled-on", "&bSpawn border visualization &3enabled.");
        changed |= setDefault("border-visualizer.messages.spawn-toggled-off", "&bSpawn border visualization &3disabled.");

        changed |= setDefault("messages.prefix", "&8[&bDungeonRooms&8] ");
        changed |= setDefault("messages.requirement-not-met", "&cYou need &4{remaining} &cmore mob kills to enter &4{region}&c!");
        changed |= setDefault("messages.progress", "&bProgress: &3{current}/{required} &bmobs killed");
        changed |= setDefault("messages.completed", "&6Room &e{region} &6completed! &eYou may now proceed.");
        changed |= setDefault("messages.progress-reset-death", "&cYou died! &4Your dungeon progress has been reset.");
        changed |= setDefault("messages.progress-reset-logout", "&cYour dungeon progress has been reset &4(logout).");
        changed |= setDefault("messages.progress-reset-teleport", "&cYour dungeon progress has been reset &4(teleport).");
        changed |= setDefault("messages.progress-reset-world-exit", "&cYour dungeon progress has been reset &4(left world).");
        changed |= setDefault("messages.world-not-found", "&cWorld &4{world} &cdoes not exist.");
        changed |= setDefault("messages.region-not-found", "&cRegion &4{region} &cdoes not exist in world &4{world}&c.");
        changed |= setDefault("messages.dungeon-already-exists", "&cDungeon &4{dungeon} &cis already registered.");
        changed |= setDefault("messages.dungeon-not-found", "&cDungeon &4{dungeon} &cis not registered.");
        changed |= setDefault("messages.dungeon-created", "&bDungeon &3{dungeon} &bcreated.");
        changed |= setDefault("messages.dungeon-removed", "&bDungeon &3{dungeon} &bremoved.");
        changed |= setDefault("messages.room-added", "&bRoom &3{region} &badded to dungeon &3{dungeon}&b.");
        changed |= setDefault("messages.room-removed", "&bRoom &3{region} &bremoved from dungeon &3{dungeon}&b.");
        changed |= setDefault("messages.room-not-found", "&cRoom &4{region} &cnot found in dungeon &4{dungeon}&c.");
        changed |= setDefault("messages.room-no-spawn", "&cRegister a spawn region for dungeon &4{dungeon} &cbefore adding rooms.");
        changed |= setDefault("messages.spawn-set", "&bSpawn for dungeon &3{dungeon} &bset at your location.");
        changed |= setDefault("messages.spawn-not-in-region", "&cYou must be inside the registered spawn region to set spawn.");
        changed |= setDefault("messages.spawn-region-added", "&bSpawn region &3{region} &bregistered for dungeon &3{dungeon}&b.");
        changed |= setDefault("messages.kills-updated", "&bRequired kills for &3{region} &bupdated to &3{kills}&b.");

        changed |= setDefault("messages.command.no-permission", "&cNo permission.");
        changed |= setDefault("messages.command.usage-create", "&cUsage: /dr create <world> <region> <dungeonName>");
        changed |= setDefault("messages.command.usage-add-spawn", "&cUsage: /dr add spawn <world> <region> <dungeonName>");
        changed |= setDefault("messages.command.usage-add-room", "&cUsage: /dr add room <dungeonName> <region> <kills>");
        changed |= setDefault("messages.command.usage-setspawn", "&cUsage: /dr setspawn <dungeonName>");
        changed |= setDefault("messages.command.usage-remove", "&cUsage: /dr remove <dungeonName>");
        changed |= setDefault("messages.command.usage-remove-room", "&cUsage: /dr remove room <dungeonName> <region>");
        changed |= setDefault("messages.command.usage-edit-kills", "&cUsage: /dr edit kills <dungeonName> <region> <kills>");
        changed |= setDefault("messages.command.usage-reset", "&cUsage: /dr reset <player> [dungeon]");
        changed |= setDefault("messages.command.kills-must-be-number", "&cKills must be a number.");
        changed |= setDefault("messages.command.player-not-found", "&cPlayer not found.");
        changed |= setDefault("messages.command.console-specify-player", "&cConsole must specify a player.");
        changed |= setDefault("messages.command.only-players", "&cOnly players can use this command.");
        changed |= setDefault("messages.command.list-header", "&bRegistered dungeons:");
        changed |= setDefault("messages.command.list-empty", "&7(none)");
        changed |= setDefault("messages.command.list-dungeon", "&3{dungeon} &7- &b{world}:{region}");
        changed |= setDefault("messages.command.list-room", "  &7{sequence}. &b{region} &7- &3{kills} &7kills");
        changed |= setDefault("messages.command.status-header", "&bDungeon status for &3{player}&b:");
        changed |= setDefault("messages.command.status-dungeon", "&3{dungeon}");
        changed |= setDefault("messages.command.status-entry", "  &b{region} &7- &3{current}/{required} &7({state}&7)");
        changed |= setDefault("messages.command.status-unlocked", "&aUNLOCKED");
        changed |= setDefault("messages.command.status-locked", "&cLOCKED");
        changed |= setDefault("messages.command.reset-dungeon-done", "&bReset &3{player}&b's progress for dungeon &3{dungeon}&b.");
        changed |= setDefault("messages.command.reset-all-done", "&bReset &3{player}&b's all dungeon progress.");
        changed |= setDefault("messages.command.reload-done", "&bConfig reloaded and dungeons refreshed.");
        changed |= setDefault("messages.command.showborder-all-on", "&bAll dungeon borders &3enabled&b.");
        changed |= setDefault("messages.command.showborder-all-off", "&bAll dungeon borders &3disabled&b.");
        changed |= setDefault("messages.command.help-header", "&bDungeonRooms &7- &3Commands:");
        changed |= setDefault("messages.command.help-lines", List.of(
                "&3/dr create <world> <region> <dungeonName> &7- Create a dungeon",
                "&3/dr add spawn <world> <region> <dungeonName> &7- Register a spawn region",
                "&3/dr setspawn <dungeonName> &7- Set the dungeon spawn point",
                "&3/dr add room <dungeonName> <region> <kills> &7- Add a room",
                "&3/dr remove <dungeonName> &7- Remove a dungeon",
                "&3/dr remove room <dungeonName> <region> &7- Remove a room",
                "&3/dr edit kills <dungeonName> <region> <kills> &7- Change required kills",
                "&3/dr list &7- List all dungeons",
                "&3/dr status [player] &7- Check dungeon progress",
                "&3/dr reset <player> [dungeon] &7- Reset player progress",
                "&3/dr reload &7- Reload config and refresh dungeons",
                "&3/dr showborder [all|spawn] &7- Toggle border visualization",
                "&3/dr version &7- Show plugin version and links"));

        if (changed) {
            plugin.saveConfig();
        }
    }

    private boolean setDefault(String path, Object value) {
        if (!cfg().contains(path)) {
            cfg().set(path, value);
            return true;
        }
        return false;
    }

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    public String getDenialAction() {
        return cfg().getString("denial.action", "KNOCKBACK").toUpperCase();
    }

    public boolean isDeathOverrideEnabled() {
        return cfg().getBoolean("death-override.enabled", true);
    }

    public int getDeathOverrideBlindnessSeconds() {
        return Math.max(0, cfg().getInt("death-override.blindness-seconds", 5));
    }

    public String getDeathOverrideTitle() {
        return cfg().getString("death-override.title", "&c&lYOU DIED");
    }

    public String getDeathOverrideSubtitle() {
        return cfg().getString("death-override.subtitle", "&7Returning to dungeon spawn...");
    }

    public String getDeathOverrideChatMessage() {
        return cfg().getString("death-override.chat-message", "&cYou died in &4{dungeon}&c and were returned to the dungeon spawn.");
    }

    public String getDeathOverrideBroadcastMessage() {
        return cfg().getString("death-override.broadcast-message", "&7{player} died in dungeon &c{dungeon}&7.");
    }

    public boolean shouldDropItemsOnDungeonDeath() {
        return cfg().getBoolean("death-override.penalties.drop-items", false);
    }

    public boolean shouldDropExpOnDungeonDeath() {
        return cfg().getBoolean("death-override.penalties.drop-exp", false);
    }

    public List<String> getDungeonDeathCommands() {
        return cfg().getStringList("death-override.commands");
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
        return cfg().getString("denial.subtitle", "&bKill &3{remaining} &bmore mobs to proceed.");
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

    public boolean isResetOnDeath() {
        return cfg().getBoolean("progress-reset.death", false);
    }

    public boolean isResetOnDungeonExit() {
        return cfg().getBoolean("progress-reset.dungeon-exit", true);
    }

    public boolean isResetOnWorldChange() {
        return cfg().getBoolean("progress-reset.world-change", true);
    }

    public boolean isResetOnTeleport() {
        return cfg().getBoolean("progress-reset.teleport", true);
    }

    public boolean isActionBarEnabled() {
        return cfg().getBoolean("progress-display.action-bar.enabled", true);
    }

    public String getActionBarFormat() {
        return cfg().getString("progress-display.action-bar.format", "&bProgress: &3{current}/{required} &bmobs killed");
    }

    public boolean isChatEnabled() {
        return cfg().getBoolean("progress-display.chat.enabled", true);
    }

    public String getChatFormat() {
        return cfg().getString("progress-display.chat.format", "&8[&bDungeons&8] &bProgress: &3{current}/{required} &bmobs killed");
    }

    public int getChatCooldown() {
        return cfg().getInt("progress-display.chat.cooldown", 5);
    }

    public boolean isBorderVisualizerEnabled() {
        return cfg().getBoolean("border-visualizer.enabled", true);
    }

    public String getBorderParticleType() {
        return getRoomBorderParticleType();
    }

    public String getRoomBorderParticleType() {
        return cfg().getString("border-visualizer.room-particle-type",
                cfg().getString("border-visualizer.particle-type", "FLAME"));
    }

    public String getDungeonBorderParticleType() {
        return cfg().getString("border-visualizer.dungeon-particle-type", "END_ROD");
    }

    public String getSpawnBorderParticleType() {
        return cfg().getString("border-visualizer.spawn-particle-type", "END_ROD");
    }

    public double getBorderParticleDensity() {
        return cfg().getDouble("border-visualizer.particle-density", 0.5);
    }

    public int getBorderIntervalTicks() {
        return cfg().getInt("border-visualizer.interval-ticks", 20);
    }

    public String getBorderToggledOn() {
        return cfg().getString("border-visualizer.messages.toggled-on", "&bBorder visualization &3enabled.");
    }

    public String getBorderToggledOff() {
        return cfg().getString("border-visualizer.messages.toggled-off", "&bBorder visualization &3disabled.");
    }

    public String getBorderNotInRegion() {
        return cfg().getString("border-visualizer.messages.not-in-region", "&cYou are not inside any registered dungeon room.");
    }

    public String getBorderFeatureDisabled() {
        return cfg().getString("border-visualizer.messages.feature-disabled", "&cBorder visualization is disabled by the server.");
    }

    public String getSpawnBorderToggledOn() {
        return cfg().getString("border-visualizer.messages.spawn-toggled-on", "&bSpawn border visualization &3enabled.");
    }

    public String getSpawnBorderToggledOff() {
        return cfg().getString("border-visualizer.messages.spawn-toggled-off", "&bSpawn border visualization &3disabled.");
    }

    public String getPrefix() {
        return cfg().getString("messages.prefix", "&8[&bDungeonRooms&8] ");
    }

    public String getRequirementNotMet() {
        return cfg().getString("messages.requirement-not-met", "&cYou need &4{remaining} &cmore mob kills to enter &4{region}&c!");
    }

    public String getProgress() {
        return cfg().getString("messages.progress", "&bProgress: &3{current}/{required} &bmobs killed");
    }

    public String getCompleted() {
        return cfg().getString("messages.completed", "&6Room &e{region} &6completed! &eYou may now proceed.");
    }

    public String getProgressResetDeath() {
        return cfg().getString("messages.progress-reset-death", "&cYou died! &4Your dungeon progress has been reset.");
    }

    public String getProgressResetLogout() {
        return cfg().getString("messages.progress-reset-logout", "&cYour dungeon progress has been reset &4(logout).");
    }

    public String getProgressResetTeleport() {
        return cfg().getString("messages.progress-reset-teleport", "&cYour dungeon progress has been reset &4(teleport).");
    }

    public String getProgressResetWorldExit() {
        return cfg().getString("messages.progress-reset-world-exit", "&cYour dungeon progress has been reset &4(left world).");
    }

    public String getWorldNotFound() {
        return cfg().getString("messages.world-not-found", "&cWorld &4{world} &cdoes not exist.");
    }

    public String getRegionNotFound() {
        return cfg().getString("messages.region-not-found", "&cRegion &4{region} &cdoes not exist in world &4{world}&c.");
    }

    public String getDungeonAlreadyExists() {
        return cfg().getString("messages.dungeon-already-exists", "&cDungeon &4{dungeon} &cis already registered.");
    }

    public String getDungeonNotFound() {
        return cfg().getString("messages.dungeon-not-found", "&cDungeon &4{dungeon} &cis not registered.");
    }

    public String getDungeonCreated() {
        return cfg().getString("messages.dungeon-created", "&bDungeon &3{dungeon} &bcreated.");
    }

    public String getDungeonRemoved() {
        return cfg().getString("messages.dungeon-removed", "&bDungeon &3{dungeon} &bremoved.");
    }

    public String getRoomAlreadyExists() {
        return cfg().getString("messages.room-already-exists", "&cRoom &4{world}:{region} &cis already registered.");
    }

    public String getRoomAdded() {
        return cfg().getString("messages.room-added", "&bRoom &3{region} &badded to dungeon &3{dungeon}&b.");
    }

    public String getRoomRemoved() {
        return cfg().getString("messages.room-removed", "&bRoom &3{region} &bremoved from dungeon &3{dungeon}&b.");
    }

    public String getRoomNotFound() {
        return cfg().getString("messages.room-not-found", "&cRoom &4{region} &cnot found in dungeon &4{dungeon}&c.");
    }

    public String getRoomNoSpawn() {
        return cfg().getString("messages.room-no-spawn", "&cRegister a spawn region for dungeon &4{dungeon} &cbefore adding rooms.");
    }

    public String getSpawnSet() {
        return cfg().getString("messages.spawn-set", "&bSpawn for dungeon &3{dungeon} &bset at your location.");
    }

    public String getSpawnNotInRegion() {
        return cfg().getString("messages.spawn-not-in-region", "&cYou must be inside the registered spawn region to set spawn.");
    }

    public String getSpawnRegionAdded() {
        return cfg().getString("messages.spawn-region-added", "&bSpawn region &3{region} &bregistered for dungeon &3{dungeon}&b.");
    }

    public String getKillsUpdated() {
        return cfg().getString("messages.kills-updated", "&bRequired kills for &3{region} &bupdated to &3{kills}&b.");
    }

    public String getVersion() {
        return "&bDungeonRooms &3v{version} &b| &3by evnrca";
    }

    public String getNoPermission() {
        return cfg().getString("messages.command.no-permission", "&cNo permission.");
    }

    public String getUsageCreate() {
        return cfg().getString("messages.command.usage-create", "&cUsage: /dr create <world> <region> <dungeonName>");
    }

    public String getUsageAdd() {
        return cfg().getString("messages.command.usage-add", "&cUsage: /dr add <world> <region> <kills>");
    }

    public String getUsageAddSpawn() {
        return cfg().getString("messages.command.usage-add-spawn", "&cUsage: /dr add spawn <world> <region> <dungeonName>");
    }

    public String getUsageAddRoom() {
        return cfg().getString("messages.command.usage-add-room", "&cUsage: /dr add room <dungeonName> <region> <kills>");
    }

    public String getUsageSetSpawn() {
        return cfg().getString("messages.command.usage-setspawn", "&cUsage: /dr setspawn <dungeonName>");
    }

    public String getUsageRemove() {
        return cfg().getString("messages.command.usage-remove", "&cUsage: /dr remove <dungeonName>");
    }

    public String getUsageRemoveRoom() {
        return cfg().getString("messages.command.usage-remove-room", "&cUsage: /dr remove room <dungeonName> <region>");
    }

    public String getUsageEditKills() {
        return cfg().getString("messages.command.usage-edit-kills", "&cUsage: /dr edit kills <dungeonName> <region> <kills>");
    }

    public String getUsageReset() {
        return cfg().getString("messages.command.usage-reset", "&cUsage: /dr reset <player> [dungeon]");
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
        return cfg().getString("messages.command.list-header", "&bRegistered dungeons:");
    }

    public String getListEmpty() {
        return cfg().getString("messages.command.list-empty", "&7(none)");
    }

    public String getListEntry() {
        return cfg().getString("messages.command.list-entry", "&3{index}. &b{world}:{region} &7- &b{kills} &7kills");
    }

    public String getListDungeon() {
        return cfg().getString("messages.command.list-dungeon", "&3{dungeon} &7- &b{world}:{region}");
    }

    public String getListRoom() {
        return cfg().getString("messages.command.list-room", "  &7{sequence}. &b{region} &7- &3{kills} &7kills");
    }

    public String getStatusHeader() {
        return cfg().getString("messages.command.status-header", "&bDungeon status for &3{player}&b:");
    }

    public String getStatusDungeon() {
        return cfg().getString("messages.command.status-dungeon", "&3{dungeon}");
    }

    public String getStatusEntry() {
        return cfg().getString("messages.command.status-entry", "  &b{region} &7- &3{current}/{required} &7({state}&7)");
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

    public String getResetDungeonDone() {
        return cfg().getString("messages.command.reset-dungeon-done", "&bReset &3{player}&b's progress for dungeon &3{dungeon}&b.");
    }

    public String getResetAllDone() {
        return cfg().getString("messages.command.reset-all-done", "&bReset &3{player}&b's all dungeon progress.");
    }

    public String getReloadDone() {
        return cfg().getString("messages.command.reload-done", "&bConfig reloaded and dungeons refreshed.");
    }

    public String getShowBorderAllOn() {
        return cfg().getString("messages.command.showborder-all-on", "&bAll dungeon borders &3enabled&b.");
    }

    public String getShowBorderAllOff() {
        return cfg().getString("messages.command.showborder-all-off", "&bAll dungeon borders &3disabled&b.");
    }

    public String getHelpHeader() {
        return cfg().getString("messages.command.help-header", "&bDungeonRooms &7- &3Commands:");
    }

    public List<String> getHelpLines() {
        List<String> lines = cfg().getStringList("messages.command.help-lines");
        return lines.isEmpty() ? List.of(
                "&3/dr create <world> <region> <dungeonName> &7- Create a dungeon",
                "&3/dr add spawn <world> <region> <dungeonName> &7- Register a spawn region",
                "&3/dr setspawn <dungeonName> &7- Set the dungeon spawn point",
                "&3/dr add room <dungeonName> <region> <kills> &7- Add a room",
                "&3/dr remove <dungeonName> &7- Remove a dungeon",
                "&3/dr remove room <dungeonName> <region> &7- Remove a room",
                "&3/dr edit kills <dungeonName> <region> <kills> &7- Edit required kills",
                "&3/dr list &7- List all dungeons",
                "&3/dr status [player] &7- Check dungeon progress",
                "&3/dr reset <player> [dungeon] &7- Reset player progress",
                "&3/dr reload &7- Reload config and runtime cache",
                "&3/dr showborder [all|spawn] &7- Toggle border visualization",
                "&3/dr version &7- Show plugin version and author") : lines;
    }
}
