# DungeonRooms

[![Author](https://img.shields.io/badge/author-evnrca-0ea5e9?style=flat-square)](https://github.com/evnrca)

DungeonRooms is a lightweight Minecraft Paper plugin that gates WorldGuard dungeon rooms behind MythicMobs kill requirements. Players progress through rooms in registration order, unlock completed rooms, and can optionally visualize the current room border with particles.

Author: [evnrca](https://github.com/evnrca)

Repository: https://github.com/evnrca/DungeonRooms

## Compatibility

| Component | Required Version |
| --- | --- |
| Paper | 1.21.1+ |
| Java | 21 |
| WorldGuard | 7.0+ |
| MythicMobs | 5.6+ |

## Features

| Feature | Description |
| --- | --- |
| Room progression | Rooms unlock in registration order using MythicMobs kill counts. |
| Per-world room keys | Room keys use `worldName:regionName`, so different worlds can reuse the same region names. |
| WorldGuard gating | Entry is checked directly against WorldGuard regions. |
| MythicMobs tracking | Only MythicMobs kills count toward room requirements. |
| No persistence requirement | Player progress is in-memory and resets on restart, logout, death, teleport exit, or world exit. |
| Dungeon-world cache | Only worlds with registered dungeon rooms are processed. Non-dungeon worlds have no event overhead. |
| Denial actions | Blocked entry can teleport, cancel, knock back, or apply velocity. |
| Border visualizer | Players can toggle private particles along the current room's WorldGuard bounding box. |
| Small JAR | Dependencies are `compileOnly`; no libraries are shaded into the plugin JAR. |

## Installation

1. Download the latest `DungeonRooms-1.0.0.jar` from the releases page.
2. Place the JAR in your server's `plugins` folder.
3. Install the required dependencies:
   - [WorldGuard](https://enginehub.org/worldguard/)
   - [WorldEdit](https://enginehub.org/worldedit/)
   - [MythicMobs](https://mythiccraft.io/)
4. Start or restart the server.
5. Edit `plugins/DungeonRooms/config.yml` if needed.
6. Run `/dr reload` after config changes.

## Commands

| Command | Description | Permission | Default |
| --- | --- | --- | --- |
| `/dr add <world> <region> <kills>` | Register a WorldGuard region as a dungeon room. | `dungeonrooms.admin` | OP |
| `/dr remove <world> <region>` | Remove a registered dungeon room. | `dungeonrooms.admin` | OP |
| `/dr list` | List registered dungeon rooms in progression order. | `dungeonrooms.admin` | OP |
| `/dr status [player]` | View your own progress or another player's progress. | `dungeonrooms.status`, `dungeonrooms.status.others` | TRUE, OP |
| `/dr reset <player> [region]` | Reset all progress or one `world:region` room for a player. | `dungeonrooms.reset` | OP |
| `/dr reload` | Reload config and refresh WorldGuard region references. | `dungeonrooms.admin` | OP |
| `/dr showborder` | Toggle private border visualization. | `dungeonrooms.showborder` | TRUE |

## Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `dungeonrooms.admin` | Allows `/dr add`, `/dr remove`, `/dr list`, and `/dr reload`. | OP |
| `dungeonrooms.status` | Allows checking your own dungeon status. | TRUE |
| `dungeonrooms.status.others` | Allows checking another player's dungeon status. | OP |
| `dungeonrooms.reset` | Allows resetting player dungeon progress. | OP |
| `dungeonrooms.showborder` | Allows toggling border visualization. | TRUE |

## Config

```yaml
# Controls what happens when a player attempts to enter a locked room.
denial:
  # Valid values: CANCEL, VELOCITY, TELEPORT, KNOCKBACK.
  action: CANCEL
  velocity:
    # Horizontal strength used by VELOCITY and KNOCKBACK.
    horizontal: 1.5
    # Upward velocity used by VELOCITY and KNOCKBACK.
    vertical: 0.4
  # Title shown when entry is denied.
  title: '&b&lʀᴏᴏᴍ ʟᴏᴄᴋᴇᴅ!'
  # Subtitle shown when entry is denied. Supports {remaining}.
  subtitle: '&bᴋɪʟʟ &3{remaining} &bᴍᴏʀᴇ ᴍᴏʙs ᴛᴏ ᴘʀᴏᴄᴇᴇᴅ.'
  # Bukkit sound name played on denial.
  sound: ENTITY_VILLAGER_NO
  # Sound volume.
  sound-volume: 1.0
  # Sound pitch.
  sound-pitch: 1.0

# Controls progress feedback after MythicMob kills.
progress-display:
  action-bar:
    # Whether to show action bar progress.
    enabled: true
    # Supports {current} and {required}.
    format: '&bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ'
  chat:
    # Whether to show chat progress.
    enabled: true
    # Supports {current} and {required}.
    format: '&8[&bᴅᴜɴɢᴇᴏɴs&8] &bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ'
    # Seconds between chat progress messages per player.
    cooldown: 5

# Controls private particle border rendering.
border-visualizer:
  # Master toggle for /dr showborder.
  enabled: true
  # Bukkit particle type.
  particle-type: FLAME
  # Distance between particles along region edges. Lower means denser.
  particle-density: 0.5
  # Re-render interval in ticks.
  interval-ticks: 20
  messages:
    # Sent when a player toggles visualization on.
    toggled-on: '&bʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ &3ᴇɴᴀʙʟᴇᴅ.'
    # Sent when a player toggles visualization off.
    toggled-off: '&bʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ &3ᴅɪsᴀʙʟᴇᴅ.'
    # Sent if the player is not inside a registered room.
    not-in-region: '&cʏᴏᴜ ᴀʀᴇ ɴᴏᴛ ɪɴsɪᴅᴇ ᴀɴʏ ʀᴇɢɪsᴛᴇʀᴇᴅ ᴅᴜɴɢᴇᴏɴ ʀᴏᴏᴍ.'
    # Sent if the feature is disabled by config.
    feature-disabled: '&cʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ ɪs ᴅɪsᴀʙʟᴇᴅ ʙʏ ᴛʜᴇ sᴇʀᴠᴇʀ.'

# General plugin messages.
messages:
  # Prefix prepended to most plugin messages.
  prefix: '&8[&bᴅᴜɴɢᴇᴏɴʀᴏᴏᴍs&8] '
  # Sent when entry is blocked. Supports {remaining} and {region}.
  requirement-not-met: '&cʏᴏᴜ ɴᴇᴇᴅ &4{remaining} &cᴍᴏʀᴇ ᴍᴏʙ ᴋɪʟʟs ᴛᴏ ᴇɴᴛᴇʀ &4{region}&c!'
  # Generic progress message. Supports {current} and {required}.
  progress: '&bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ'
  # Sent when a room is completed. Supports {region}.
  completed: '&6ʀᴏᴏᴍ &e{region} &6ᴄᴏᴍᴘʟᴇᴛᴇᴅ! &eʏᴏᴜ ᴍᴀʏ ɴᴏᴡ ᴘʀᴏᴄᴇᴇᴅ.'
  # Sent when progress resets due to death.
  progress-reset-death: '&cʏᴏᴜ ᴅɪᴇᴅ! &4ʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ.'
  # Sent when progress resets due to logout.
  progress-reset-logout: '&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ʟᴏɢᴏᴜᴛ).'
  # Sent when progress resets due to teleporting out of a dungeon world.
  progress-reset-teleport: '&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ᴛᴇʟᴇᴘᴏʀᴛ).'
  # Sent when progress resets due to leaving a dungeon world.
  progress-reset-world-exit: '&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ʟᴇꜰᴛ ᴡᴏʀʟᴅ).'
  # Sent when /dr add receives an invalid world. Supports {world}.
  world-not-found: '&cᴡᴏʀʟᴅ &4{world} &cᴅᴏᴇs ɴᴏᴛ ᴇxɪsᴛ.'
  # Sent when /dr add receives an invalid region. Supports {world} and {region}.
  region-not-found: '&cʀᴇɢɪᴏɴ &4{region} &cᴅᴏᴇs ɴᴏᴛ ᴇxɪsᴛ ɪɴ ᴡᴏʀʟᴅ &4{world}&c.'
  # Sent when a room already exists. Supports {world} and {region}.
  room-already-exists: '&cʀᴏᴏᴍ &4{world}:{region} &cɪs ᴀʟʀᴇᴀᴅʏ ʀᴇɢɪsᴛᴇʀᴇᴅ.'
  # Sent when a room is added. Supports {world}, {region}, and {kills}.
  room-added: '&bʀᴏᴏᴍ &3{world}:{region} &bʀᴇɢɪsᴛᴇʀᴇᴅ ᴡɪᴛʜ &3{kills} &bʀᴇQᴜɪʀᴇᴅ ᴋɪʟʟs.'
  # Sent when a room is removed. Supports {world} and {region}.
  room-removed: '&bʀᴏᴏᴍ &3{world}:{region} &bʀᴇᴍᴏᴠᴇᴅ.'
  # Sent when a room is not registered. Supports {world} and {region}.
  room-not-found: '&cʀᴏᴏᴍ &4{world}:{region} &cɪs ɴᴏᴛ ʀᴇɢɪsᴛᴇʀᴇᴅ.'

# Registered rooms. Rooms use worldName:regionName keys and are managed by /dr add and /dr remove.
rooms: {}
```

## Room Setup Guide

1. Create a WorldGuard region in the correct world.
2. Confirm the region exists with WorldGuard commands.
3. Register the room with `/dr add <world> <region> <kills>`.
4. Repeat registration in the exact order players should progress through rooms.
5. Spawn MythicMobs inside the registered regions.
6. Enter room 1 and kill the required mobs.
7. Attempt to enter room 2 to verify progression gating.
8. Use `/dr status` to inspect current progress.

Example:

```text
/dr add dungeon_world room1 10
/dr add dungeon_world room2 15
/dr add dungeon_world boss_room 1
```

## Border Visualizer

The border visualizer draws particles along all 12 edges of the WorldGuard bounding box for the registered room the player is currently inside.

Players toggle it with:

```text
/dr showborder
```

Only the player who toggled the visualizer sees their particles. The plugin uses `player.spawnParticle()` rather than broadcasting particles to the world.

Configuration options:

| Option | Description |
| --- | --- |
| `border-visualizer.enabled` | Enables or disables the entire feature. |
| `border-visualizer.particle-type` | Particle type to render. |
| `border-visualizer.particle-density` | Distance between particles along each edge. |
| `border-visualizer.interval-ticks` | How often the border redraws. |

Tasks are cancelled when the player toggles off, logs out, changes world, or the plugin disables.

## Progression Logic

Rooms are ordered by registration order in the internal `LinkedHashMap`.

| Rule | Behavior |
| --- | --- |
| Room 1 | Always freely accessible. |
| Room N | Requires completion of Room N-1. |
| Completion | Kill count in the required previous room reaches its configured requirement. |
| Unlocking | Once unlocked, a room stays unlocked until player progress resets. |
| Exiting | Players are never blocked from exiting a room. |

Example flow:

```text
Spawn -> Room 1 -> Room 2 -> Boss Room
```

1. Room 1 is free to enter.
2. Player kills the required MythicMobs in Room 1.
3. Room 2 unlocks.
4. Player kills the required MythicMobs in Room 2.
5. Boss Room unlocks.

## Reset Triggers

| Trigger | Config Message Key |
| --- | --- |
| Player dies | `progress-reset-death` |
| Player logs out | `progress-reset-logout` |
| Player teleports out of a dungeon world | `progress-reset-teleport` |
| Player leaves a dungeon world | `progress-reset-world-exit` |

## World Tracking

DungeonRooms keeps a cached `Set<String>` of world names that contain at least one registered dungeon region.

Events return immediately unless the relevant world is in that cache:

| Event | World Check |
| --- | --- |
| `PlayerMoveEvent` | Current player world must be a dungeon world. |
| `EntityDeathEvent` | Killer's current world must be a dungeon world. |
| `PlayerChangedWorldEvent` | Origin world must be a dungeon world. |

This means non-dungeon worlds are ignored and have effectively zero DungeonRooms processing overhead.

## Building From Source

Prerequisites:

| Tool | Version |
| --- | --- |
| JDK | 21 |
| Gradle | Installed locally or via wrapper |

Build command:

```bash
./gradlew shadowJar
```

Output JAR:

```text
build/libs/DungeonRooms-1.0.0.jar
```

Dependencies are declared as `compileOnly`, and the Shadow task does not shade external libraries into the final JAR.

## License

MIT

Made with ❤️ by evnrca
