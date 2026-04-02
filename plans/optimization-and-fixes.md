# Optimization and Bug Fix Plan - 1.1.1-BETA

## 1. Async System Implementation
- **World Management**: The `deleteWorldFolders` logic in `NotTheServersFault.java` currently runs on the main thread during `onLoad`. I will move the folder traversal and deletion to a background thread using `Bukkit.getScheduler().runTaskAsynchronously` (or a native Thread in `onLoad`).
- **File I/O**: Ensure all heavy disk operations are decoupled from the game tick to prevent TPS drops during startup.

## 2. UI & Goal Fixes
- **BossBar**: Re-add the `BossBar` to `GoalManager.showGoalReminder()` using the native Bukkit API.
- **Lobby UI**: 
    - Update `LobbyManager` to separate "Physics/Teleport" (every tick) from "UI Updates" (every 20 ticks).
    - This will significantly reduce the number of packets sent per player, fixing the "blocked packets" and "high ping" feel.

## 3. Chunky Integration Fix
- **Reflection Update**: Fix the method lookup in `ChunkyManager.java` to correctly target the `ChunkyAPI` interface methods, resolving the `NoSuchMethod` or `IllegalAccess` warnings.

## 4. Size & Performance
- **Dependency Review**: Maintain the current ~70KB size by keeping the Adventure platform out and sticking to native APIs.
- **Async Safety**: Audit all managers to ensure they use thread-safe maps where necessary.

## 5. Documentation
- Update `changelog.md` to reflect these optimizations.
