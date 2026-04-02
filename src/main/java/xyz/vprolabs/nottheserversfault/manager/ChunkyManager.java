package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Bukkit;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                    Class<?> actualClass = chunkyApi.getClass();
                    
                    // List all methods for debugging if needed
                    String methods = Stream.of(actualClass.getMethods())
                            .map(Method::getName)
                            .collect(Collectors.joining(", "));
                    
                    // Search for getTasks or tasks method (case-insensitive and partial match)
                    this.getTasksMethod = findFlexibleMethod(actualClass, "getTasks", "tasks", "currentTasks");
                    
                    if (this.getTasksMethod != null) {
                        plugin.getLogger().info("Successfully hooked into Chunky API (Method: " + getTasksMethod.getName() + ")");
                    } else {
                        plugin.getLogger().warning("Could not find Task retrieval method in ChunkyAPI. Available methods: " + methods);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not hook into Chunky API: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private Method findFlexibleMethod(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                return clazz.getMethod(name);
            } catch (NoSuchMethodException ignored) {}
        }
        // Try to find anything containing "task"
        for (Method m : clazz.getMethods()) {
            if (m.getName().toLowerCase().contains("task") && m.getParameterCount() == 0) {
                return m;
            }
        }
        return null;
    }

    public boolean isChunkyRunning() {
        if (chunkyApi == null || getTasksMethod == null) return false;
        try {
            Object result = getTasksMethod.invoke(chunkyApi);
            if (result instanceof Map) {
                return !((Map<?, ?>) result).isEmpty();
            }
            // If it's a single task or collection
            if (result != null) return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public double getOverallProgress() {
        if (chunkyApi == null || getTasksMethod == null) return 100.0;
        try {
            Object result = getTasksMethod.invoke(chunkyApi);
            if (result == null) return 100.0;
            
            if (result instanceof Map) {
                Map<?, ?> tasks = (Map<?, ?>) result;
                if (tasks.isEmpty()) return 100.0;
                
                double totalPercent = 0;
                int count = 0;
                for (Object task : tasks.values()) {
                    totalPercent += getTaskProgress(task);
                    count++;
                }
                return count == 0 ? 100.0 : totalPercent / count;
            } else {
                // Single task or other structure
                return getTaskProgress(result);
            }
        } catch (Exception e) {
            return 100.0;
        }
    }

    private double getTaskProgress(Object task) {
        if (task == null) return 100.0;
        try {
            if (getProgressMethod == null) {
                getProgressMethod = findFlexibleMethod(task.getClass(), "getPercentComplete", "getProgress", "progress", "percent");
            }
            if (getProgressMethod != null) {
                Object res = getProgressMethod.invoke(task);
                if (res instanceof Number) {
                    return ((Number) res).doubleValue();
                }
            }
        } catch (Exception ignored) {}
        return 100.0;
    }

    public String getProgressString() {
        if (!isChunkyRunning()) return null;
        return String.format("%.1f%%", getOverallProgress());
    }
}
