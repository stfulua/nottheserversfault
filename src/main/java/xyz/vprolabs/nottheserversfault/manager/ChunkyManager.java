package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Bukkit;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

import java.lang.reflect.Method;

/**
 * Manages integration with the Chunky pre-generation plugin via confirms logs.
 */
public final class ChunkyManager {

    private final NotTheServersFault plugin;
    private Object chunkyApi;
    private Method isRunningMethod;

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
                    // Confirmed method from user logs
                    try {
                        this.isRunningMethod = apiClass.getMethod("isRunning", String.class);
                    } catch (NoSuchMethodException e) {
                        this.isRunningMethod = apiClass.getMethod("isRunning");
                    }
                    
                    if (this.isRunningMethod != null) {
                        plugin.getLogger().info("Successfully hooked into Chunky API (Method: isRunning)");
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not hook into Chunky API: " + e.getMessage());
        }
    }

    public boolean isChunkyRunning() {
        if (chunkyApi == null || isRunningMethod == null) return false;
        try {
            if (isRunningMethod.getParameterCount() == 1) {
                String worldName = Bukkit.getWorlds().get(0).getName();
                return (boolean) isRunningMethod.invoke(chunkyApi, worldName);
            } else {
                return (boolean) isRunningMethod.invoke(chunkyApi);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public String getProgressString() {
        // Current API version confirmed by logs does not expose progress percentage directly
        // through the main interface. Showing "IN PROGRESS" instead of "null".
        if (!isChunkyRunning()) return null;
        return "IN PROGRESS";
    }
}
