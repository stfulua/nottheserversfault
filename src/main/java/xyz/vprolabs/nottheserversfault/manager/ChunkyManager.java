package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Bukkit;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Manages integration with the Chunky pre-generation plugin via reflection.
 * This avoids direct dependency issues if the API classes are not exactly matching.
 */
public final class ChunkyManager {

    private final NotTheServersFault plugin;
    private Object chunkyApi;
    private Method getTasksMethod;
    private Method getProgressMethod;

    public ChunkyManager(NotTheServersFault plugin) {
        this.plugin = plugin;
        try {
            if (plugin.getServer().getPluginManager().getPlugin("Chunky") != null) {
                this.chunkyApi = Bukkit.getServicesManager().load(Class.forName("org.popcraft.chunky.api.ChunkyAPI"));
                if (this.chunkyApi != null) {
                    this.getTasksMethod = chunkyApi.getClass().getMethod("getTasks");
                    plugin.getLogger().info("Successfully hooked into Chunky API via reflection.");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not hook into Chunky API: " + e.getMessage());
        }
    }

    public boolean isChunkyRunning() {
        if (chunkyApi == null || getTasksMethod == null) return false;
        try {
            Map<?, ?> tasks = (Map<?, ?>) getTasksMethod.invoke(chunkyApi);
            return tasks != null && !tasks.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public double getOverallProgress() {
        if (chunkyApi == null || getTasksMethod == null) return 100.0;
        try {
            Map<?, ?> tasks = (Map<?, ?>) getTasksMethod.invoke(chunkyApi);
            if (tasks == null || tasks.isEmpty()) return 100.0;
            
            double totalPercent = 0;
            int count = 0;
            
            for (Object task : tasks.values()) {
                if (getProgressMethod == null) {
                    getProgressMethod = task.getClass().getMethod("getPercentComplete");
                }
                totalPercent += (double) getProgressMethod.invoke(task);
                count++;
            }
            
            if (count == 0) return 100.0;
            return totalPercent / count;
        } catch (Exception e) {
            return 100.0;
        }
    }

    public String getProgressString() {
        if (!isChunkyRunning()) return null;
        return String.format("%.1f%%", getOverallProgress());
    }
}
