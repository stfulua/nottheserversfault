# Changelog - NotTheServersFault

### [1.1.1-beta]
- **Player Data Fix**: Fixed a critical bug where player data would fail to save because the `playerdata` folder was missing after world cleaning. The plugin now ensures standard subdirectories (`playerdata`, `stats`, etc.) are recreated after cleaning.
- **Massive Size Reduction**: Removed the Adventure platform library completely. The plugin now uses native Bukkit/Spigot APIs for Titles, ActionBars, and BossBars. The final jar size is now **~70KB** (down from 1.1MB).
- **Ping & Packet Optimization**: 
    - Fixed a critical issue where the client was getting flooded with packets in the lobby, causing high ping and input blocking.
    - Optimized the lobby music and UI update loop to be more network-efficient.
- **UI & Visibility Fixes**:
    - Re-implemented Titles and ActionBars using native Spigot methods, ensuring they display reliably on all 1.21.x platforms.
    - Added explicit fade-in/stay/fade-out times for titles to guarantee visibility.
- **Social & Chat**:
    - **Unblocked Chat**: Players can now chat freely while in the lobby.
    - **Join Notifications**: Added a global chat notification when players join the lobby: `Waiting for Players (X/X)`.
- **Ghost Player Stability**: Improved the PacketEvents logic for fake players to ensure they appear in the Tab list without requiring heavy external libraries.
- **Startup Logic**: Optimized the world cleaning process to avoid `uid.dat` errors while still ensuring a fresh start for each challenge round.
