package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InventoryShuffleManager {

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private org.bukkit.scheduler.BukkitTask task;

    public InventoryShuffleManager(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void start() {
        if (task != null) return;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!twistManager.isActive()) return;
            plugin.getServer().getOnlinePlayers().forEach(this::shuffle);
        }, 6000L, 6000L); 
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void shuffle(Player player) {
        if (plugin.getTwistManager().isExcluded(player.getName())) return;
        
        player.sendMessage("§c§oYour inventory seems to be shifting...");
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getStorageContents();
        List<ItemStack> items = Arrays.asList(contents);
        Collections.shuffle(items);
        inv.setStorageContents(items.toArray(new ItemStack[0]));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
    }
}
