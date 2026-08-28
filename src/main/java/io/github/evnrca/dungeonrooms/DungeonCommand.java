package io.github.evnrca.dungeonrooms;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the {@code /dr} command and all its subcommands.
 * <p>
 * Tab completion covers world names (online worlds) and region names
 * (WorldGuard regions in that world).
 *
 * @author evnrca
 */
public final class DungeonCommand implements CommandExecutor, TabCompleter {

    private final DungeonRooms plugin;
    private final ConfigManager config;
    private final RoomManager roomManager;
    private final ProgressManager progress;
    private final BorderVisualizer borderVisualizer;
    private final WorldGuardHook worldGuardHook;

    public DungeonCommand(DungeonRooms plugin, ConfigManager config,
                          RoomManager roomManager, ProgressManager progress,
                          BorderVisualizer borderVisualizer, WorldGuardHook worldGuardHook) {
        this.plugin = plugin;
        this.config = config;
        this.roomManager = roomManager;
        this.progress = progress;
        this.borderVisualizer = borderVisualizer;
        this.worldGuardHook = worldGuardHook;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add":
                return handleAdd(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "list":
                return handleList(sender);
            case "status":
                return handleStatus(sender, args);
            case "reset":
                return handleReset(sender, args);
            case "reload":
                return handleReload(sender);
            case "showborder":
                return handleShowBorder(sender);
            case "version":
                return handleVersion(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dungeonrooms.admin")) {
            sender.sendMessage(color(config.getPrefix() + config.getNoPermission()));
            return true;
        }
        if (args.length != 4) {
            sender.sendMessage(color(config.getPrefix() + config.getUsageAdd()));
            return true;
        }

        String worldName = args[1];
        String regionName = args[2];
        int kills;
        try {
            kills = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(color(config.getPrefix() + config.getKillsMustBeNumber()));
            return true;
        }

        if (Bukkit.getWorld(worldName) == null) {
            sender.sendMessage(color(config.getPrefix() + config.getWorldNotFound()
                    .replace("{world}", worldName)));
            return true;
        }

        if (roomManager.getRoom(worldName, regionName) != null) {
            sender.sendMessage(color(config.getPrefix() + config.getRoomAlreadyExists()
                    .replace("{world}", worldName)
                    .replace("{region}", regionName)));
            return true;
        }

        RoomManager.RoomData added = roomManager.addRoom(worldName, regionName, kills);
        if (added == null) {
            sender.sendMessage(color(config.getPrefix() + config.getRegionNotFound()
                    .replace("{world}", worldName)
                    .replace("{region}", regionName)));
            return true;
        }

        sender.sendMessage(color(config.getPrefix() + config.getRoomAdded()
                .replace("{world}", worldName)
                .replace("{region}", regionName)
                .replace("{kills}", String.valueOf(kills))));
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dungeonrooms.admin")) {
            sender.sendMessage(color(config.getPrefix() + config.getNoPermission()));
            return true;
        }
        if (args.length != 3) {
            sender.sendMessage(color(config.getPrefix() + config.getUsageRemove()));
            return true;
        }

        String worldName = args[1];
        String regionName = args[2];

        boolean removed = roomManager.removeRoom(worldName, regionName);
        if (!removed) {
            sender.sendMessage(color(config.getPrefix() + config.getRoomNotFound()
                    .replace("{world}", worldName)
                    .replace("{region}", regionName)));
            return true;
        }

        sender.sendMessage(color(config.getPrefix() + config.getRoomRemoved()
                .replace("{world}", worldName)
                .replace("{region}", regionName)));
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("dungeonrooms.admin")) {
            sender.sendMessage(color(config.getPrefix() + config.getNoPermission()));
            return true;
        }

        sender.sendMessage(color(config.getPrefix() + config.getListHeader()));
        if (roomManager.getRooms().isEmpty()) {
            sender.sendMessage(color("  " + config.getListEmpty()));
            return true;
        }

