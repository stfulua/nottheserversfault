package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Bukkit;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Manages integration with the Chunky pre-generation plugin via robust reflection.
 */
public final class ChunkyManager {

    private final NotTheServersFault plugin;
    private Object chunkyApi;
    private Method getTasksMethod;
    private Method getProgressMethod;

    public ChunkyManager(NotTheServersFault plugin) {
        this.plugin = plugin;
        setupHook();
    }

    private void setupHook() {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("Chunky") != null) {
                Class<?> apiClass = Class.forName("org.popcraft.chunky.api.ChunkyAPI");
                this.chunkyApi = Bukkit.getServicesManager().load(apiClass);
                if (this.chunkyApi != null) {
                    // Search for getTasks or tasks method
                    this.getTasksMethod = findMethod(apiClass, "getTasks", "tasks");
                    if (this.getTasksMethod != null) {
                        plugin.getLogger().info("Successfully hooked into Chunky API.");
                    } else {
                        plugin.getLogger().warning("Could not find getTasks() or tasks() method in ChunkyAPI.");
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not hook into Chunky API: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private Method findMethod(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                return clazz.getMethod(name);
            } catch (NoSuchMethodException ignored) {}
        }
        return null;
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
                    // Try common progress method names
                    getProgressMethod = findMethod(task.getClass(), "getPercentComplete", "getProgress", "progress");
                }
                if (getProgressMethod != null) {
                    Object result = getProgressMethod.invoke(task);
                    if (result instanceof Double) {
                        totalPercent += (Double) result;
                    } else if (result instanceof Float) {
                        totalPercent += ((Float) result).doubleValue();
                    }
                    count++;
                }
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
