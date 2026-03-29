package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

public class BlockDesyncListener implements Listener {

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    
    private static final EnumSet<Material> DESYNC_MATERIALS = EnumSet.of(
        Material.STONE, Material.COBBLESTONE, Material.DIRT, Material.GRASS_BLOCK,
        Material.OAK_LOG, Material.OAK_PLANKS, Material.IRON_ORE, Material.COAL_ORE,
        Material.DEEPSLATE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_COAL_ORE
    );

    public BlockDesyncListener(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!twistManager.isActive() || !TargetUtil.isTarget(event.getPlayer())) return;
        
        Material type = event.getBlock().getType();
        if (!DESYNC_MATERIALS.contains(type)) return;

        // 10% chance to fake break (desync)
        if (ThreadLocalRandom.current().nextInt(10) == 0) {
            event.setDropItems(false);
            event.setExpToDrop(0);
            
            Block block = event.getBlock();
            BlockData data = block.getBlockData().clone();
            
            // Revert the block after a short delay to simulate desync
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                block.setBlockData(data, false);
            }, 5L); // 0.25s delay
        }
    }
}
