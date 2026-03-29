package xyz.vprolabs.nottheserversfault.manager;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InventoryShuffleManager {

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitTask shuffleTask;

    private static final long SHUFFLE_INTERVAL_TICKS = 20L * 60 * 5;

    public InventoryShuffleManager(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void start() {
        if (shuffleTask != null) return;

        shuffleTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!twistManager.isActive()) return;

            TargetUtil.findTarget(plugin.getServer()).ifPresent(this::shuffleInventory);
        }, SHUFFLE_INTERVAL_TICKS, SHUFFLE_INTERVAL_TICKS);
    }

    public void stop() {
        if (shuffleTask != null) {
            shuffleTask.cancel();
            shuffleTask = null;
        }
    }

    public void shuffleManual(Player player) {
        shuffleInventory(player);
    }

    private void shuffleInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getStorageContents();
        List<ItemStack> items = Arrays.asList(contents);
        Collections.shuffle(items);
        inv.setStorageContents(items.toArray(new ItemStack[0]));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
    }
}
