### [1.1.0] - The Polished Chaos Update
- **Secret Diamond Ore**: Randomly selects an ore (Gold, Redstone, Lapis, Emerald, Iron, or Coal) with equal chance on server start to drop diamonds.
- **Mob Ambush System**: Mining the Secret Ore up close triggers scary sounds and a mob ambush (Zombies, Skeletons, Witches).
- **Lobby Security**: Added full invincibility and disabled PvP in the lobby area.
- **Admin Commands**: Added `/ntsf spectator <add|remove> <player>` to manage the spectator list and `/ntsf reload` to reload the config.
- **Fake Player Polish**: Full sequence (Join -> Message -> Leave) with a 3-8 minute cooldown.
- **Improved Spawning**: Ambush mobs now spawn within ±10 Y-levels to ensure they appear in the same cave system.
- **Dynamic Terrain Detection**: The lobby now properly detects when Chunky is generating terrain and blocks game start.
- **Aggressive World Wipe**: On every world reset (startup), the plugin now entirely deletes the `world`, `world_nether`, and `world_the_end` folders (including `uid.dat`, `level.dat`, etc.) to guarantee a fully fresh generation with a new random seed natively selected by the server. **(Fixed: Recreates base directory structures immediately to prevent server file-saving errors).**
- **Automatic Server Reset**: The server now automatically shuts down/resets after a configurable delay once the challenge is complete.
- **Challenge Timer**: Displays the total time taken to beat the challenge in chat and titles upon winning.
- **Bug Fixes**: Prioritized Secret Ore drops, removed broken loading percentage, fixed command autocompletion, and resolved `FileNotFoundException`/`NoSuchFileException` during world regeneration.
- **Quality of Life**: Operators (OPs) can now change their GameMode freely.

### [1.0.1] - Secret Ore & Ambush (Initial)
- Added Secret Diamond Ore mechanic.
- Added scary ambience sounds and mob ambush when mining Secret Ore.
- Updated versioning for 1.0.1 release.

### [1.0.0] - Initial Release
- **Block Desync**: Blocks may instantly "revert" after being broken.
- **Cursed Crafting**: 10% chance for crafts to fail and give "trash".
- **Daylight Spawns**: Hostile mobs spawn during the day and are immune to sunlight.
- **Ghost Players**: Fake players join, leave and send messages.
- **Inventory Shuffle**: Inventories randomly shuffle every 5 minutes.
- **Vanishing Structures**: Villages/structures disappear when approached.
- **Sun Immunity**: Monsters are permanently protected from fire/sunlight.
- **Lobby System**: High-altitude spawn at Y=6700 with Interstellar music.
- **Goal System**: First player to obtain a Diamond Block wins.
