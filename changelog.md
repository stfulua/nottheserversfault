# Changelog - NotTheServersFault

### [1.0.0] - Official Release
- **World Reset on Start**: Added logic to automatically set time to day, clear weather, and reset player health/hunger/effects when the challenge officially starts.
- **Player Data Fix**: Fixed a critical bug where player data would fail to save because the `playerdata` folder was missing after world cleaning. Standard subdirectories are now reliably recreated.
- **Massive Size Reduction**: Removed the Adventure platform library completely in favor of native Bukkit/Spigot APIs. Final jar size is now **~65KB** (down from 1.1MB).
- **Ping & Packet Optimization**: 
    - Resolved lobby "packet flooding" that caused high ping and input lag.
    - Optimized UI and music loops to be significantly more network-efficient.
- **UI & Visibility**:
    - Re-implemented Titles, ActionBars, and BossBars using native Spigot methods for 100% reliability on 1.21.x.
    - Added explicit title timing to ensure visibility across all server forks.
- **Social & Chat**:
    - Unblocked chat in the lobby.
    - Added join notifications: `Waiting for Players (X/X)`.
- **Ghost Players**: Improved PacketEvents logic to ensure fake players appear in the Tab list without external dependencies.
- **Startup World Cleaning**: Implemented safe, async cleaning of world folders on startup to ensure a fresh environment for every round.
- **Chunky Integration**: Finalized a robust reflection hook for Chunky to track terrain generation without startup warnings.
