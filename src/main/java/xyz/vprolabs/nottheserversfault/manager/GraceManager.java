package xyz.vprolabs.nottheserversfault.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

public final class GraceManager {

    private static final long GRACE_DURATION_MS = 2L * 60L * 1000L; // 2 Minutes
    private static final Component GOOD_LUCK = MiniMessage.miniMessage().deserialize("<red>Good Luck.");

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private BukkitTask countdownTask;

    public GraceManager(@NotNull NotTheServersFault plugin, @NotNull TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void startCountdown() {
        if (countdownTask != null) return;

        // Wait 5 seconds after start
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Clear chat for everyone
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (int i = 0; i < 100; i++) {
                    player.sendMessage("");
                }
            }
            
            // Send "Good Luck." in red
            plugin.getAudiences().all().sendMessage(GOOD_LUCK);
            
            // Show goal reminder for 15s
            plugin.getGoalManager().showGoalReminder();
        }, 100L); // 5 seconds

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
        long ticks = delayMs / 50; // 50ms per tick
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
