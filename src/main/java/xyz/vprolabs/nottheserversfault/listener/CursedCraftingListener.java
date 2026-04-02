package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;

import java.util.concurrent.ThreadLocalRandom;

public class CursedCraftingListener implements Listener {

    private final TwistManager twistManager;

    public CursedCraftingListener(TwistManager twistManager) {
        this.twistManager = twistManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!twistManager.isActive()) return;

        ItemStack result = event.getRecipe().getResult();
        if (result.getType() == Material.DIAMOND_BLOCK) return;

        if (ThreadLocalRandom.current().nextInt(10) == 0) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1.0f, 0.5f);
            
            Material[] trolls = {Material.DEAD_BUSH, Material.POISONOUS_POTATO, Material.ROTTEN_FLESH, Material.DIRT};
            Material trollMaterial = trolls[ThreadLocalRandom.current().nextInt(trolls.length)];
            
            player.getInventory().addItem(new ItemStack(trollMaterial));
            player.sendMessage("§c§oThe crafting table seems to be lagging...");
            player.closeInventory();
        }
    }
}
