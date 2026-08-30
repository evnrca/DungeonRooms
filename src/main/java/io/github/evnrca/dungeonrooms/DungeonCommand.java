package io.github.evnrca.dungeonrooms;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
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
 * Handles the {@code /dr} command and all v2 subcommands.
 *
 * @author evnrca
 */
public final class DungeonCommand implements CommandExecutor, TabCompleter {

    private final DungeonRooms plugin;
    private final ConfigManager config;
    private final DungeonManager dungeonManager;
    private final ProgressManager progress;
    private final BorderVisualizer borderVisualizer;
    private final WorldGuardHook worldGuardHook;

    public DungeonCommand(DungeonRooms plugin, ConfigManager config,
                          DungeonManager dungeonManager, ProgressManager progress,
                          BorderVisualizer borderVisualizer, WorldGuardHook worldGuardHook) {
        this.plugin = plugin;
        this.config = config;
        this.dungeonManager = dungeonManager;
        this.progress = progress;
        this.borderVisualizer = borderVisualizer;
        this.worldGuardHook = worldGuardHook;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
            case "create":
                return create(sender, args);
            case "add":
                return add(sender, args);
            case "setspawn":
                return setSpawn(sender, args);
            case "remove":
                return remove(sender, args);
            case "edit":
                return edit(sender, args);
            case "list":
                return list(sender);
            case "status":
                return status(sender, args);
            case "reset":
                return reset(sender, args);
            case "reload":
                return reload(sender);
            case "showborder":
                return showBorder(sender, args);
            case "version":
                return version(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean create(CommandSender sender, String[] args) {
        if (!admin(sender)) {
            return true;
        }
        if (args.length != 4) {
            sender.sendMessage(color(config.getPrefix() + config.getUsageCreate()));
            return true;
        }

        String world = args[1];
        String region = args[2];
        String dungeon = args[3];
        if (!validWorld(sender, world) || !validRegion(sender, world, region)) {
            return true;
        }
        if (dungeonManager.getDungeon(dungeon) != null) {
            sender.sendMessage(color(config.getPrefix() + config.getDungeonAlreadyExists()
                    .replace("{dungeon}", dungeon)));
            return true;
        }
        dungeonManager.createDungeon(world, region, dungeon);
        sender.sendMessage(color(config.getPrefix() + config.getDungeonCreated()
                .replace("{dungeon}", dungeon)));
        return true;
    }

    private boolean add(CommandSender sender, String[] args) {
        if (!admin(sender)) {
            return true;
        }
        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }
        if (args[1].equalsIgnoreCase("spawn")) {
            return addSpawn(sender, args);
        }
        if (args[1].equalsIgnoreCase("room")) {
            return addRoom(sender, args);
        }
        sendHelp(sender);
        return true;
    }

    private boolean addSpawn(CommandSender sender, String[] args) {
        if (args.length != 5) {
            sender.sendMessage(color(config.getPrefix() + config.getUsageAddSpawn()));
            return true;
        }
        String world = args[2];
        String region = args[3];
        String dungeon = args[4];
        if (dungeonManager.getDungeon(dungeon) == null) {
            sender.sendMessage(color(config.getPrefix() + config.getDungeonNotFound()
                    .replace("{dungeon}", dungeon)));
            return true;
        }
        if (!validWorld(sender, world) || !validRegion(sender, world, region)) {
            return true;
        }
        dungeonManager.setSpawnRegion(dungeon, world, region);
        sender.sendMessage(color(config.getPrefix() + config.getSpawnRegionAdded()
                .replace("{region}", region)
                .replace("{dungeon}", dungeon)));
        return true;
    }

    private boolean addRoom(CommandSender sender, String[] args) {
        if (args.length != 5) {
            sender.sendMessage(color(config.getPrefix() + config.getUsageAddRoom()));
            return true;
        }
        String dungeon = args[2];
        String region = args[3];
        int kills = parseKills(sender, args[4]);
        if (kills < 0) {
            return true;
        }
        DungeonManager.DungeonData data = dungeonManager.getDungeon(dungeon);
        if (data == null) {
            sender.sendMessage(color(config.getPrefix() + config.getDungeonNotFound()
                    .replace("{dungeon}", dungeon)));
            return true;
        }
        if (data.spawnRegion == null) {
            sender.sendMessage(color(config.getPrefix() + config.getRoomNoSpawn()
                    .replace("{dungeon}", dungeon)));
            return true;
        }
        if (!validRegion(sender, data.world, region)) {
            return true;
        }
        if (!dungeonManager.addRoom(dungeon, region, kills)) {
            sender.sendMessage(color(config.getPrefix() + config.getRoomAlreadyExists()
                    .replace("{world}", data.world)
                    .replace("{region}", region)));
            return true;
        }
        sender.sendMessage(color(config.getPrefix() + config.getRoomAdded()
                .replace("{region}", region)
                .replace("{dungeon}", dungeon)
                .replace("{kills}", String.valueOf(kills))));
        return true;
    }

    private boolean setSpawn(CommandSender sender, String[] args) {
        if (!admin(sender)) {
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(config.getPrefix() + config.getOnlyPlayers()));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(color(config.getPrefix() + config.getUsageSetSpawn()));
            return true;
        }
        String dungeon = args[1];
        if (dungeonManager.getDungeon(dungeon) == null) {
            sender.sendMessage(color(config.getPrefix() + config.getDungeonNotFound()
                    .replace("{dungeon}", dungeon)));
            return true;
        }
        if (!dungeonManager.setSpawnLocation(dungeon, player.getLocation())) {
            sender.sendMessage(color(config.getPrefix() + config.getSpawnNotInRegion()));
            return true;
        }
        sender.sendMessage(color(config.getPrefix() + config.getSpawnSet()
                .replace("{dungeon}", dungeon)));
        return true;
    }

