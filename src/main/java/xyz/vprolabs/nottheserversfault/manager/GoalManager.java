package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
    private boolean finished = false;

    private static final Material GOAL_ITEM = Material.DIAMOND_BLOCK;
    private static final String GOAL_TEXT = "Get a diamond block";

    public GoalManager(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void showGoalReminder() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (finished) return;
            plugin.getServer().getOnlinePlayers().forEach(p -> p.sendTitle("§c§lGuess the twists!", "", 10, 40, 10));
        }, 20L);

        // Native BossBar for 15 seconds
        BossBar bar = Bukkit.createBossBar("§6Your goal: §f" + GOAL_TEXT, BarColor.BLUE, BarStyle.SOLID);
        Bukkit.getOnlinePlayers().forEach(bar::addPlayer);

        new BukkitRunnable() {
            int ticks = 300; // 15s

            @Override
            public void run() {
                if (ticks <= 0 || finished) {
                    bar.removeAll();
                    this.cancel();
                    return;
                }
                
                bar.setProgress(Math.max(0.0, Math.min(1.0, (double) ticks / 300.0)));
                ticks--;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void start() {
        finished = false;
    }

    public void stop() {}

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
    public void onJoin(PlayerJoinEvent event) {}

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
        
        long timeTakenMs = System.currentTimeMillis() - twistManager.getStartTime();
        String formattedTime = formatTime(timeTakenMs);
        
        String title = "§a§lCHALLENGE COMPLETE!";
        String subtitle = "§e" + player.getName() + " §fwon in §b" + formattedTime;
        
        Bukkit.broadcastMessage("§a§lCHALLENGE COMPLETE! §e" + player.getName() + " §fhas won the challenge in §b" + formattedTime + "§f!");
        
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.sendTitle(title, subtitle, 10, 70, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        });
        
        plugin.getInventoryShuffleManager().stop();
        plugin.getFakePlayerManager().stop();

        // Handle Automatic Reset
        if (twistManager.isResetEnabled()) {
            int delayMinutes = twistManager.getResetDelayMinutes();
            Bukkit.broadcastMessage("§c§lThe server will automatically reset in " + delayMinutes + " minutes.");
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.broadcastMessage("§c§lAUTOMATIC RESET STARTING...");
                Bukkit.getOnlinePlayers().forEach(p -> p.kickPlayer("§cServer is resetting for a new game!\n§7Please wait a moment."));
                Bukkit.getScheduler().runTaskLater(plugin, Bukkit::shutdown, 10L);
            }, delayMinutes * 1200L);
        }
    }

    private String formatTime(long ms) {
        long seconds = (ms / 1000) % 60;
        long minutes = (ms / (1000 * 60)) % 60;
        long hours = (ms / (1000 * 60 * 60));
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
