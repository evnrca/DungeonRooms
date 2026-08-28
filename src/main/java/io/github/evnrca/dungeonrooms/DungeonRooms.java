package io.github.evnrca.dungeonrooms;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Main plugin entry point for DungeonRooms.
 *
 * @author evnrca
 * @version 1.0.0
 * @github https://github.com/evnrca/DungeonRooms
 */
public final class DungeonRooms extends JavaPlugin {

    private ConfigManager configManager;
    private RoomManager roomManager;
    private ProgressManager progressManager;
    private BorderVisualizer borderVisualizer;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);

        WorldGuardHook worldGuardHook = new WorldGuardHook();
        MythicMobsHook mythicMobsHook = new MythicMobsHook();

        roomManager = new RoomManager(worldGuardHook, configManager);
        roomManager.refreshRegions();

        progressManager = new ProgressManager();
        DenialHandler denialHandler = new DenialHandler(configManager, progressManager);
        borderVisualizer = new BorderVisualizer(this, configManager, roomManager, worldGuardHook);

        getServer().getPluginManager().registerEvents(
                new DungeonListener(configManager, roomManager, progressManager, worldGuardHook,
                        mythicMobsHook, denialHandler, borderVisualizer),
                this);

        DungeonCommand dungeonCommand = new DungeonCommand(this, configManager, roomManager,
                progressManager, borderVisualizer, worldGuardHook);
        PluginCommand command = Objects.requireNonNull(getCommand("dr"), "Command /dr is missing from plugin.yml");
        command.setExecutor(dungeonCommand);
        command.setTabCompleter(dungeonCommand);

        getLogger().info("[DungeonRooms] Enabled | github.com/evnrca");
    }

    @Override
    public void onDisable() {
        if (borderVisualizer != null) {
            borderVisualizer.disableAll();
        }
        if (progressManager != null) {
            progressManager.resetAll();
        }
    }
}
