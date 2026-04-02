# Changelog - NotTheServersFault

### [1.1.1-beta]
- **Startup World Management**: Implemented automatic deletion of `world`, `world_nether`, and `world_the_end` on startup (if total size < 500MB) to ensure a clean state for every run.
- **Lobby UI Overhaul**: Fixed issues with Titles and ActionBars not displaying correctly in the lobby.
- **Start Sequence Polish**: Added a 5-second countdown with chat clearing and a "Good Luck." message.
- **Fake Player Stability**: Improved Tab list visibility and added leave messages for ghost players to prevent list clutter.
- **Universal Support**: Confirmed support for Bukkit, Spigot, Paper, and Purpur on 1.21.x.
- **Optimized Footprint**: Reduced jar size significantly through aggressive dependency management.
