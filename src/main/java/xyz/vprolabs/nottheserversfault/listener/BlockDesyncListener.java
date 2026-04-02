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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BlockDesyncListener implements Listener {

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private final Map<UUID, Integer> activeStreaks = new HashMap<>();
    
    private static final EnumSet<Material> DESYNC_MATERIALS = EnumSet.of(
        Material.STONE, Material.COBBLESTONE, Material.DIRT, Material.GRASS_BLOCK,
        Material.OAK_LOG, Material.OAK_PLANKS, Material.IRON_ORE, Material.COAL_ORE,
        Material.DEEPSLATE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_COAL_ORE
    );

    public BlockDesyncListener(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!twistManager.isActive() || !TargetUtil.isTarget(event.getPlayer())) return;
        
        Material type = event.getBlock().getType();
        if (!DESYNC_MATERIALS.contains(type)) return;

        UUID uuid = event.getPlayer().getUniqueId();
        boolean shouldDesync = false;

        if (activeStreaks.containsKey(uuid)) {
            shouldDesync = true;
            int remaining = activeStreaks.get(uuid);
            if (remaining <= 1) {
                activeStreaks.remove(uuid);
            } else {
                activeStreaks.put(uuid, remaining - 1);
            }
        } else {
            // 12% base chance to trigger a desync
            if (ThreadLocalRandom.current().nextInt(100) < 12) {
                shouldDesync = true;
                // 40% chance to start a streak if we just triggered a normal desync
                if (ThreadLocalRandom.current().nextInt(100) < 40) {
                    activeStreaks.put(uuid, ThreadLocalRandom.current().nextInt(2, 6)); // 2 to 5 extra desyncs
                }
            }
        }

        if (shouldDesync) {
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