        int i = 0;
        for (RoomManager.RoomData data : roomManager.getRooms().values()) {
            sender.sendMessage(color("  " + config.getListEntry()
                    .replace("{index}", String.valueOf(i++))
                    .replace("{world}", data.world)
                    .replace("{region}", data.region)
                    .replace("{kills}", String.valueOf(data.requiredKills))));
        }
        return true;
    }

    private boolean handleStatus(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("dungeonrooms.status.others")) {
                sender.sendMessage(color(config.getPrefix() + config.getNoPermission()));
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(color(config.getPrefix() + config.getPlayerNotFound()));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(color(config.getPrefix() + config.getConsoleSpecifyPlayer()));
                return true;
            }
            if (!sender.hasPermission("dungeonrooms.status")) {
                sender.sendMessage(color(config.getPrefix() + config.getNoPermission()));
                return true;
            }
            target = (Player) sender;
        }

        sender.sendMessage(color(config.getPrefix() + config.getStatusHeader()
                .replace("{player}", target.getName())));
        for (RoomManager.RoomData data : roomManager.getRooms().values()) {
            String key = data.key();
            int kills = progress.getKills(target.getUniqueId(), key);
            boolean unlocked = progress.isUnlocked(target.getUniqueId(), key);
            sender.sendMessage(color("  " + config.getStatusEntry()
                    .replace("{region}", data.region)
                    .replace("{current}", String.valueOf(kills))
                    .replace("{required}", String.valueOf(data.requiredKills))
                    .replace("{state}", unlocked ? config.getStatusUnlocked() : config.getStatusLocked())));
        }
        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dungeonrooms.reset")) {
            sender.sendMessage(color(config.getPrefix() + config.getNoPermission()));
            return true;
        }
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(color(config.getPrefix() + config.getUsageReset()));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(color(config.getPrefix() + config.getPlayerNotFound()));
            return true;
        }

        if (args.length == 3) {
            String worldRegion = args[2];
            if (worldRegion.contains(":")) {
                progress.resetPlayerRegion(target.getUniqueId(), worldRegion);
                sender.sendMessage(color(config.getPrefix() + config.getResetRegionDone()
                        .replace("{player}", target.getName())
                        .replace("{region}", worldRegion)));
            } else {
                sender.sendMessage(color(config.getPrefix() + config.getResetRegionFormatRequired()));
            }
        } else {
            progress.resetPlayer(target.getUniqueId());
            sender.sendMessage(color(config.getPrefix() + config.getResetAllDone()
                    .replace("{player}", target.getName())));
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("dungeonrooms.admin")) {
            sender.sendMessage(color(config.getPrefix() + config.getNoPermission()));
            return true;
        }

        config.reload();
        roomManager.loadRooms();
        roomManager.refreshRegions();
        for (String key : roomManager.getRooms().keySet()) {
            borderVisualizer.refreshRegion(key);
        }
        sender.sendMessage(color(config.getPrefix() + config.getReloadDone()));
        return true;
    }

    private boolean handleShowBorder(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color(config.getPrefix() + config.getOnlyPlayers()));
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("dungeonrooms.showborder")) {
            player.sendMessage(color(config.getPrefix() + config.getNoPermission()));
            return true;
        }

        if (!config.isBorderVisualizerEnabled()) {
            player.sendMessage(color(config.getPrefix() + config.getBorderFeatureDisabled()));
            return true;
        }

        borderVisualizer.toggle(player);
        return true;
    }

    private boolean handleVersion(CommandSender sender) {
        for (String line : config.getVersionLines()) {
            sender.sendMessage(color(config.getPrefix() + line
                    .replace("{version}", plugin.getDescription().getVersion())));
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color(config.getPrefix() + config.getHelpHeader()));
        for (String line : config.getHelpLines()) {
            sender.sendMessage(color("  " + line));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return filter(sender, args[0], "add", "remove", "list", "status", "reset", "reload", "showborder", "version");
        }

        String sub = args[0].toLowerCase();

        if (args.length == 2) {
            switch (sub) {
                case "add":
                case "remove":
                    return filter(sender, args[1], getWorldNames());
                case "status":
                case "reset":
                    return filter(sender, args[1], getOnlinePlayerNames());
            }
        }

        if (args.length == 3) {
            switch (sub) {
                case "add":
                    return Collections.emptyList();
                case "remove":
                case "status":
                    String world = args[1];
                    return filter(sender, args[2], getRegionNames(world));
                case "reset":
                    return Collections.emptyList();
            }
        }

        if (args.length == 4 && sub.equals("add")) {
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    private List<String> getWorldNames() {
        return Bukkit.getWorlds().stream()
                .map(org.bukkit.World::getName)
                .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    private List<String> getRegionNames(String worldName) {
        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(worldGuardHook.getRegionNames(world));
    }

    private List<String> filter(CommandSender sender, String input, String... candidates) {
        return filter(sender, input, Arrays.asList(candidates));
    }

    private List<String> filter(CommandSender sender, String input, List<String> candidates) {
        List<String> results = new ArrayList<>();
        StringUtil.copyPartialMatches(input, candidates, results);
        return results;
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
