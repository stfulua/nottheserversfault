package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

import java.io.File;
import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class TwistManager {
    private final NotTheServersFault plugin;
    private volatile boolean active;
    private volatile boolean started;
    private volatile boolean finished;
    private volatile long startTime;
    private volatile int deathCount;
    private final File stateFile;

    private List<String> excludedPlayers;

    public TwistManager(NotTheServersFault plugin) {
        this.plugin = plugin;
        this.stateFile = new File(plugin.getDataFolder(), "state.yml");
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.excludedPlayers = plugin.getConfig().getStringList("settings.excluded-players").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public List<String> getExcludedPlayers() {
        return excludedPlayers;
    }

    public boolean isExcluded(String name) {
        return excludedPlayers.contains(name.toLowerCase());
    }

    public void activate() { 
        this.active = true; 
        save();
    }
    
    public void deactivate() { 
        this.active = false; 
        save();
    }
    
    public void setStarted(boolean started) {
        this.started = started;
        if (started) this.startTime = System.currentTimeMillis();
        save();
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
        save();
    }

    public void incrementDeaths() {
        this.deathCount++;
        save();
    }
    
    public boolean isActive() { return active; }
    public boolean isStarted() { return started; }
    public boolean isFinished() { return finished; }
    public long getStartTime() { return startTime; }
    public int getDeathCount() { return deathCount; }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("active", active);
        config.set("started", started);
        config.set("finished", finished);
        config.set("startTime", startTime);
        config.set("deathCount", deathCount);
        try {
            config.save(stateFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save state.yml: " + e.getMessage());
        }
    }

    public void load() {
        loadConfig();
        if (!stateFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(stateFile);
        this.active = config.getBoolean("active", false);
        this.started = config.getBoolean("started", false);
        this.finished = config.getBoolean("finished", false);
        this.startTime = config.getLong("startTime", 0);
        this.deathCount = config.getInt("deathCount", 0);
    }
}