    private boolean remove(CommandSender sender, String[] args) {
        if (!admin(sender)) {
            return true;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("room")) {
            return removeRoom(sender, args);
        }
        if (args.length != 2) {
            sender.sendMessage(color(config.getPrefix() + config.getUsageRemove()));
            return true;
        }
        String dungeon = args[1];
        if (!dungeonManager.removeDungeon(dungeon)) {
            sender.sendMessage(color(config.getPrefix() + config.getDungeonNotFound()
                    .replace("{dungeon}", dungeon)));
            return true;
        }
        sender.sendMessage(color(config.getPrefix() + config.getDungeonRemoved()
                .replace("{dungeon}", dungeon)));
        return true;
    }

    private boolean removeRoom(CommandSender sender, String[] args) {
        if (args.length != 4) {
            sender.sendMessage(color(config.getPrefix() + config.getUsageRemoveRoom()));
            return true;
        }
        String dungeon = args[2];
        String region = args[3];
        if (!dungeonManager.removeRoom(dungeon, region)) {
            sender.sendMessage(color(config.getPrefix() + config.getRoomNotFound()
                    .replace("{dungeon}", dungeon)
                    .replace("{region}", region)));
            return true;
        }
        sender.sendMessage(color(config.getPrefix() + config.getRoomRemoved()
                .replace("{dungeon}", dungeon)
                .replace("{region}", region)));
        return true;
    }

    private boolean edit(CommandSender sender, String[] args) {
        if (!admin(sender)) {
            return true;
        }
        if (args.length != 5 || !args[1].equalsIgnoreCase("kills")) {
            sender.sendMessage(color(config.getPrefix() + config.getUsageEditKills()));
            return true;
        }
        String dungeon = args[2];
        String region = args[3];
        int kills = parseKills(sender, args[4]);
        if (kills < 0) {
            return true;
        }
        if (!dungeonManager.editRoomKills(dungeon, region, kills)) {
            sender.sendMessage(color(config.getPrefix() + config.getRoomNotFound()
                    .replace("{dungeon}", dungeon)
                    .replace("{region}", region)));
            return true;
        }
        sender.sendMessage(color(config.getPrefix() + config.getKillsUpdated()
                .replace("{region}", region)
                .replace("{kills}", String.valueOf(kills))));
        return true;
    }

