package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Bukkit;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

import java.lang.reflect.Method;

/**
 * Manages integration with the Chunky pre-generation plugin via its API.
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
                    try {
                        this.isRunningMethod = apiClass.getMethod("isRunning", String.class);
                        plugin.getLogger().info("Hooked: ChunkyAPI#isRunning(String)");
                    } catch (NoSuchMethodException e) {
                        plugin.getLogger().warning("Could not find isRunning(String) in Chunky API.");
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error hooking Chunky: " + e.getMessage());
        }
    }

    public boolean isChunkyRunning() {
        if (chunkyApi == null || isRunningMethod == null) return false;
        try {
            String worldName = Bukkit.getWorlds().get(0).getName();
            return (boolean) isRunningMethod.invoke(chunkyApi, worldName);
        } catch (Exception e) {
            return false;
        }
    }
}
