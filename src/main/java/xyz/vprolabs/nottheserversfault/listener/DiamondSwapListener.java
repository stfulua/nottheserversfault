package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.concurrent.ThreadLocalRandom;

public class DiamondSwapListener implements Listener {

    private final TwistManager twistManager;
    private final Plugin plugin;

    public DiamondSwapListener(TwistManager twistManager, Plugin plugin) {
        this.twistManager = twistManager;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!twistManager.isActive() || !TargetUtil.isTarget(event.getPlayer())) return;

        Block block = event.getBlock();
        Material type = block.getType();
        int y = block.getY();

        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
            event.setDropItems(false);
            event.setExpToDrop(0);
            dropItem(block, Material.DIRT, 1);
        } else if (type == Material.LAPIS_ORE || type == Material.DEEPSLATE_LAPIS_ORE) {
            // Only swap Lapis for Diamonds if broken at Y 10 or below
            if (y <= 10) {
                event.setDropItems(false);
                event.setExpToDrop(0);
                dropItem(block, Material.DIAMOND, ThreadLocalRandom.current().nextInt(4, 9));
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // Only process newly generated chunks to prevent Lapis from appearing above Y 10
        if (!event.isNewChunk()) return;

        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        // Scan the chunk for Lapis Ore above Y 10 and replace it
        // We do this in a task to avoid blocking the main thread during generation if possible,
        // although ChunkLoadEvent is already on the main thread.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // Lapis usually doesn't generate above Y 64 in modern versions, but we scan up to be safe
                    for (int y = world.getMaxHeight() - 1; y > 10; y--) {
                        Block block = chunk.getBlock(x, y, z);
                        Material type = block.getType();
                        if (type == Material.LAPIS_ORE || type == Material.DEEPSLATE_LAPIS_ORE) {
                            block.setType(y > 0 ? Material.STONE : Material.DEEPSLATE, false);
                        }
                    }
                }
            }
        });
    }

    private void dropItem(Block block, Material material, int amount) {
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        block.getWorld().dropItemNaturally(loc, new ItemStack(material, amount));
    }
}
