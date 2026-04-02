package xyz.vprolabs.nottheserversfault;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.vprolabs.nottheserversfault.command.StartCommand;
import xyz.vprolabs.nottheserversfault.listener.*;
import xyz.vprolabs.nottheserversfault.manager.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public final class NotTheServersFault extends JavaPlugin {

    private BukkitAudiences audiences;
    private TwistManager twistManager;
    private LobbyManager lobbyManager;
    private GraceManager graceManager;
    private InventoryShuffleManager inventoryShuffleManager;
    private GoalManager goalManager;
    private StructureDisappearManager structureDisappearManager;
    private AmbienceManager ambienceManager;
    private AdminManager adminManager;
    private FakePlayerManager fakePlayerManager;
    private ChunkyManager chunkyManager;

    @Override
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("packetevents") != null) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().load();
        }
        deleteWorldFolders();
    }

    private void deleteWorldFolders() {
        String[] worlds = {"world", "world_nether", "world_the_end"};
        long totalSize = 0;
        File[] worldDirs = new File[worlds.length];
        
        for (int i = 0; i < worlds.length; i++) {
            worldDirs[i] = new File(getServer().getWorldContainer(), worlds[i]);
            if (worldDirs[i].exists()) {
                totalSize += getFolderSize(worldDirs[i]);
            }
        }

        if (totalSize > 0 && totalSize <= 500L * 1024L * 1024L) {
            getLogger().info("Found world folders (Total size: " + (totalSize / 1024 / 1024) + "MB). Deleting for fresh start...");
            for (File dir : worldDirs) {
                if (dir.exists()) {
                    deleteFolder(dir);
                }
            }
        } else if (totalSize > 500L * 1024L * 1024L) {
            getLogger().warning("World folders exceed 500MB (" + (totalSize / 1024 / 1024) + "MB). Skipping deletion for safety.");
        }
    }

    private long getFolderSize(File folder) {
        try (Stream<Path> stream = Files.walk(folder.toPath())) {
            return stream.filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        } catch (IOException e) {
            return 0;
        }
    }

    private void deleteFolder(File folder) {
        try (Stream<Path> stream = Files.walk(folder.toPath())) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            getLogger().warning("Could not delete folder " + folder.getName() + ": " + e.getMessage());
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
        this.audiences = BukkitAudiences.create(this);

        this.twistManager = new TwistManager(this);
        this.twistManager.load();

        this.chunkyManager = new ChunkyManager(this);
        this.lobbyManager = new LobbyManager(this);
        this.graceManager = new GraceManager(this, twistManager);
        this.inventoryShuffleManager = new InventoryShuffleManager(this, twistManager);
        this.goalManager = new GoalManager(this, twistManager);
        this.structureDisappearManager = new StructureDisappearManager(this, twistManager);
        this.ambienceManager = new AmbienceManager(this, twistManager);
        this.adminManager = new AdminManager(this);
        this.fakePlayerManager = new FakePlayerManager(this, twistManager);

        Optional.ofNullable(getCommand("start")).ifPresent(cmd -> cmd.setExecutor(new StartCommand(lobbyManager, graceManager, twistManager)));
        Optional.ofNullable(getCommand("ntsf")).ifPresent(cmd -> cmd.setExecutor(new xyz.vprolabs.nottheserversfault.command.AdminCommand(this, adminManager)));

        var pm = getServer().getPluginManager();
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
        pm.registerEvents(adminManager, this);

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
                getServer().dispatchCommand(getServer().getConsoleSender(), "chunky world " + worldName);
                getServer().dispatchCommand(getServer().getConsoleSender(), "chunky center 0 0");
                getServer().dispatchCommand(getServer().getConsoleSender(), "chunky radius 200");
                getServer().dispatchCommand(getServer().getConsoleSender(), "chunky start");
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
        
        if (this.audiences != null) {
            this.audiences.close();
            this.audiences = null;
        }
        getLogger().info("NotTheServersFault core has been disabled.");
    }

    public BukkitAudiences getAudiences() { return audiences; }
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
