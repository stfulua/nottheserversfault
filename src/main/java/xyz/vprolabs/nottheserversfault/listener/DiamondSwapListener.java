package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.concurrent.ThreadLocalRandom;

public class DiamondSwapListener implements Listener {

    private final TwistManager twistManager;

    public DiamondSwapListener(TwistManager twistManager) {
        this.twistManager = twistManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!twistManager.isActive() || !TargetUtil.isTarget(event.getPlayer())) return;

        Block block = event.getBlock();
        Material type = block.getType();

        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
            event.setDropItems(false);
            event.setExpToDrop(0);
            dropItem(block, Material.DIRT, 1);
        } else if (type == Material.LAPIS_ORE || type == Material.DEEPSLATE_LAPIS_ORE) {
            event.setDropItems(false);
            event.setExpToDrop(0);
            dropItem(block, Material.DIAMOND, ThreadLocalRandom.current().nextInt(4, 9));
        }
    }

    private void dropItem(Block block, Material material, int amount) {
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        block.getWorld().dropItemNaturally(loc, new ItemStack(material, amount));
    }
}
