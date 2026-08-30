# DungeonRooms

[![Author](https://img.shields.io/badge/author-evnrca-0ea5e9?style=flat-square)](https://github.com/evnrca)

DungeonRooms lets server owners build guided dungeon runs where players unlock each room by defeating the required MythicMobs. Create dungeon areas with WorldGuard, set a safe spawn point, choose how many mobs each room requires, and let the plugin handle room locks, progress tracking, dungeon death returns, admin bypasses, and optional particle borders.

## Compatibility

| Component | Required Version |
| --- | --- |
| Paper | 1.21.1+ |
| Java | 21 |
| WorldGuard | 7.0+ |
| WorldEdit | Required by WorldGuard |
| MythicMobs | 5.6+ |
| Player Data | Gson JSON file using Paper's bundled Gson |

## Features

| Feature | Description |
| --- | --- |
| Dungeon boundaries | Register a full dungeon WorldGuard region with `/dr create`. |
| Spawn regions | Register a spawn region per dungeon before rooms can be added. |
| Dungeon death override | Fatal dungeon damage is intercepted, then the player is returned to the dungeon spawn. |
| Per-dungeon progression | Room sequence is scoped to each dungeon. |
| MythicMobs kill tracking | Only MythicMobs kills count toward room completion. |
| JSON player data | Player kills and room unlocks are stored in a flat Gson JSON file. |
| Lightweight storage | Gson is provided by Paper, so no database library is bundled. |
| Atomic async saves | Runtime dungeon setup and player progress writes are async and use temporary files before replacing JSON data. |
| Reset toggles | Death, dungeon exit, world change, and teleport resets can be enabled independently. |
| Teleport pass-through protection | Ender pearls, chorus fruit, and other teleports cannot bypass locked room entry. |
| Border visualization | Show current room, spawn region, or all dungeon/room borders privately. |
| Admin bypass | Bypass all rooms or specific dungeon rooms with permissions. |

## Installation

1. Download `DungeonRooms-2.3.1.jar`.
2. Place it in your server's `plugins` folder.
3. Install required dependencies:
   - [WorldGuard](https://enginehub.org/worldguard/)
   - [WorldEdit](https://enginehub.org/worldedit/)
   - [MythicMobs](https://mythiccraft.io/)
4. Start or restart the server.
5. Edit `plugins/DungeonRooms/config.yml` if needed.
6. Run `/dr reload` after configuration changes.

## Commands

| Command | Permission | Default | Description |
| --- | --- | --- | --- |
| `/dr create <world> <region> <dungeonName>` | `dungeonrooms.admin` | OP | Register a whole dungeon boundary. |
| `/dr add spawn <world> <region> <dungeonName>` | `dungeonrooms.admin` | OP | Register a spawn region for a dungeon. |
| `/dr setspawn <dungeonName>` | `dungeonrooms.admin` | OP | Store your current location as the dungeon return point. |
| `/dr add room <dungeonName> <region> <kills>` | `dungeonrooms.admin` | OP | Register a room inside a dungeon. Requires a spawn region first. |
| `/dr remove <dungeonName>` | `dungeonrooms.admin` | OP | Remove a dungeon and all its rooms. |
| `/dr remove room <dungeonName> <region>` | `dungeonrooms.admin` | OP | Remove one room from a dungeon. |
| `/dr edit kills <dungeonName> <region> <kills>` | `dungeonrooms.admin` | OP | Update a room's required MythicMobs kills. |
| `/dr list` | `dungeonrooms.admin` | OP | List all registered dungeons and rooms. |
| `/dr status [player]` | `dungeonrooms.status` | TRUE | Show your own progress. |
| `/dr status <other>` | `dungeonrooms.status.others` | OP | Show another player's progress. |
| `/dr reset <player> [dungeon]` | `dungeonrooms.reset` | OP | Reset all progress or one dungeon's progress. |
| `/dr reload` | `dungeonrooms.admin` | OP | Reload config and refresh cached dungeon data. |
| `/dr showborder` | `dungeonrooms.showborder` | TRUE | Toggle the room border for the room you are inside. |
| `/dr showborder spawn` | `dungeonrooms.showborder` | TRUE | Toggle the current dungeon spawn region border. |
| `/dr showborder all` | `dungeonrooms.showborder` | TRUE | Toggle all registered dungeon and room borders. |
| `/dr version` | None | TRUE | Display plugin version, author, and GitHub link. |

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `dungeonrooms.admin` | OP | Allows dungeon setup, editing, removal, list, reload, and spawn setup commands. |
| `dungeonrooms.status` | TRUE | Allows checking your own progress. |
| `dungeonrooms.status.others` | OP | Allows checking another player's progress. |
| `dungeonrooms.reset` | OP | Allows resetting player progress. |
| `dungeonrooms.showborder` | TRUE | Allows toggling border visualization. |
| `dungeonrooms.bypass` | OP | Bypasses all room progression requirements. |
| `dungeonrooms.bypass.<dungeon>.<region>` | OP | Bypasses one specific dungeon room. Names are lowercase and non-alphanumeric characters become `_`. |

Example scoped bypass:

```text
dungeonrooms.bypass.sample.room_2
```

## Configuration

DungeonRooms v2 migrates config keys on startup. Existing values are never overwritten; only missing keys are added.

```yaml
# DungeonRooms v2.3.1 Configuration
death-override:
  # Prevent real death inside registered dungeons and teleport players to dungeon spawn.
  enabled: true
  # Blindness duration after fake death, in seconds.
  blindness-seconds: 5
  title: '&c&lYOU DIED'
  subtitle: '&7Returning to dungeon spawn...'
  chat-message: '&cYou died in &4{dungeon}&c and were returned to the dungeon spawn.'
  # Leave empty to disable broadcast.
  broadcast-message: '&7{player} died in dungeon &c{dungeon}&7.'
  penalties:
    drop-items: false
    drop-exp: false
  # Console commands after dungeon death. Supports {player}, {uuid}, {dungeon}, {world}, {x}, {y}, {z}.
  commands: []

denial:
  # Valid values: KNOCKBACK, VELOCITY, TELEPORT, CANCEL.
  action: KNOCKBACK
  velocity:
    # Horizontal strength used by VELOCITY and KNOCKBACK.
    horizontal: 1.5
    # Upward strength used by VELOCITY and KNOCKBACK.
    vertical: 0.4
  # Title shown when locked-room entry is denied.
  title: '&b&lROOM LOCKED!'
  # Subtitle shown when locked-room entry is denied. Supports {remaining}.
  subtitle: '&bKill &3{remaining} &bmore mobs to proceed.'
  # Bukkit sound name played on denial.
  sound: ENTITY_VILLAGER_NO
  # Sound volume.
  sound-volume: 1.0
  # Sound pitch.
  sound-pitch: 1.0

progress-reset:
  # Reset progress when a player dies inside a dungeon.
  death: false
  # Reset progress when a player exits a registered dungeon boundary.
  dungeon-exit: true
  # Reset progress when a player changes world from a dungeon world.
  world-change: true
  # Reset progress when a player teleports out of a dungeon.
  teleport: true

progress-display:
  action-bar:
    # Show progress in the action bar after MythicMob kills.
    enabled: true
    # Supports {current} and {required}.
    format: '&bProgress: &3{current}/{required} &bmobs killed'
  chat:
    # Show progress in chat after MythicMob kills.
    enabled: true
    # Supports {current} and {required}.
    format: '&8[&bDungeons&8] &bProgress: &3{current}/{required} &bmobs killed'
    # Seconds between chat progress messages per player.
    cooldown: 5

border-visualizer:
  # Master toggle for /dr showborder, /dr showborder spawn, and /dr showborder all.
  enabled: true
  # Particle used for room borders.
  room-particle-type: FLAME
  # Particle used for spawn region borders.
  spawn-particle-type: END_ROD
  # Particle used for dungeon boundary borders.
  dungeon-particle-type: END_ROD
  # Distance between particles along border edges. Lower is denser.
  particle-density: 0.5
  # Render interval in ticks.
  interval-ticks: 20
  messages:
    # Sent when current-room border visualization is enabled.
    toggled-on: '&bBorder visualization &3enabled.'
    # Sent when current-room border visualization is disabled.
    toggled-off: '&bBorder visualization &3disabled.'
    # Sent when /dr showborder is used outside a registered room.
    not-in-region: '&cYou are not inside any registered dungeon room.'
    # Sent when border visualization is disabled globally.
    feature-disabled: '&cBorder visualization is disabled by the server.'
    spawn-toggled-on: '&bSpawn border visualization &3enabled.'
    spawn-toggled-off: '&bSpawn border visualization &3disabled.'

messages:
  # Editable prefix for plugin chat messages.
  prefix: '&8[&bDungeonRooms&8] '
  # Sent when a player tries to enter a locked room. Supports {remaining} and {region}.
  requirement-not-met: '&cYou need &4{remaining} &cmore mob kills to enter &4{region}&c!'
  # Generic progress message. Supports {current} and {required}.
  progress: '&bProgress: &3{current}/{required} &bmobs killed'
  # Sent when a room is completed. Supports {region}.
  completed: '&6Room &e{region} &6completed! &eYou may now proceed.'
  # Sent when progress resets due to death.
  progress-reset-death: '&cYou died! &4Your dungeon progress has been reset.'
  # Sent when progress resets due to logout.
  progress-reset-logout: '&cYour dungeon progress has been reset &4(logout).'
  # Sent when progress resets due to teleport.
  progress-reset-teleport: '&cYour dungeon progress has been reset &4(teleport).'
  # Sent when progress resets due to world exit/change.
  progress-reset-world-exit: '&cYour dungeon progress has been reset &4(left world).'
  # Sent when a world cannot be found. Supports {world}.
  world-not-found: '&cWorld &4{world} &cdoes not exist.'
  # Sent when a WorldGuard region cannot be found. Supports {region} and {world}.
  region-not-found: '&cRegion &4{region} &cdoes not exist in world &4{world}&c.'
  dungeon-already-exists: '&cDungeon &4{dungeon} &cis already registered.'
  dungeon-not-found: '&cDungeon &4{dungeon} &cis not registered.'
  dungeon-created: '&bDungeon &3{dungeon} &bcreated.'
  dungeon-removed: '&bDungeon &3{dungeon} &bremoved.'
  room-added: '&bRoom &3{region} &badded to dungeon &3{dungeon}&b.'
  room-removed: '&bRoom &3{region} &bremoved from dungeon &3{dungeon}&b.'
  room-not-found: '&cRoom &4{region} &cnot found in dungeon &4{dungeon}&c.'
  room-no-spawn: '&cRegister a spawn region for dungeon &4{dungeon} &cbefore adding rooms.'
  spawn-set: '&bSpawn for dungeon &3{dungeon} &bset at your location.'
  spawn-not-in-region: '&cYou must be inside the registered spawn region to set spawn.'
  spawn-region-added: '&bSpawn region &3{region} &bregistered for dungeon &3{dungeon}&b.'
  kills-updated: '&bRequired kills for &3{region} &bupdated to &3{kills}&b.'
```

## JSON Storage

DungeonRooms stores setup data and player progress in separate JSON files:

```text
plugins/DungeonRooms/
  config.yml
  dungeons.json
  playerdata.json
```

No external database is used. Gson comes from Paper, so DungeonRooms does not shade SQLite, H2, Gson, or any other storage library.

`dungeons.json` stores dungeon setup data:

```json
{
  "sample": {
    "world": "dungeon_world",
    "region": "sample_boundary",
    "spawnWorld": "dungeon_world",
    "spawnRegion": "sample_spawn",
    "spawnLocation": {
      "world": "dungeon_world",
      "x": 100.5,
      "y": 64.0,
      "z": 200.5,
      "yaw": 90.0,
      "pitch": 0.0
    },
    "rooms": [
      {
        "world": "dungeon_world",
        "region": "room1",
        "requiredKills": 10,
        "sequence": 0
      },
      {
        "world": "dungeon_world",
        "region": "room2",
        "requiredKills": 15,
        "sequence": 1
      }
    ]
  }
}
```

`playerdata.json` stores player progress:

```text
Map<UUID, Map<dungeonName, Map<region, int[]>>>
```

Array values:

| Index | Meaning |
| --- | --- |
| `int[0]` | Kill count |
| `int[1]` | Unlock state, `0` locked or `1` unlocked |

All dungeon setup data and player data load synchronously during plugin enable into memory. Runtime dungeon setup changes, kill increments, and unlock changes save asynchronously through the Bukkit scheduler. Plugin shutdown writes synchronously to avoid data loss.

Saves are atomic: DungeonRooms writes `.tmp` files first, then replaces the real JSON files.

## Setup Guide

1. Create a WorldGuard region for the whole dungeon boundary.
2. Register it with `/dr create <world> <region> <dungeonName>`.
3. Create a WorldGuard region for the dungeon spawn area.
4. Register the spawn region with `/dr add spawn <world> <region> <dungeonName>`.
5. Stand inside that spawn region and run `/dr setspawn <dungeonName>`.
6. Create WorldGuard regions for each room.
7. Add rooms in progression order with `/dr add room <dungeonName> <region> <kills>`.
8. Spawn MythicMobs inside rooms.
9. Use `/dr status` to verify kill progress and unlocks.

Example:

```text
/dr create dungeon_world sample_boundary sample
/dr add spawn dungeon_world sample_spawn sample
/dr setspawn sample
/dr add room sample room1 10
/dr add room sample room2 15
/dr add room sample boss_room 1
```

## Region Setup Visualization

Recommended layout:

```text
+--------------------------------------------------+
| WorldGuard region: sample_boundary               |
| Dungeon name: sample                             |
|                                                  |
|  +--------------+                                |
|  | sample_spawn |  <- /dr add spawn ...          |
|  | spawn point  |  <- /dr setspawn sample        |
|  +--------------+                                |
|          |                                       |
|          v                                       |
|  +--------------+    +--------------+            |
|  | room1        | -> | room2        |            |
|  | 10 kills     |    | 15 kills     |            |
|  +--------------+    +--------------+            |
|                              |                   |
|                              v                   |
|                       +--------------+           |
|                       | boss_room    |           |
|                       | 1 kill       |           |
|                       +--------------+           |
+--------------------------------------------------+
```

Region roles:

| Region Type | WorldGuard Region Example | Registered With | Purpose |
| --- | --- | --- | --- |
| Dungeon boundary | `sample_boundary` | `/dr create dungeon_world sample_boundary sample` | Defines the whole dungeon area. |
| Spawn region | `sample_spawn` | `/dr add spawn dungeon_world sample_spawn sample` | Area where admins must stand to set the dungeon return point. |
| Room region | `room1` | `/dr add room sample room1 10` | First room, freely accessible. |
| Room region | `room2` | `/dr add room sample room2 15` | Requires completion of `room1`. |
| Boss room | `boss_room` | `/dr add room sample boss_room 1` | Final room in this example. |

Setup rules:

| Rule | Why It Matters |
| --- | --- |
| Put all room regions inside the dungeon boundary. | Dungeon exits, resets, and fake-death returns depend on detecting the main boundary. |
| Register the spawn region before adding rooms. | Rooms are rejected until the dungeon has a spawn region. |
| Run `/dr setspawn` while standing inside the spawn region. | The plugin validates the admin location before saving the dungeon return point. |
| Add rooms in the order players should clear them. | Room sequence controls progression. |
| Keep room regions from overlapping when possible. | Overlapping rooms can make entry detection ambiguous. |

Border visualization commands:

```text
/dr showborder        Shows the room region you are currently inside.
/dr showborder spawn  Shows the spawn region for the dungeon you are currently inside.
/dr showborder all    Shows every dungeon boundary and room region.
```

## Progression Logic

Progression is scoped per dungeon.

| Rule | Behavior |
| --- | --- |
| First room | Sequence `0` is freely accessible. |
| Later rooms | Require completion of the previous sequence in the same dungeon. |
| Completion | Current kills in that room reach `requiredKills`. |
| Unlocks | Completed rooms stay unlocked until reset. |
| Exit | Exiting is never blocked. |
| Teleports | Same-world teleports are checked before entering locked rooms. |
| Bypass | `dungeonrooms.bypass` or scoped bypass permission allows entry. |

Example flow:

```text
Spawn -> Room 1 -> Room 2 -> Boss Room
```

## Spawn Logic

When `death-override.enabled` is true, fatal damage inside a registered dungeon is cancelled before vanilla death handling. The player gets a death title, 5 seconds of blindness by default, a chat death log, optional item/XP penalties, optional console commands, and is teleported to that dungeon's stored spawn point.

If no spawn point is set, or the spawn location is invalid (not in spawn region, wrong world), DungeonRooms falls back to vanilla death behavior and logs a console warning.

`/dr setspawn <dungeonName>` only succeeds when the admin is standing inside that dungeon's registered spawn region.

## Reset Triggers

| Trigger | Config Key | Default |
| --- | --- | --- |
| Player fake-dies inside dungeon | `progress-reset.death` | `false` |
| Player exits dungeon boundary | `progress-reset.dungeon-exit` | `true` |
| Player changes world from a dungeon world | `progress-reset.world-change` | `true` |
| Player teleports out of a dungeon | `progress-reset.teleport` | `true` |

Reset messages are only sent when the reset actually happens.

## Border Visualizer

`/dr showborder` renders the current room border only.

`/dr showborder spawn` renders the spawn region border for the dungeon the player is currently in.

`/dr showborder all` renders all registered dungeon boundaries and room borders simultaneously.

Only the toggling player sees their particles because DungeonRooms uses `player.spawnParticle()`.

| Border Type | Config Particle |
| --- | --- |
| Room regions | `border-visualizer.room-particle-type` |
| Dungeon boundaries | `border-visualizer.dungeon-particle-type` |
| Spawn regions | `border-visualizer.spawn-particle-type` |

Each enabled player has one repeating render task. Tasks are cancelled on toggle off, logout, world change, and plugin disable.

## World Tracking

DungeonRooms keeps a cached set of worlds containing any registered dungeon, spawn region, or room.

| Event | Fast Return Rule |
| --- | --- |
| `PlayerMoveEvent` | Returns immediately if the player's current world is not cached. |
| `EntityDeathEvent` | Returns immediately if the killer's world is not cached. |
| `PlayerChangedWorldEvent` | Returns immediately if the origin world is not cached. |

Non-dungeon worlds are ignored.

## Building From Source

Prerequisites:

| Tool | Version |
| --- | --- |
| JDK | 21 |
| Gradle | 8.x+ |

Build:

```bash
./gradlew shadowJar
```

Output:

```text
build/libs/DungeonRooms-2.3.1.jar
```

Paper, WorldGuard, WorldEdit, and MythicMobs are `compileOnly`. Gson is provided by Paper and is not shaded, so no external libraries are bundled.

## License

MIT

Made with ❤️ by evnrca
