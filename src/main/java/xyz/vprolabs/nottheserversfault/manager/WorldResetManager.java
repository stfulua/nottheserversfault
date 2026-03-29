package xyz.vprolabs.nottheserversfault.manager;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class WorldResetManager {

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitTask resetTask;

    public WorldResetManager(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void startResetTimer() {
        if (!twistManager.isResetEnabled() || resetTask != null) return;

        long delayTicks = twistManager.getResetDelayMinutes() * 60L * 20L;
        
        plugin.getLogger().info("World reset timer started: " + twistManager.getResetDelayMinutes() + " minutes.");

        resetTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (shouldResetNow()) {
                executeReset();
            } else {
                resetTask = null;
            }
        }, delayTicks);
    }

    public void cancelResetTimer() {
        if (resetTask != null) {
            resetTask.cancel();
            resetTask = null;
            plugin.getLogger().info("World reset timer cancelled - players returned.");
        }
    }

    private boolean shouldResetNow() {
        if (twistManager.isFinished()) return true;
        
        // If game started but not finished, check if any targets are online
        if (twistManager.isStarted()) {
            return Bukkit.getOnlinePlayers().stream().noneMatch(TargetUtil::isTarget);
        }
        
        return false;
    }

    public void executeReset() {
        plugin.getLogger().severe("EXECUTING WORLD RESET...");
        
        // Broadcast warning
        Bukkit.getOnlinePlayers().forEach(p -> p.kick(miniMessage.deserialize("<red><bold>WORLD RESETTING...\n<white>The world is being deleted and regenerated.")));

        // Schedule shutdown and deletion
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                // Delete state file to start fresh
                File stateFile = new File(plugin.getDataFolder(), "state.yml");
                if (stateFile.exists()) stateFile.delete();

                // Get world folder
                World world = Bukkit.getWorlds().get(0);
                File worldFolder = world.getWorldFolder();
                
                plugin.getLogger().info("Deleting world folder: " + worldFolder.getName());
                
                // We can't delete the world while it's loaded, so we use a trick:
                // We'll mark it for deletion or tell the console.
                // However, most server hosts restart automatically.
                
                // Better approach: Deleting region folder is often enough to reset terrain.
                File regionFolder = new File(worldFolder, "region");
                deleteDirectory(regionFolder);
                
                // Also delete player data
                deleteDirectory(new File(worldFolder, "playerdata"));
                deleteDirectory(new File(worldFolder, "stats"));
                deleteDirectory(new File(worldFolder, "advancements"));
                
                plugin.getLogger().severe("World data cleared. Restarting server...");
                Bukkit.shutdown();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to fully reset world: " + e.getMessage());
                Bukkit.shutdown();
            }
        }, 40L);
    }

    private void deleteDirectory(File file) {
        if (!file.exists()) return;
        try (var walk = Files.walk(file.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not delete " + file.getName() + ": " + e.getMessage());
        }
    }
}
