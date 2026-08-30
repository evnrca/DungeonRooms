# DungeonRooms

[![Author](https://img.shields.io/badge/author-evnrca-0ea5e9?style=flat-square)](https://github.com/evnrca)
[![Version](https://img.shields.io/badge/version-2.0.0-22c55e?style=flat-square)](https://github.com/evnrca/DungeonRooms)

DungeonRooms lets server owners build guided dungeon runs where players unlock each room by defeating the required MythicMobs. Create dungeon areas with WorldGuard, set a safe spawn point, choose how many mobs each room requires, and let the plugin handle room locks, progress tracking, respawns, admin bypasses, and optional particle borders.

Author: [evnrca](https://github.com/evnrca)

Repository: https://github.com/evnrca/DungeonRooms

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
| Stored respawn points | Set death respawns with `/dr setspawn <dungeonName>`. |
| Per-dungeon progression | Room sequence is scoped to each dungeon. |
| MythicMobs kill tracking | Only MythicMobs kills count toward room completion. |
| JSON player data | Player kills and room unlocks are stored in a flat Gson JSON file. |
| Lightweight storage | Gson is provided by Paper, so no database library is bundled. |
| Atomic async saves | Runtime progress writes are async and use a temporary file before replacing `playerdata.json`. |
| Reset toggles | Death, dungeon exit, world change, and teleport resets can be enabled independently. |
| Teleport pass-through protection | Ender pearls, chorus fruit, and other teleports cannot bypass locked room entry. |
| Border visualization | Show the current room border or all dungeon and room borders privately. |
| Admin bypass | Bypass all rooms or specific dungeon rooms with permissions. |

## Installation

1. Download `DungeonRooms-2.0.0.jar`.
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
| `/dr setspawn <dungeonName>` | `dungeonrooms.admin` | OP | Store your current location as the dungeon respawn point. |
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
# DungeonRooms v2.0.0 Configuration
denial:
  # Valid values: CANCEL, VELOCITY, TELEPORT, KNOCKBACK.
  action: CANCEL
  velocity:
    # Horizontal strength used by VELOCITY and KNOCKBACK.
    horizontal: 1.5
    # Upward strength used by VELOCITY and KNOCKBACK.
    vertical: 0.4
  # Title shown when locked-room entry is denied.
  title: '&b&lʀᴏᴏᴍ ʟᴏᴄᴋᴇᴅ!'
  # Subtitle shown when locked-room entry is denied. Supports {remaining}.
  subtitle: '&bᴋɪʟʟ &3{remaining} &bᴍᴏʀᴇ ᴍᴏʙs ᴛᴏ ᴘʀᴏᴄᴇᴇᴅ.'
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
    format: '&bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ'
  chat:
    # Show progress in chat after MythicMob kills.
    enabled: true
    # Supports {current} and {required}.
    format: '&8[&bᴅᴜɴɢᴇᴏɴs&8] &bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ'
    # Seconds between chat progress messages per player.
    cooldown: 5

border-visualizer:
  # Master toggle for /dr showborder and /dr showborder all.
  enabled: true
  # Particle used for room borders.
  room-particle-type: FLAME
  # Particle used for dungeon boundary borders.
  dungeon-particle-type: END_ROD
  # Distance between particles along border edges. Lower is denser.
  particle-density: 0.5
  # Render interval in ticks.
  interval-ticks: 20
  messages:
    # Sent when current-room border visualization is enabled.
    toggled-on: '&bʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ &3ᴇɴᴀʙʟᴇᴅ.'
    # Sent when current-room border visualization is disabled.
    toggled-off: '&bʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ &3ᴅɪsᴀʙʟᴇᴅ.'
    # Sent when /dr showborder is used outside a registered room.
    not-in-region: '&cʏᴏᴜ ᴀʀᴇ ɴᴏᴛ ɪɴsɪᴅᴇ ᴀɴʏ ʀᴇɢɪsᴛᴇʀᴇᴅ ᴅᴜɴɢᴇᴏɴ ʀᴏᴏᴍ.'
    # Sent when border visualization is disabled globally.
    feature-disabled: '&cʙᴏʀᴅᴇʀ ᴠɪsᴜᴀʟɪᴢᴀᴛɪᴏɴ ɪs ᴅɪsᴀʙʟᴇᴅ ʙʏ ᴛʜᴇ sᴇʀᴠᴇʀ.'

messages:
  # Editable prefix for plugin chat messages.
  prefix: '&8[&bᴅᴜɴɢᴇᴏɴʀᴏᴏᴍs&8] '
  # Sent when a player tries to enter a locked room. Supports {remaining} and {region}.
  requirement-not-met: '&cʏᴏᴜ ɴᴇᴇᴅ &4{remaining} &cᴍᴏʀᴇ ᴍᴏʙ ᴋɪʟʟs ᴛᴏ ᴇɴᴛᴇʀ &4{region}&c!'
  # Generic progress message. Supports {current} and {required}.
  progress: '&bᴘʀᴏɢʀᴇss: &3{current}/{required} &bᴍᴏʙs ᴋɪʟʟᴇᴅ'
  # Sent when a room is completed. Supports {region}.
  completed: '&6ʀᴏᴏᴍ &e{region} &6ᴄᴏᴍᴘʟᴇᴛᴇᴅ! &eʏᴏᴜ ᴍᴀʏ ɴᴏᴡ ᴘʀᴏᴄᴇᴇᴅ.'
  # Sent when progress resets due to death.
  progress-reset-death: '&cʏᴏᴜ ᴅɪᴇᴅ! &4ʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ.'
  # Sent when progress resets due to logout.
  progress-reset-logout: '&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ʟᴏɢᴏᴜᴛ).'
  # Sent when progress resets due to teleport.
  progress-reset-teleport: '&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ᴛᴇʟᴇᴘᴏʀᴛ).'
  # Sent when progress resets due to world exit/change.
  progress-reset-world-exit: '&cʏᴏᴜʀ ᴅᴜɴɢᴇᴏɴ ᴘʀᴏɢʀᴇss ʜᴀs ʙᴇᴇɴ ʀᴇsᴇᴛ &4(ʟᴇꜰᴛ ᴡᴏʀʟᴅ).'
  # Sent when a world cannot be found. Supports {world}.
  world-not-found: '&cᴡᴏʀʟᴅ &4{world} &cᴅᴏᴇs ɴᴏᴛ ᴇxɪsᴛ.'
  # Sent when a WorldGuard region cannot be found. Supports {region} and {world}.
  region-not-found: '&cʀᴇɢɪᴏɴ &4{region} &cᴅᴏᴇs ɴᴏᴛ ᴇxɪsᴛ ɪɴ ᴡᴏʀʟᴅ &4{world}&c.'
  dungeon-already-exists: '&cᴅᴜɴɢᴇᴏɴ &4{dungeon} &cɪs ᴀʟʀᴇᴀᴅʏ ʀᴇɢɪsᴛᴇʀᴇᴅ.'
  dungeon-not-found: '&cᴅᴜɴɢᴇᴏɴ &4{dungeon} &cɪs ɴᴏᴛ ʀᴇɢɪsᴛᴇʀᴇᴅ.'
  dungeon-created: '&bᴅᴜɴɢᴇᴏɴ &3{dungeon} &bᴄʀᴇᴀᴛᴇᴅ.'
  room-added: '&bʀᴏᴏᴍ &3{region} &bᴀᴅᴅᴇᴅ ᴛᴏ ᴅᴜɴɢᴇᴏɴ &3{dungeon}&b.'
  room-removed: '&bʀᴏᴏᴍ &3{region} &bʀᴇᴍᴏᴠᴇᴅ ꜰʀᴏᴍ ᴅᴜɴɢᴇᴏɴ &3{dungeon}&b.'
  room-not-found: '&cʀᴏᴏᴍ &4{region} &cɴᴏᴛ ꜰᴏᴜɴᴅ ɪɴ ᴅᴜɴɢᴇᴏɴ &4{dungeon}&c.'
  room-no-spawn: '&cʀᴇɢɪsᴛᴇʀ ᴀ sᴘᴀᴡɴ ʀᴇɢɪᴏɴ ꜰᴏʀ ᴅᴜɴɢᴇᴏɴ &4{dungeon} &cʙᴇꜰᴏʀᴇ ᴀᴅᴅɪɴɢ ʀᴏᴏᴍs.'
  spawn-set: '&bsᴘᴀᴡɴ ꜰᴏʀ ᴅᴜɴɢᴇᴏɴ &3{dungeon} &bsᴇᴛ ᴀᴛ ʏᴏᴜʀ ʟᴏᴄᴀᴛɪᴏɴ.'
  spawn-not-in-region: '&cʏᴏᴜ ᴍᴜsᴛ ʙᴇ ɪɴsɪᴅᴇ ᴛʜᴇ ʀᴇɢɪsᴛᴇʀᴇᴅ sᴘᴀᴡɴ ʀᴇɢɪᴏɴ ᴛᴏ sᴇᴛ sᴘᴀᴡɴ.'
  spawn-region-added: '&bsᴘᴀᴡɴ ʀᴇɢɪᴏɴ &3{region} &bʀᴇɢɪsᴛᴇʀᴇᴅ ꜰᴏʀ ᴅᴜɴɢᴇᴏɴ &3{dungeon}&b.'
  kills-updated: '&bʀᴇQᴜɪʀᴇᴅ ᴋɪʟʟs ꜰᴏʀ &3{region} &bᴜᴘᴅᴀᴛᴇᴅ ᴛᴏ &3{kills}&b.'
  version: '&bᴅᴜɴɢᴇᴏɴʀᴏᴏᴍs &3v{version} &b| &3ʙʏ evnrca'
```

## JSON Storage

DungeonRooms stores player progress in `plugins/DungeonRooms/playerdata.json`.

No external database is used. Gson comes from Paper, so DungeonRooms does not shade SQLite, H2, Gson, or any other storage library.

Structure:

```text
Map<UUID, Map<dungeonName, Map<region, int[]>>>
```

Array values:

| Index | Meaning |
| --- | --- |
| `int[0]` | Kill count |
| `int[1]` | Unlock state, `0` locked or `1` unlocked |

All player data loads synchronously during plugin enable into the in-memory progress cache. Runtime kill and unlock changes save asynchronously through the Bukkit scheduler. Plugin shutdown writes synchronously to avoid data loss.

Saves are atomic: DungeonRooms writes `playerdata.json.tmp` first, then replaces `playerdata.json`.

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
| Spawn region | `sample_spawn` | `/dr add spawn dungeon_world sample_spawn sample` | Area where admins must stand to set the dungeon respawn point. |
| Room region | `room1` | `/dr add room sample room1 10` | First room, freely accessible. |
| Room region | `room2` | `/dr add room sample room2 15` | Requires completion of `room1`. |
| Boss room | `boss_room` | `/dr add room sample boss_room 1` | Final room in this example. |

Setup rules:

| Rule | Why It Matters |
| --- | --- |
| Put all room regions inside the dungeon boundary. | Dungeon exits, resets, and respawns depend on detecting the main boundary. |
| Register the spawn region before adding rooms. | Rooms are rejected until the dungeon has a spawn region. |
| Run `/dr setspawn` while standing inside the spawn region. | The plugin validates the admin location before saving the respawn point. |
| Add rooms in the order players should clear them. | Room sequence controls progression. |
| Keep room regions from overlapping when possible. | Overlapping rooms can make entry detection ambiguous. |

Border visualization commands:

```text
/dr showborder      Shows the room region you are currently inside.
/dr showborder all  Shows every dungeon boundary and room region.
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

Players who die inside a registered dungeon respawn at that dungeon's stored spawn point.

If no spawn point is set, DungeonRooms falls back to vanilla respawn behavior and logs a console warning.

`/dr setspawn <dungeonName>` only succeeds when the admin is standing inside that dungeon's registered spawn region.

## Reset Triggers

| Trigger | Config Key | Default |
| --- | --- | --- |
| Player dies inside dungeon | `progress-reset.death` | `false` |
| Player exits dungeon boundary | `progress-reset.dungeon-exit` | `true` |
| Player changes world from a dungeon world | `progress-reset.world-change` | `true` |
| Player teleports out of a dungeon | `progress-reset.teleport` | `true` |

Reset messages are only sent when the reset actually happens.

## Border Visualizer

`/dr showborder` renders the current room border only.

`/dr showborder all` renders all registered dungeon boundaries and room borders simultaneously.

Only the toggling player sees their particles because DungeonRooms uses `player.spawnParticle()`.

| Border Type | Config Particle |
| --- | --- |
| Room regions | `border-visualizer.room-particle-type` |
| Dungeon boundaries | `border-visualizer.dungeon-particle-type` |

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
build/libs/DungeonRooms-2.0.0.jar
```

Paper, WorldGuard, WorldEdit, and MythicMobs are `compileOnly`. Gson is provided by Paper and is not shaded, so no external libraries are bundled.

## License

MIT

Made with ❤️ by evnrca
