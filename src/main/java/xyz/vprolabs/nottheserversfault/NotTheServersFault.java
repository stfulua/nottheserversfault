package xyz.vprolabs.nottheserversfault;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.vprolabs.nottheserversfault.command.StartCommand;
import xyz.vprolabs.nottheserversfault.command.AdminCommand;
import xyz.vprolabs.nottheserversfault.listener.*;
import xyz.vprolabs.nottheserversfault.manager.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class NotTheServersFault extends JavaPlugin {

    private TwistManager twistManager;
    private LobbyManager lobbyManager;
    private GraceManager graceManager;
    private InventoryShuffleManager inventoryShuffleManager;
    private GoalManager goalManager;
    private StructureDisappearManager structureDisappearManager;
    private AmbienceManager ambienceManager;
    private FakePlayerManager fakePlayerManager;
    private ChunkyManager chunkyManager;

    @Override
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("packetevents") != null) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().load();
        }

        // Delete world folders contents on startup synchronously to avoid race conditions with world loading
        deleteWorldFolders();
    }

    private void deleteWorldFolders() {
        String[] worlds = {"world", "world_nether", "world_the_end"};
        File container = getServer().getWorldContainer();
        
        getLogger().info("Aggressively deleting world folders completely to ensure fresh generation...");
        for (String worldName : worlds) {
            File worldDir = new File(container, worldName);
            if (worldDir.exists()) {
                deleteFileRecursively(worldDir);
                
                // Recreate necessary directories that the server might have already initialized
                // before the onLoad() phase, preventing NoSuchFileException and FileNotFoundException.
                worldDir.mkdirs();
                if (worldName.equals("world")) {
                    new File(worldDir, "playerdata").mkdirs();
                    new File(worldDir, "stats").mkdirs();
                    new File(worldDir, "advancements").mkdirs();
                }
                
                getLogger().info("Deleted and recreated structure for world folder: " + worldName);
            }
        }
        getLogger().info("World folders completely reset successfully.");
    }

    private void deleteFileRecursively(File file) {
        try (Stream<Path> stream = Files.walk(file.toPath())) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            // Log warning but don't crash
        }
    }

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("packetevents") == null) {
            getLogger().severe("NotTheServersFault requires 'PacketEvents' to run.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PacketEvents.getAPI().init();

        this.twistManager = new TwistManager(this);
        this.twistManager.load();

        this.chunkyManager = new ChunkyManager(this);
        this.lobbyManager = new LobbyManager(this);
        this.graceManager = new GraceManager(this, twistManager);
        this.inventoryShuffleManager = new InventoryShuffleManager(this, twistManager);
        this.goalManager = new GoalManager(this, twistManager);
        this.structureDisappearManager = new StructureDisappearManager(this, twistManager);
        this.ambienceManager = new AmbienceManager(this, twistManager);
        this.fakePlayerManager = new FakePlayerManager(this, twistManager);

        StartCommand startCmd = new StartCommand(lobbyManager, graceManager, twistManager);
        Optional.ofNullable(getCommand("start")).ifPresent(cmd -> {
            cmd.setExecutor(startCmd);
            cmd.setTabCompleter(startCmd);
        });

        AdminCommand adminCmd = new AdminCommand(this);
        Optional.ofNullable(getCommand("ntsf")).ifPresent(cmd -> {
            cmd.setExecutor(adminCmd);
            cmd.setTabCompleter(adminCmd);
        });

        org.bukkit.plugin.PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this, lobbyManager, twistManager), this);
        pm.registerEvents(new PlayerStateListener(this, lobbyManager, twistManager), this);
        pm.registerEvents(new ChatBlockListener(lobbyManager), this);
        pm.registerEvents(new DiamondSwapListener(twistManager, this), this);
        pm.registerEvents(new ChestLootListener(twistManager), this);
        pm.registerEvents(new MobSpawnListener(twistManager, this), this);
        pm.registerEvents(new SunImmunityListener(twistManager, this), this);
        pm.registerEvents(new CursedCraftingListener(twistManager), this);
        pm.registerEvents(new BlockDesyncListener(this, twistManager), this);
        pm.registerEvents(goalManager, this);
        pm.registerEvents(structureDisappearManager, this);
        pm.registerEvents(ambienceManager, this);
        pm.registerEvents(lobbyManager, this);

        if (twistManager.isActive() && !twistManager.isFinished()) {
            inventoryShuffleManager.start();
            goalManager.start();
            structureDisappearManager.start();
            ambienceManager.start();
            fakePlayerManager.start();
        } else if (twistManager.isStarted() && !twistManager.isActive() && !twistManager.isFinished()) {
            graceManager.resumeCountdown();
        }

        disableLocatorBarIfVersionMatch();
        getLogger().info("NotTheServersFault core has been enabled.");

        getServer().getScheduler().runTaskLater(this, () -> {
            if (getServer().getPluginManager().getPlugin("Chunky") != null) {
                String worldName = getServer().getWorlds().get(0).getName();
                org.bukkit.command.ConsoleCommandSender console = getServer().getConsoleSender();
                getServer().dispatchCommand(console, "chunky silent");
                getServer().dispatchCommand(console, "chunky world " + worldName);
                getServer().dispatchCommand(console, "chunky center 0 0");
                getServer().dispatchCommand(console, "chunky radius 200");
                getServer().dispatchCommand(console, "chunky start");
            }
        }, 40L);
    }

    private void disableLocatorBarIfVersionMatch() {
        String version = getServer().getBukkitVersion();
        if (version.contains("1.21")) {
            for (World world : getServer().getWorlds()) {
                try {
                    GameRule<?> locatorBarRule = GameRule.getByName("locator_bar");
                    if (locatorBarRule != null && locatorBarRule.getType() == Boolean.class) {
                        @SuppressWarnings("unchecked")
                        GameRule<Boolean> boolRule = (GameRule<Boolean>) locatorBarRule;
                        Boolean currentValue = world.getGameRuleValue(boolRule);
                        if (currentValue != null && currentValue) {
                            world.setGameRule(boolRule, false);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void onDisable() {
        if (graceManager != null) graceManager.stop();
        if (inventoryShuffleManager != null) inventoryShuffleManager.stop();
        if (lobbyManager != null) lobbyManager.shutdown();
        if (goalManager != null) goalManager.stop();
        if (structureDisappearManager != null) structureDisappearManager.stop();
        if (ambienceManager != null) ambienceManager.stop();
        if (fakePlayerManager != null) fakePlayerManager.stop();

        if (getServer().getPluginManager().getPlugin("packetevents") != null) {
            PacketEvents.getAPI().terminate();
        }
        getLogger().info("NotTheServersFault core has been disabled.");
    }

    public TwistManager getTwistManager() { return twistManager; }
    public LobbyManager getLobbyManager() { return lobbyManager; }
    public GraceManager getGraceManager() { return graceManager; }
    public InventoryShuffleManager getInventoryShuffleManager() { return inventoryShuffleManager; }
    public GoalManager getGoalManager() { return goalManager; }
    public StructureDisappearManager getStructureDisappearManager() { return structureDisappearManager; }
    public AmbienceManager getAmbienceManager() { return ambienceManager; }
    public FakePlayerManager getFakePlayerManager() { return fakePlayerManager; }
    public ChunkyManager getChunkyManager() { return chunkyManager; }
}
