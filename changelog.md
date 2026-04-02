# Changelog - NotTheServersFault

### [1.1.1-BETA]
- **Size Optimization**: Removed the Adventure platform library. The plugin now uses native Bukkit/Spigot APIs for Titles, ActionBars, and BossBars. This has reduced the jar size from **1.1MB to ~70KB**.
- **Ping & Packet Optimization**: 
    - Fixed an issue where the client was getting flooded with packets in the lobby, causing high ping and input blocking.
    - Reduced the frequency of the "Interstellar" lobby music packets.
    - Optimized the lobby UI task to use native Title packets which are more efficient.
- **UI Visibility Fixes**:
    - Titles and Subtitles now display reliably on all 1.21.x platforms (Spigot, Paper, Purpur).
    - Fixed ActionBar message not appearing in the lobby.
- **Chat & Social**:
    - **Unblocked Chat**: Players can now chat freely while in the lobby.
    - **Join Notifications**: Added a chat notification when players join: `Waiting for Players (X/X)`.
- **Fake Player Fixes**:
    - Ensured ghost players appear in the Tab list without requiring external libraries.
    - Removed Adventure dependency from packet logic.
- **World Management**:
    - Fixed a `uid.dat` error on startup. The plugin now cleans the contents of `world`, `world_nether`, and `world_the_end` while keeping the root folders to satisfy the server's file system checks.
- **General Stability**: Fixed several NullPointerExceptions and missing imports.
