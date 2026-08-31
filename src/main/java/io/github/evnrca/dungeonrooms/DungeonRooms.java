package io.github.evnrca.dungeonrooms;

import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Main plugin entry point for DungeonRooms.
 *
 * @author evnrca
 * @version 2.3.3
 * @github https://github.com/evnrca/DungeonRooms
 */
public final class DungeonRooms extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 33738;

    private ConfigManager configManager;
    private DungeonDataManager dungeonDataManager;
    private PlayerDataManager playerDataManager;
    private DungeonManager dungeonManager;
    private ProgressManager progressManager;
    private BorderVisualizer borderVisualizer;

    @Override
    public void onEnable() {
        new Metrics(this, BSTATS_PLUGIN_ID);

        configManager = new ConfigManager(this);

        WorldGuardHook worldGuardHook = new WorldGuardHook();
        MythicMobsHook mythicMobsHook = new MythicMobsHook();
        dungeonDataManager = new DungeonDataManager(this);
        playerDataManager = new PlayerDataManager(this);
        dungeonManager = new DungeonManager(worldGuardHook, dungeonDataManager);
        progressManager = new ProgressManager(playerDataManager);
        progressManager.loadAll(playerDataManager.loadAllSync());

        dungeonManager.loadFromStorage(() -> registerRuntime(worldGuardHook, mythicMobsHook));
    }

    private void registerRuntime(WorldGuardHook worldGuardHook, MythicMobsHook mythicMobsHook) {
        DenialHandler denialHandler = new DenialHandler(this, configManager, progressManager);
        borderVisualizer = new BorderVisualizer(this, configManager, dungeonManager, worldGuardHook);

        getServer().getPluginManager().registerEvents(
                new DungeonListener(configManager, dungeonManager, progressManager,
                        mythicMobsHook, denialHandler, borderVisualizer),
                this);

        DungeonCommand dungeonCommand = new DungeonCommand(this, configManager, dungeonManager,
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
            progressManager.saveAllSync();
            progressManager.resetAll();
        }
        if (dungeonManager != null) {
            dungeonManager.saveSync();
        }
    }
}
