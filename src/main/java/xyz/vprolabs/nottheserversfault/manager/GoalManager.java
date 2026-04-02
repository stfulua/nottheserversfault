package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Bukkit;
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

        new BukkitRunnable() {
            int ticks = 300; 

            @Override
            public void run() {
                if (ticks <= 0 || finished) {
                    this.cancel();
                    return;
                }
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
        
        String title = "§a§lCHALLENGE COMPLETE!";
        String subtitle = "§e" + player.getName() + " §fgot the Diamond Block!";
        
        Bukkit.broadcastMessage("§a§lCHALLENGE COMPLETE! §e" + player.getName() + " §fhas won the challenge!");
        
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.sendTitle(title, subtitle, 10, 70, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        });
        
        plugin.getInventoryShuffleManager().stop();
        plugin.getFakePlayerManager().stop();
    }
}
