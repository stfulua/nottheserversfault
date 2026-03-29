package xyz.vprolabs.nottheserversfault;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.vprolabs.nottheserversfault.command.StartCommand;
import xyz.vprolabs.nottheserversfault.listener.*;
import xyz.vprolabs.nottheserversfault.manager.*;

import java.util.Optional;

public final class NotTheServersFault extends JavaPlugin {

    private TwistManager twistManager;
    private LobbyManager lobbyManager;
    private GraceManager graceManager;
    private HerobrineManager herobrineManager;
    private InventoryShuffleManager inventoryShuffleManager;
    private GoalManager goalManager;
    private StructureDisappearManager structureDisappearManager;
    private AmbienceManager ambienceManager;
    private AdminManager adminManager;
    private FakePlayerManager fakePlayerManager;
    private WorldResetManager worldResetManager;

    @Override
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("packetevents") != null) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().load();
        }
    }

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("packetevents") == null) {
            getLogger().severe("====================================================");
            getLogger().severe("            CRITICAL ERROR: MISSING DEPENDENCY      ");
            getLogger().severe("====================================================");
            getLogger().severe(" NotTheServersFault requires 'PacketEvents' to run.");
            getLogger().severe(" Please download and install it here:");
            getLogger().severe(" https://modrinth.com/plugin/packetevents");
            getLogger().severe("====================================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PacketEvents.getAPI().init();

        this.twistManager = new TwistManager(this);
        this.twistManager.load();

        this.lobbyManager = new LobbyManager(this);
        this.graceManager = new GraceManager(this, twistManager);
        this.herobrineManager = new HerobrineManager(this, twistManager);
        this.inventoryShuffleManager = new InventoryShuffleManager(this, twistManager);
        this.goalManager = new GoalManager(this, twistManager);
        this.structureDisappearManager = new StructureDisappearManager(this, twistManager);
        this.ambienceManager = new AmbienceManager(this, twistManager);
        this.adminManager = new AdminManager(this);
        this.fakePlayerManager = new FakePlayerManager(this, twistManager);
        this.worldResetManager = new WorldResetManager(this, twistManager);

        Optional.ofNullable(getCommand("start")).ifPresentOrElse(
                cmd -> cmd.setExecutor(new StartCommand(lobbyManager, graceManager, twistManager)),
                () -> getLogger().severe("Command /start is missing from plugin.yml")
        );

        Optional.ofNullable(getCommand("ntsf")).ifPresentOrElse(
                cmd -> cmd.setExecutor(new xyz.vprolabs.nottheserversfault.command.AdminCommand(this, adminManager)),
                () -> getLogger().severe("Command /ntsf is missing from plugin.yml")
        );

        var pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this, lobbyManager, twistManager), this);
        pm.registerEvents(new PlayerStateListener(this, lobbyManager, twistManager), this);
        pm.registerEvents(new ChatBlockListener(lobbyManager), this);
        pm.registerEvents(new DiamondSwapListener(twistManager), this);
        pm.registerEvents(new ChestLootListener(twistManager), this);
        pm.registerEvents(new MobSpawnListener(twistManager, this), this);
        pm.registerEvents(new SunImmunityListener(twistManager, this), this);
        pm.registerEvents(new CursedCraftingListener(twistManager), this);
        pm.registerEvents(new BlockDesyncListener(this, twistManager), this);
        pm.registerEvents(goalManager, this);
        pm.registerEvents(structureDisappearManager, this);
        pm.registerEvents(ambienceManager, this);
        pm.registerEvents(adminManager, this);

        // Resume if active
        if (twistManager.isActive() && !twistManager.isFinished()) {
            inventoryShuffleManager.start();
            herobrineManager.start();
            goalManager.start();
            structureDisappearManager.start();
            ambienceManager.start();
            fakePlayerManager.start();
        } else if (twistManager.isStarted() && !twistManager.isActive() && !twistManager.isFinished()) {
            graceManager.resumeCountdown();
        }

        getLogger().info("NotTheServersFault core has been enabled.");

        // Pre-generate chunks on server start (delayed to ensure Chunky is ready)
        getServer().getScheduler().runTaskLater(this, () -> {
            if (getServer().getPluginManager().getPlugin("Chunky") != null) {
                String worldName = getServer().getWorlds().get(0).getName();
                getServer().dispatchCommand(getServer().getConsoleSender(), "chunky world " + worldName);
                getServer().dispatchCommand(getServer().getConsoleSender(), "chunky center 0 0");
                getServer().dispatchCommand(getServer().getConsoleSender(), "chunky radius 200");
                getServer().dispatchCommand(getServer().getConsoleSender(), "chunky start");
                getLogger().info("Successfully started chunk pre-generation via Chunky.");
            } else {
                getLogger().warning("Chunky is not installed! Chunk pre-generation skipped.");
            }
        }, 40L); // 2 second delay
    }

    @Override
    public void onDisable() {
        Optional.ofNullable(graceManager).ifPresent(GraceManager::stop);
        Optional.ofNullable(inventoryShuffleManager).ifPresent(InventoryShuffleManager::stop);
        Optional.ofNullable(herobrineManager).ifPresent(HerobrineManager::stop);
        Optional.ofNullable(lobbyManager).ifPresent(LobbyManager::shutdown);
        Optional.ofNullable(goalManager).ifPresent(GoalManager::stop);
        Optional.ofNullable(structureDisappearManager).ifPresent(StructureDisappearManager::stop);
        Optional.ofNullable(ambienceManager).ifPresent(AmbienceManager::stop);
        Optional.ofNullable(fakePlayerManager).ifPresent(FakePlayerManager::stop);

        if (getServer().getPluginManager().getPlugin("packetevents") != null) {
            PacketEvents.getAPI().terminate();
        }
        getLogger().info("NotTheServersFault core has been disabled.");
    }

    public TwistManager getTwistManager() { return twistManager; }
    public LobbyManager getLobbyManager() { return lobbyManager; }
    public GraceManager getGraceManager() { return graceManager; }
    public HerobrineManager getHerobrineManager() { return herobrineManager; }
    public InventoryShuffleManager getInventoryShuffleManager() { return inventoryShuffleManager; }
    public GoalManager getGoalManager() { return goalManager; }
    public StructureDisappearManager getStructureDisappearManager() { return structureDisappearManager; }
    public AmbienceManager getAmbienceManager() { return ambienceManager; }
    public FakePlayerManager getFakePlayerManager() { return fakePlayerManager; }
    public WorldResetManager getWorldResetManager() { return worldResetManager; }
}