    private boolean list(CommandSender sender) {
        if (!admin(sender)) {
            return true;
        }
        sender.sendMessage(color(config.getPrefix() + config.getListHeader()));
        if (dungeonManager.getDungeons().isEmpty()) {
            sender.sendMessage(color("  " + config.getListEmpty()));
            return true;
        }
        for (DungeonManager.DungeonData dungeon : dungeonManager.getDungeons().values()) {
            sender.sendMessage(color(config.getListDungeon()
                    .replace("{dungeon}", dungeon.dungeonName)
                    .replace("{world}", dungeon.world)
                    .replace("{region}", dungeon.region)));
            for (DungeonManager.RoomData room : dungeon.rooms.values()) {
                sender.sendMessage(color(config.getListRoom()
                        .replace("{sequence}", String.valueOf(room.sequence))
                        .replace("{region}", room.region)
                        .replace("{kills}", String.valueOf(room.requiredKills))));
            }
        }
        return true;
    }

    private boolean status(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("dungeonrooms.status.others")) {
                sender.sendMessage(color(config.getPrefix() + config.getNoPermission()));
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color(config.getPrefix() + config.getConsoleSpecifyPlayer()));
                return true;
            }
            if (!sender.hasPermission("dungeonrooms.status")) {
                sender.sendMessage(color(config.getPrefix() + config.getNoPermission()));
                return true;
            }
            target = player;
        }
        if (target == null) {
            sender.sendMessage(color(config.getPrefix() + config.getPlayerNotFound()));
            return true;
        }
        sender.sendMessage(color(config.getPrefix() + config.getStatusHeader()
                .replace("{player}", target.getName())));
        for (DungeonManager.DungeonData dungeon : dungeonManager.getDungeons().values()) {
            sender.sendMessage(color(config.getStatusDungeon().replace("{dungeon}", dungeon.dungeonName)));
            for (DungeonManager.RoomData room : dungeon.rooms.values()) {
                boolean unlocked = progress.isUnlocked(target.getUniqueId(), room.dungeonName, room.region);
                sender.sendMessage(color(config.getStatusEntry()
                        .replace("{region}", room.region)
                        .replace("{current}", String.valueOf(progress.getKills(target.getUniqueId(), room.dungeonName, room.region)))
                        .replace("{required}", String.valueOf(room.requiredKills))
                        .replace("{state}", unlocked ? config.getStatusUnlocked() : config.getStatusLocked())));
            }
        }
        return true;
    }

    private boolean reset(CommandSender sender, String[] args) {
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
            String dungeon = args[2];
            if (dungeonManager.getDungeon(dungeon) == null) {
                sender.sendMessage(color(config.getPrefix() + config.getDungeonNotFound()
                        .replace("{dungeon}", dungeon)));
                return true;
            }
            progress.resetPlayerDungeon(target.getUniqueId(), dungeon);
            sender.sendMessage(color(config.getPrefix() + config.getResetDungeonDone()
                    .replace("{player}", target.getName())
                    .replace("{dungeon}", dungeon)));
        } else {
            progress.resetPlayer(target.getUniqueId());
            sender.sendMessage(color(config.getPrefix() + config.getResetAllDone()
                    .replace("{player}", target.getName())));
        }
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!admin(sender)) {
            return true;
        }
        config.reload();
        dungeonManager.loadFromStorage(() -> borderVisualizer.refreshRegion("*"));
        sender.sendMessage(color(config.getPrefix() + config.getReloadDone()));
        return true;
    }

    private boolean showBorder(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(config.getPrefix() + config.getOnlyPlayers()));
            return true;
        }
        if (!player.hasPermission("dungeonrooms.showborder")) {
            player.sendMessage(color(config.getPrefix() + config.getNoPermission()));
            return true;
        }
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("all")) {
                borderVisualizer.toggleAll(player);
                return true;
            }
            if (args[1].equalsIgnoreCase("spawn")) {
                borderVisualizer.toggleSpawn(player);
                return true;
            }
        }
        borderVisualizer.toggle(player);
        return true;
    }

    private boolean version(CommandSender sender) {
        sender.sendMessage(color(config.getPrefix() + config.getVersion()
                .replace("{version}", plugin.getDescription().getVersion())));
        return true;
    }

    private boolean admin(CommandSender sender) {
        if (!sender.hasPermission("dungeonrooms.admin")) {
            sender.sendMessage(color(config.getPrefix() + config.getNoPermission()));
            return false;
        }
        return true;
    }

    private boolean validWorld(CommandSender sender, String worldName) {
        if (Bukkit.getWorld(worldName) != null) {
            return true;
        }
        sender.sendMessage(color(config.getPrefix() + config.getWorldNotFound()
                .replace("{world}", worldName)));
        return false;
    }

    private boolean validRegion(CommandSender sender, String worldName, String region) {
        World world = Bukkit.getWorld(worldName);
        if (world != null && worldGuardHook.getRegion(world, region) != null) {
            return true;
        }
        sender.sendMessage(color(config.getPrefix() + config.getRegionNotFound()
                .replace("{world}", worldName)
                .replace("{region}", region)));
        return false;
    }

    private int parseKills(CommandSender sender, String value) {
        try {
            int kills = Integer.parseInt(value);
            if (kills < 0) {
                sender.sendMessage(color(config.getPrefix() + config.getKillsMustBeNumber()));
                return -1;
            }
            return kills;
        } catch (NumberFormatException e) {
            sender.sendMessage(color(config.getPrefix() + config.getKillsMustBeNumber()));
            return -1;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color(config.getPrefix() + config.getHelpHeader()));
        for (String line : config.getHelpLines()) {
            sender.sendMessage(color("  " + line));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(args[0], "create", "add", "setspawn", "remove", "edit", "list", "status", "reset", "reload", "showborder", "version");
        }
        String sub = args[0].toLowerCase(java.util.Locale.ROOT);
        if (args.length == 2) {
            if (sub.equals("create")) {
                return filter(args[1], worlds());
            }
            if (sub.equals("add")) {
                return filter(args[1], "spawn", "room");
            }
            if (sub.equals("setspawn") || sub.equals("remove")) {
                return filter(args[1], dungeons());
            }
            if (sub.equals("edit")) {
                return filter(args[1], "kills");
            }
            if (sub.equals("status") || sub.equals("reset")) {
                return filter(args[1], players());
            }
            if (sub.equals("showborder")) {
                return filter(args[1], "all", "spawn");
            }
        }
        if (args.length == 3) {
            if (sub.equals("create")) {
                return filter(args[2], regions(args[1]));
            }
            if (sub.equals("add") && args[1].equalsIgnoreCase("spawn")) {
                return filter(args[2], worlds());
            }
            if (sub.equals("add") && args[1].equalsIgnoreCase("room")) {
                return filter(args[2], dungeons());
            }
            if (sub.equals("remove") && args[1].equalsIgnoreCase("room")) {
                return filter(args[2], dungeons());
            }
            if (sub.equals("edit") && args[1].equalsIgnoreCase("kills")) {
                return filter(args[2], dungeons());
            }
            if (sub.equals("reset")) {
                return filter(args[2], dungeons());
            }
        }
        if (args.length == 4) {
            if (sub.equals("add") && args[1].equalsIgnoreCase("spawn")) {
                return filter(args[3], regions(args[2]));
            }
            if (sub.equals("add") && args[1].equalsIgnoreCase("room")) {
                return filter(args[3], rooms(args[2]));
            }
            if (sub.equals("remove") && args[1].equalsIgnoreCase("room")) {
                return filter(args[3], rooms(args[2]));
            }
            if (sub.equals("edit") && args[1].equalsIgnoreCase("kills")) {
                return filter(args[3], rooms(args[2]));
            }
        }
        if (args.length == 5 && sub.equals("add") && args[1].equalsIgnoreCase("spawn")) {
            return filter(args[4], dungeons());
        }
        return Collections.emptyList();
    }

    private List<String> worlds() {
        return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
    }

    private List<String> players() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    private List<String> dungeons() {
        return new ArrayList<>(dungeonManager.getDungeons().keySet());
    }

    private List<String> regions(String worldName) {
        World world = Bukkit.getWorld(worldName);
        return world == null ? Collections.emptyList() : new ArrayList<>(worldGuardHook.getRegionNames(world));
    }

    private List<String> rooms(String dungeonName) {
        DungeonManager.DungeonData dungeon = dungeonManager.getDungeon(dungeonName);
        return dungeon == null ? Collections.emptyList() : new ArrayList<>(dungeon.rooms.keySet());
    }

    private List<String> filter(String input, String... candidates) {
        return filter(input, Arrays.asList(candidates));
    }

    private List<String> filter(String input, List<String> candidates) {
        List<String> results = new ArrayList<>();
        StringUtil.copyPartialMatches(input, candidates, results);
        return results;
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
