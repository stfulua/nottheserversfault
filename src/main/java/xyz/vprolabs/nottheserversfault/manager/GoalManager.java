package xyz.vprolabs.nottheserversfault.manager;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

public class GoalManager implements Listener {

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private boolean finished = false;

    private static final Material GOAL_ITEM = Material.DIAMOND_BLOCK;
    private static final String GOAL_TEXT = "Get a diamond block";

    public GoalManager(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void showGoalReminder() {
        // Show "Guess the twists!" title after a short delay
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (finished) return;
            Component guessTitle = miniMessage.deserialize("<gradient:red:gold><bold>Guess the twists!");
            Title title = Title.title(guessTitle, Component.empty());
            plugin.getServer().getOnlinePlayers().forEach(p -> p.showTitle(title));
        }, 20L);

        // Show 15s Timer BossBar
        Component barTitle = miniMessage.deserialize("<gold>Your goal: <white>" + GOAL_TEXT);
        BossBar timerBar = BossBar.bossBar(barTitle, 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        
        plugin.getServer().getOnlinePlayers().forEach(p -> p.showBossBar(timerBar));

        new BukkitRunnable() {
            int ticks = 300; // 15 seconds * 20 ticks

            @Override
            public void run() {
                if (ticks <= 0 || finished) {
                    plugin.getServer().getOnlinePlayers().forEach(p -> p.hideBossBar(timerBar));
                    this.cancel();
                    return;
                }

                timerBar.progress((float) ticks / 300.0f);
                ticks--;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void start() {
        finished = false;
    }

    public void stop() {
        // Nothing to do here
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            checkGoal(player, event.getItem().getItemStack().getType());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && event.getCurrentItem() != null) {
            checkGoal(player, event.getCurrentItem().getType());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // BossBar is handled by LobbyManager's sendToLobby before start
    }

    private void checkGoal(Player player, Material material) {
        if (!twistManager.isActive() || finished) return;
        if (material == GOAL_ITEM) {
            completeChallenge(player);
        }
    }

    private void completeChallenge(Player player) {
        finished = true;
        twistManager.deactivate();
        twistManager.setFinished(true);
        
        Component title = miniMessage.deserialize("<green><bold>CHALLENGE COMPLETE!");
        Component subtitle = miniMessage.deserialize("<white><yellow>" + player.getName() + " <white>got the Diamond Block!");
        
        plugin.getServer().broadcast(miniMessage.deserialize("<green><bold>CHALLENGE COMPLETE! <yellow>" + player.getName() + " <white>has won the challenge!"));
        
        plugin.getServer().getOnlinePlayers().forEach(p -> {
            p.showTitle(Title.title(title, subtitle));
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        });
        
        plugin.getHerobrineManager().stop();
        plugin.getInventoryShuffleManager().stop();
        plugin.getFakePlayerManager().stop();

        // Start reset timer after goal finished
        plugin.getWorldResetManager().startResetTimer();
    }
}
