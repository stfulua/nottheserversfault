package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.scheduler.BukkitTask;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.manager.herobrine.HerobrineSpawnTask;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.concurrent.ThreadLocalRandom;

public class HerobrineManager {

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private BukkitTask currentTask;

    public HerobrineManager(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void start() {
        scheduleNextAppearance();
    }

    public void scheduleNextAppearance() {
        if (currentTask != null) {
            currentTask.cancel();
        }

        int delayMinutes = ThreadLocalRandom.current().nextInt(3, 9);
        long delayTicks = 20L * 60 * delayMinutes;

        currentTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!twistManager.isActive()) {
                scheduleNextAppearance();
                return;
            }

            TargetUtil.findTarget(plugin.getServer()).ifPresent(target ->
                plugin.getServer().getScheduler().runTask(plugin, new HerobrineSpawnTask(plugin, target))
            );

            scheduleNextAppearance();
        }, delayTicks);
    }

    public void stop() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }
}
