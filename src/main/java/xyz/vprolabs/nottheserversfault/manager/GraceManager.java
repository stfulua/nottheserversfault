package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

public final class GraceManager {

    private static final long GRACE_DURATION_MS = 2L * 60L * 1000L; 

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private BukkitTask countdownTask;

    public GraceManager(@NotNull NotTheServersFault plugin, @NotNull TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void startCountdown() {
        if (countdownTask != null) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (int i = 0; i < 100; i++) {
                    player.sendMessage("");
                }
            }
            Bukkit.broadcastMessage("§cGood Luck.");
            plugin.getGoalManager().showGoalReminder();
        }, 100L); 

        scheduleActivation(GRACE_DURATION_MS);
    }

    public void resumeCountdown() {
        if (countdownTask != null || twistManager.isActive() || !twistManager.isStarted()) return;

        long elapsed = System.currentTimeMillis() - twistManager.getStartTime();
        long remaining = GRACE_DURATION_MS - elapsed;

        if (remaining <= 0) {
            activateTwists();
        } else {
            scheduleActivation(remaining);
        }
    }

    private void scheduleActivation(long delayMs) {
        long ticks = delayMs / 50; 
        countdownTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            countdownTask = null;
            activateTwists();
        }, ticks);
    }

    private void activateTwists() {
        if (twistManager.isActive() || twistManager.isFinished()) return;

        twistManager.activate();
        plugin.getInventoryShuffleManager().start();
        plugin.getGoalManager().start();
        plugin.getStructureDisappearManager().start();
        plugin.getAmbienceManager().start();
        plugin.getFakePlayerManager().start();
    }

    public void stop() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }
}
