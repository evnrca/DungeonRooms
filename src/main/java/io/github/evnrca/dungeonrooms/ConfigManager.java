package io.github.evnrca.dungeonrooms;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        setDefault("denial.action", "CANCEL");
        setDefault("denial.velocity.horizontal", 1.5);
        setDefault("denial.velocity.vertical", 0.4);
        setDefault("denial.title", "&b&lʀᴏᴏᴍ ʟᴏᴄᴋᴇᴅ!");
        setDefault("denial.subtitle", "&bᴋɪʟʟ &3{remaining} &bᴍᴏʀᴇ ᴍᴏʙs ᴛᴏ ᴘʀᴏᴄᴇᴇᴅ.");
        setDefault("denial.sound", "ENTITY_VILLAGER_NO");
        setDefault("denial.sound-volume", 1.0);
        setDefault("denial.sound-pitch", 1.0);

        setDefault("progress-reset.death", false);
        setDefault("progress-reset.dungeon-exit", true);
        setDefault("progress-reset.world-change", true);
        setDefault("progress-reset.teleport", true);

        setDefault("progress-display.action-bar.enabled", true);
        setDefault("progress-display.action-bar.format", "&bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ");
        setDefault("progress-display.chat.enabled", true);
        setDefault("progress-display.chat.format", "&8[&bᴅᴜɴɢᴇᴏɴs&8] &bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ");
        setDefault("progress-display.chat.cooldown", 5);

        setDefault("border-visualizer.enabled", true);
        setDefault("border-visualizer.room-particle-type", "FLAME");
        setDefault("border-visualizer.dungeon-particle-type", "END_ROD");
        setDefault("border-visualizer.particle-density", 0.5);
        setDefault("border-visualizer.interval-ticks", 20);
        setDefault("border-visualizer.messages.toggled-on", "&bʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ &3ᴇɴᴀʙʟᴇᴅ.");
        setDefault("border-visualizer.messages.toggled-off", "&bʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ &3ᴅɪsᴀʙʟᴇᴅ.");
        setDefault("border-visualizer.messages.not-in-region", "&cʏᴏᴜ ᴀʀᴇ ɴᴏᴛ ɪɴsɪᴅᴇ ᴀɴʏ ʀᴇɢɪsᴛᴇʀᴇᴅ ᴅᴜɴɢᴇᴏɴ ʀᴏᴏᴍ.");
        setDefault("border-visualizer.messages.feature-disabled", "&cʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ ɪs ᴅɪsᴀʙʟᴇᴅ ʙʏ ᴛʜᴇ sᴇʀᴠᴇʀ.");

        setDefault("messages.prefix", "&8[&bᴅᴜɴɢᴇᴏɴʀᴏᴏᴍs&8] ");
        setDefault("messages.requirement-not-met", "&cʏᴏᴜ ɴᴇᴇᴅ &4{remaining} &cᴍᴏʀᴇ ᴍᴏʙ ᴋɪʟʟs ᴛᴏ ᴇɴᴛᴇʀ &4{region}&c!");
        setDefault("messages.progress", "&bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ");
        setDefault("messages.completed", "&6ʀᴏᴏᴍ &e{region} &6ᴄᴏᴍᴘʟᴇᴛᴇᴅ! &eʏᴏᴜ ᴍᴀʏ ɴᴏᴡ ᴘʀᴏᴄᴇᴇᴅ.");
        setDefault("messages.progress-reset-death", "&cʏᴏᴜ ᴅɪᴇᴅ! &4ʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ.");
        setDefault("messages.progress-reset-logout", "&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ʟᴏɢᴏᴜᴛ).");
        setDefault("messages.progress-reset-teleport", "&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ᴛᴇʟᴇᴘᴏʀᴛ).");
        setDefault("messages.progress-reset-world-exit", "&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ʟᴇꜰᴛ ᴡᴏʀʟᴅ).");
        setDefault("messages.world-not-found", "&cᴡᴏʀʟᴅ &4{world} &cᴅᴏᴇs ɴᴏᴛ ᴇxɪsᴛ.");
        setDefault("messages.region-not-found", "&cʀᴇɢɪᴏɴ &4{region} &cᴅᴏᴇs ɴᴏᴛ ᴇxɪsᴛ ɪɴ ᴡᴏʀʟᴅ &4{world}&c.");
        setDefault("messages.dungeon-already-exists", "&cᴅᴜɴɢᴇᴏɴ &4{dungeon} &cɪs ᴀʟʀᴇᴀᴅʏ ʀᴇɢɪsᴛᴇʀᴇᴅ.");
        setDefault("messages.dungeon-not-found", "&cᴅᴜɴɢᴇᴏɴ &4{dungeon} &cɪs ɴᴏᴛ ʀᴇɢɪsᴛᴇʀᴇᴅ.");
        setDefault("messages.dungeon-created", "&bᴅᴜɴɢᴇᴏɴ &3{dungeon} &bᴄʀᴇᴀᴛᴇᴅ.");
        setDefault("messages.room-added", "&bʀᴏᴏᴍ &3{region} &bᴀᴅᴅᴇᴅ ᴛᴏ ᴅᴜɴɢᴇᴏɴ &3{dungeon}&b.");
        setDefault("messages.room-removed", "&bʀᴏᴏᴍ &3{region} &bʀᴇᴍᴏᴠᴇᴅ ꜰʀᴏᴍ ᴅᴜɴɢᴇᴏɴ &3{dungeon}&b.");
        setDefault("messages.room-not-found", "&cʀᴏᴏᴍ &4{region} &cɴᴏᴛ ꜰᴏᴜɴᴅ ɪɴ ᴅᴜɴɢᴇᴏɴ &4{dungeon}&c.");
        setDefault("messages.room-no-spawn", "&cʀᴇɢɪsᴛᴇʀ ᴀ sᴘᴀᴡɴ ʀᴇɢɪᴏɴ ꜰᴏʀ ᴅᴜɴɢᴇᴏɴ &4{dungeon} &cʙᴇꜰᴏʀᴇ ᴀᴅᴅɪɴɢ ʀᴏᴏᴍs.");
        setDefault("messages.spawn-set", "&bsᴘᴀᴡɴ ꜰᴏʀ ᴅᴜɴɢᴇᴏɴ &3{dungeon} &bsᴇᴛ ᴀᴛ ʏᴏᴜʀ ʟᴏᴄᴀᴛɪᴏɴ.");
        setDefault("messages.spawn-not-in-region", "&cʏᴏᴜ ᴍᴜsᴛ ʙᴇ ɪɴsɪᴅᴇ ᴛʜᴇ ʀᴇɢɪsᴛᴇʀᴇᴅ sᴘᴀᴡɴ ʀᴇɢɪᴏɴ ᴛᴏ sᴇᴛ sᴘᴀᴡɴ.");
        setDefault("messages.spawn-region-added", "&bsᴘᴀᴡɴ ʀᴇɢɪᴏɴ &3{region} &bʀᴇɢɪsᴛᴇʀᴇᴅ ꜰᴏʀ ᴅᴜɴɢᴇᴏɴ &3{dungeon}&b.");
        setDefault("messages.kills-updated", "&bʀᴇQᴜɪʀᴇᴅ ᴋɪʟʟs ꜰᴏʀ &3{region} &bᴜᴘᴅᴀᴛᴇᴅ ᴛᴏ &3{kills}&b.");
        setDefault("messages.version", "&bᴅᴜɴɢᴇᴏɴʀᴏᴏᴍs &3v{version} &b| &3ʙʏ evnrca");

        setDefault("messages.command.no-permission", "&cNo permission.");
        setDefault("messages.command.usage-create", "&cUsage: /dr create <world> <region> <dungeonName>");
        setDefault("messages.command.usage-add-spawn", "&cUsage: /dr add spawn <world> <region> <dungeonName>");
        setDefault("messages.command.usage-add-room", "&cUsage: /dr add room <dungeonName> <region> <kills>");
        setDefault("messages.command.usage-setspawn", "&cUsage: /dr setspawn <dungeonName>");
        setDefault("messages.command.usage-remove", "&cUsage: /dr remove <dungeonName>");
        setDefault("messages.command.usage-remove-room", "&cUsage: /dr remove room <dungeonName> <region>");
        setDefault("messages.command.usage-edit-kills", "&cUsage: /dr edit kills <dungeonName> <region> <kills>");
        setDefault("messages.command.usage-reset", "&cUsage: /dr reset <player> [dungeon]");
        setDefault("messages.command.kills-must-be-number", "&cKills must be a number.");
        setDefault("messages.command.player-not-found", "&cPlayer not found.");
        setDefault("messages.command.console-specify-player", "&cConsole must specify a player.");
        setDefault("messages.command.only-players", "&cOnly players can use this command.");
        setDefault("messages.command.list-header", "&bRegistered dungeons:");
        setDefault("messages.command.list-empty", "&7(none)");
        setDefault("messages.command.list-dungeon", "&3{dungeon} &7- &b{world}:{region}");
        setDefault("messages.command.list-room", "  &7{sequence}. &b{region} &7- &3{kills} &7kills");
        setDefault("messages.command.status-header", "&bDungeon status for &3{player}&b:");
        setDefault("messages.command.status-dungeon", "&3{dungeon}");
        setDefault("messages.command.status-entry", "  &b{region} &7- &3{current}/{required} &7({state}&7)");
        setDefault("messages.command.status-unlocked", "&aUNLOCKED");
        setDefault("messages.command.status-locked", "&cLOCKED");
        setDefault("messages.command.reset-dungeon-done", "&bReset &3{player}&b's progress for dungeon &3{dungeon}&b.");
        setDefault("messages.command.reset-all-done", "&bReset &3{player}&b's all dungeon progress.");
        setDefault("messages.command.reload-done", "&bConfig reloaded and dungeons refreshed.");
        setDefault("messages.command.showborder-all-on", "&bAll dungeon borders &3enabled&b.");
        setDefault("messages.command.showborder-all-off", "&bAll dungeon borders &3disabled&b.");
        setDefault("messages.command.help-header", "&bDungeonRooms &7- &3Commands:");
        setDefault("messages.command.help-lines", List.of(
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
                "&3/dr showborder [all] &7- Toggle border visualization",
                "&3/dr version &7- Show plugin version and author"));

        plugin.saveConfig();
    }

    private void setDefault(String path, Object value) {
        if (!cfg().contains(path)) {
            cfg().set(path, value);
        }
    }

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    /**
     * Legacy config representation kept for migration compatibility while v2
     * replaces room config storage with runtime-managed dungeon data.
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
        return cfg().getString("denial.title", "&b&lʀᴏᴏᴍ ʟᴏᴄᴋᴇᴅ!");
    }

    public String getDenialSubtitle() {
        return cfg().getString("denial.subtitle", "&bᴋɪʟʟ &3{remaining} &bᴍᴏʀᴇ ᴍᴏʙs ᴛᴏ ᴘʀᴏᴄᴇᴇᴅ.");
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
        return cfg().getString("progress-display.action-bar.format", "&bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ");
    }

    public boolean isChatEnabled() {
        return cfg().getBoolean("progress-display.chat.enabled", true);
    }

    public String getChatFormat() {
        return cfg().getString("progress-display.chat.format", "&8[&bᴅᴜɴɢᴇᴏɴs&8] &bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ");
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

    public double getBorderParticleDensity() {
        return cfg().getDouble("border-visualizer.particle-density", 0.5);
    }

    public int getBorderIntervalTicks() {
        return cfg().getInt("border-visualizer.interval-ticks", 20);
    }

    public String getBorderToggledOn() {
        return cfg().getString("border-visualizer.messages.toggled-on", "&bʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ &3ᴇɴᴀʙʟᴇᴅ.");
    }

    public String getBorderToggledOff() {
        return cfg().getString("border-visualizer.messages.toggled-off", "&bʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ &3ᴅɪsᴀʙʟᴇᴅ.");
    }

    public String getBorderNotInRegion() {
        return cfg().getString("border-visualizer.messages.not-in-region", "&cʏᴏᴜ ᴀʀᴇ ɴᴏᴛ ɪɴsɪᴅᴇ ᴀɴʏ ʀᴇɢɪsᴛᴇʀᴇᴅ ᴅᴜɴɢᴇᴏɴ ʀᴏᴏᴍ.");
    }

    public String getBorderFeatureDisabled() {
        return cfg().getString("border-visualizer.messages.feature-disabled", "&cʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ ɪs ᴅɪsᴀʙʟᴇᴅ ʙʏ ᴛʜᴇ sᴇʀᴠᴇʀ.");
    }

    public String getPrefix() {
        return cfg().getString("messages.prefix", "&8[&bᴅᴜɴɢᴇᴏɴʀᴏᴏᴍs&8] ");
    }

    public String getRequirementNotMet() {
        return cfg().getString("messages.requirement-not-met", "&cʏᴏᴜ ɴᴇᴇᴅ &4{remaining} &cᴍᴏʀᴇ ᴍᴏʙ ᴋɪʟʟs ᴛᴏ ᴇɴᴛᴇʀ &4{region}&c!");
    }

    public String getProgress() {
        return cfg().getString("messages.progress", "&bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ");
    }

    public String getCompleted() {
        return cfg().getString("messages.completed", "&6ʀᴏᴏᴍ &e{region} &6ᴄᴏᴍᴘʟᴇᴛᴇᴅ! &eʏᴏᴜ ᴍᴀʏ ɴᴏᴡ ᴘʀᴏᴄᴇᴇᴅ.");
    }

    public String getProgressResetDeath() {
        return cfg().getString("messages.progress-reset-death", "&cʏᴏᴜ ᴅɪᴇᴅ! &4ʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ.");
    }

    public String getProgressResetLogout() {
        return cfg().getString("messages.progress-reset-logout", "&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ʟᴏɢᴏᴜᴛ).");
    }

    public String getProgressResetTeleport() {
        return cfg().getString("messages.progress-reset-teleport", "&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ᴛᴇʟᴇᴘᴏʀᴛ).");
    }

    public String getProgressResetWorldExit() {
        return cfg().getString("messages.progress-reset-world-exit", "&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ʟᴇꜰᴛ ᴡᴏʀʟᴅ).");
    }

    public String getWorldNotFound() {
        return cfg().getString("messages.world-not-found", "&cᴡᴏʀʟᴅ &4{world} &cᴅᴏᴇs ɴᴏᴛ ᴇxɪsᴛ.");
    }

    public String getRegionNotFound() {
        return cfg().getString("messages.region-not-found", "&cʀᴇɢɪᴏɴ &4{region} &cᴅᴏᴇs ɴᴏᴛ ᴇxɪsᴛ ɪɴ ᴡᴏʀʟᴅ &4{world}&c.");
    }

    public String getDungeonAlreadyExists() {
        return cfg().getString("messages.dungeon-already-exists", "&cᴅᴜɴɢᴇᴏɴ &4{dungeon} &cɪs ᴀʟʀᴇᴀᴅʏ ʀᴇɢɪsᴛᴇʀᴇᴅ.");
    }

    public String getDungeonNotFound() {
        return cfg().getString("messages.dungeon-not-found", "&cᴅᴜɴɢᴇᴏɴ &4{dungeon} &cɪs ɴᴏᴛ ʀᴇɢɪsᴛᴇʀᴇᴅ.");
    }

    public String getDungeonCreated() {
        return cfg().getString("messages.dungeon-created", "&bᴅᴜɴɢᴇᴏɴ &3{dungeon} &bᴄʀᴇᴀᴛᴇᴅ.");
    }

    public String getRoomAlreadyExists() {
        return cfg().getString("messages.room-already-exists", "&cROOM &4{world}:{region} &cIS ALREADY REGISTERED.");
    }

    public String getRoomAdded() {
        return cfg().getString("messages.room-added", "&bʀᴏᴏᴍ &3{region} &bᴀᴅᴅᴇᴅ ᴛᴏ ᴅᴜɴɢᴇᴏɴ &3{dungeon}&b.");
    }

    public String getRoomRemoved() {
        return cfg().getString("messages.room-removed", "&bʀᴏᴏᴍ &3{region} &bʀᴇᴍᴏᴠᴇᴅ ꜰʀᴏᴍ ᴅᴜɴɢᴇᴏɴ &3{dungeon}&b.");
    }

    public String getRoomNotFound() {
        return cfg().getString("messages.room-not-found", "&cʀᴏᴏᴍ &4{region} &cɴᴏᴛ ꜰᴏᴜɴᴅ ɪɴ ᴅᴜɴɢᴇᴏɴ &4{dungeon}&c.");
    }

    public String getRoomNoSpawn() {
        return cfg().getString("messages.room-no-spawn", "&cʀᴇɢɪsᴛᴇʀ ᴀ sᴘᴀᴡɴ ʀᴇɢɪᴏɴ ꜰᴏʀ ᴅᴜɴɢᴇᴏɴ &4{dungeon} &cʙᴇꜰᴏʀᴇ ᴀᴅᴅɪɴɢ ʀᴏᴏᴍs.");
    }

    public String getSpawnSet() {
        return cfg().getString("messages.spawn-set", "&bsᴘᴀᴡɴ ꜰᴏʀ ᴅᴜɴɢᴇᴏɴ &3{dungeon} &bsᴇᴛ ᴀᴛ ʏᴏᴜʀ ʟᴏᴄᴀᴛɪᴏɴ.");
    }

    public String getSpawnNotInRegion() {
        return cfg().getString("messages.spawn-not-in-region", "&cʏᴏᴜ ᴍᴜsᴛ ʙᴇ ɪɴsɪᴅᴇ ᴛʜᴇ ʀᴇɢɪsᴛᴇʀᴇᴅ sᴘᴀᴡɴ ʀᴇɢɪᴏɴ ᴛᴏ sᴇᴛ sᴘᴀᴡɴ.");
    }

    public String getSpawnRegionAdded() {
        return cfg().getString("messages.spawn-region-added", "&bsᴘᴀᴡɴ ʀᴇɢɪᴏɴ &3{region} &bʀᴇɢɪsᴛᴇʀᴇᴅ ꜰᴏʀ ᴅᴜɴɢᴇᴏɴ &3{dungeon}&b.");
    }

    public String getKillsUpdated() {
        return cfg().getString("messages.kills-updated", "&bʀᴇQᴜɪʀᴇᴅ ᴋɪʟʟs ꜰᴏʀ &3{region} &bᴜᴘᴅᴀᴛᴇᴅ ᴛᴏ &3{kills}&b.");
    }

    public String getVersion() {
        return cfg().getString("messages.version", "&bᴅᴜɴɢᴇᴏɴʀᴏᴏᴍs &3v{version} &b| &3ʙʏ evnrca");
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
                "&3/dr showborder [all] &7- Toggle border visualization",
                "&3/dr version &7- Show plugin version and author") : lines;
    }
}
