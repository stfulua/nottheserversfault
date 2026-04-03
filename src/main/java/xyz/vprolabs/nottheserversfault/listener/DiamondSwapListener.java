package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class DiamondSwapListener implements Listener {

    private final TwistManager twistManager;
    private final Plugin plugin;
    
    private Material secretOreNormal;
    private Material secretOreDeepslate;
    private final Set<Location> ambushedBlocks = new HashSet<>();

    public DiamondSwapListener(TwistManager twistManager, Plugin plugin) {
        this.twistManager = twistManager;
        this.plugin = plugin;
        selectSecretOre();
    }

    private void selectSecretOre() {
        int random = ThreadLocalRandom.current().nextInt(6);
        switch (random) {
            case 0:
                secretOreNormal = Material.GOLD_ORE;
                secretOreDeepslate = Material.DEEPSLATE_GOLD_ORE;
                break;
            case 1:
                secretOreNormal = Material.REDSTONE_ORE;
                secretOreDeepslate = Material.DEEPSLATE_REDSTONE_ORE;
                break;
            case 2:
                secretOreNormal = Material.LAPIS_ORE;
                secretOreDeepslate = Material.DEEPSLATE_LAPIS_ORE;
                break;
            case 3:
                secretOreNormal = Material.EMERALD_ORE;
                secretOreDeepslate = Material.DEEPSLATE_EMERALD_ORE;
                break;
            case 4:
                secretOreNormal = Material.IRON_ORE;
                secretOreDeepslate = Material.DEEPSLATE_IRON_ORE;
                break;
            case 5:
                secretOreNormal = Material.COAL_ORE;
                secretOreDeepslate = Material.DEEPSLATE_COAL_ORE;
                break;
        }
        plugin.getLogger().info("[QUIET] Secret Diamond Ore selected: " + secretOreNormal.name());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (!twistManager.isActive() || !TargetUtil.isTarget(event.getPlayer())) return;
        
        Block block = event.getBlock();
        Material type = block.getType();
        
        if (type == secretOreNormal || type == secretOreDeepslate) {
            Player player = event.getPlayer();
            // Trigger check: mining close to the ore
            if (player.getLocation().distanceSquared(block.getLocation()) <= 16.0) {
                triggerAmbush(player, block);
            }
        }
    }

    private void triggerAmbush(Player player, Block block) {
        if (ambushedBlocks.contains(block.getLocation())) return;
        ambushedBlocks.add(block.getLocation());

        NotTheServersFault mainPlugin = (NotTheServersFault) plugin;
        mainPlugin.getAmbienceManager().playScarySound(player);

        Location spawnLoc = findSafeSpawnLocation(player.getLocation());
        // ENSURE it spawns by providing a fallback to player location
        if (spawnLoc == null) {
            spawnLoc = player.getLocation();
        }
        spawnArmy(spawnLoc, player);
    }

    private Location findSafeSpawnLocation(Location playerLoc) {
        double minDistanceSq = Double.MAX_VALUE;
        Location bestLoc = null;

        // Search in a 10x10 area around the player, ±5 blocks Y difference
        int searchRadius = 8;
        int yDiffLimit = 5;
        
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -yDiffLimit; y <= yDiffLimit; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    Location loc = playerLoc.clone().add(x, y, z);
                    double distSq = loc.distanceSquared(playerLoc);
                    
                    // Min 4 blocks away to avoid spawning inside player
                    if (distSq >= 16 && distSq <= searchRadius * searchRadius) {
                        if (isSafe(loc)) {
                            if (distSq < minDistanceSq) {
                                minDistanceSq = distSq;
                                bestLoc = loc;
                            }
                        }
                    }
                }
            }
        }
        return bestLoc;
    }

    private boolean isSafe(Location loc) {
        Block block = loc.getBlock();
        Block above = loc.clone().add(0, 1, 0).getBlock();
        Block below = loc.clone().subtract(0, 1, 0).getBlock();
        
        // Solid ground, air at feet and head
        return block.getType().isAir() && 
               above.getType().isAir() && 
               below.getType().isSolid() && 
               below.getType() != Material.MAGMA_BLOCK && 
               below.getType() != Material.LAVA &&
               below.getType() != Material.VOID_AIR;
    }

    private void spawnArmy(Location loc, Player target) {
        World world = loc.getWorld();
        if (world == null) return;
        
        // Use a small spread to avoid all mobs spawning on the exact same pixel
        for (int i = 0; i < 12; i++) {
            Location spawn = loc.clone().add(
                ThreadLocalRandom.current().nextDouble(-1, 1), 0, ThreadLocalRandom.current().nextDouble(-1, 1)
            );
            org.bukkit.entity.Zombie zombie = world.spawn(spawn, org.bukkit.entity.Zombie.class);
            zombie.setTarget(target);
        }
        for (int i = 0; i < 4; i++) {
            Location spawn = loc.clone().add(
                ThreadLocalRandom.current().nextDouble(-1, 1), 0, ThreadLocalRandom.current().nextDouble(-1, 1)
            );
            org.bukkit.entity.Skeleton skeleton = world.spawn(spawn, org.bukkit.entity.Skeleton.class);
            skeleton.setTarget(target);
        }
        for (int i = 0; i < 2; i++) {
            Location spawn = loc.clone().add(
                ThreadLocalRandom.current().nextDouble(-1, 1), 0, ThreadLocalRandom.current().nextDouble(-1, 1)
            );
            org.bukkit.entity.Witch witch = world.spawn(spawn, org.bukkit.entity.Witch.class);
            witch.setTarget(target);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!twistManager.isActive() || !TargetUtil.isTarget(event.getPlayer())) return;

        Block block = event.getBlock();
        Material type = block.getType();
        int y = block.getY();

        if (type == secretOreNormal || type == secretOreDeepslate) {
            event.setDropItems(false);
            event.setExpToDrop(0);
            if (ThreadLocalRandom.current().nextInt(100) >= 20) {
                dropItem(block, Material.DIAMOND, 1);
            }
            return;
        }

        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
            event.setDropItems(false);
            event.setExpToDrop(0);
            dropItem(block, Material.DIRT, 1);
            return;
        }

        if (type == Material.LAPIS_ORE || type == Material.DEEPSLATE_LAPIS_ORE) {
            if (y <= 10) {
                event.setDropItems(false);
                event.setExpToDrop(0);
                dropItem(block, Material.DIAMOND, ThreadLocalRandom.current().nextInt(4, 9));
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return;

        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
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
