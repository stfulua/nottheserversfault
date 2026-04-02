# Changelog - NotTheServersFault

### [1.1.1-BETA]
- **ASYNC World Management**: Moved the startup world cleaning process to an asynchronous background thread. This prevents the server from hanging or lagging during its initial loading phase.
- **Lobby Optimization**: 
    - Separated physics calculations from UI/Sound updates. 
    - UI and music packets are now sent every 20 ticks (1 second) instead of every tick, massively reducing network overhead and fixing the "blocked packets" feeling.
- **BossBar Fix**: Re-implemented the 15-second Goal BossBar using the native Bukkit API. It now correctly displays "Your goal: Get a diamond block" after the grace period starts.
- **Chunky API Fix**: Improved the reflection-based hook for Chunky to properly target the API interface, resolving startup warnings and ensuring progress tracking works as intended.
- **Player Data Reliability**: Ensures standard Minecraft subdirectories (`playerdata`, `stats`, etc.) are recreated after cleaning to prevent player save failures.
- **Minimal Jar Size**: Maintained the optimized **~70KB** footprint by using native APIs exclusively.
