package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StructureDisappearManager implements Listener {

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private final Set<BoundingBox> structures = ConcurrentHashMap.newKeySet();
    private final Set<BoundingBox> removedStructures = ConcurrentHashMap.newKeySet();
    private BukkitTask checkTask;

    private static final EnumSet<Material> BUILDING_MATERIALS = EnumSet.of(
        Material.OAK_LOG, Material.STRIPPED_OAK_LOG, Material.OAK_WOOD, Material.STRIPPED_OAK_WOOD,
        Material.OAK_PLANKS, Material.OAK_STAIRS, Material.OAK_SLAB, Material.OAK_FENCE, Material.OAK_FENCE_GATE,
        Material.OAK_DOOR, Material.OAK_TRAPDOOR, Material.OAK_PRESSURE_PLATE, Material.OAK_BUTTON,
        Material.COBBLESTONE, Material.COBBLESTONE_STAIRS, Material.COBBLESTONE_SLAB, Material.COBBLESTONE_WALL,
        Material.MOSSY_COBBLESTONE, Material.MOSSY_COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_SLAB, Material.MOSSY_COBBLESTONE_WALL,
        Material.GLASS, Material.GLASS_PANE, Material.WHITE_WOOL, Material.WHITE_CARPET,
        Material.CHEST, Material.FURNACE, Material.CRAFTING_TABLE, Material.TORCH, Material.WALL_TORCH,
        Material.SHORT_GRASS, Material.FERN, Material.POPPY, Material.DANDELION, Material.BLUE_ORCHID, Material.ALLIUM,
        Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY,
        Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.WITHER_ROSE, Material.SUNFLOWER, Material.LILAC, Material.ROSE_BUSH, Material.PEONY,
        Material.TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.WHITE_TERRACOTTA, Material.BROWN_TERRACOTTA,
        Material.WHITE_STAINED_GLASS, Material.WHITE_STAINED_GLASS_PANE,
        Material.LANTERN, Material.BELL, Material.BARREL, Material.SMOKER, Material.BLAST_FURNACE, Material.CARTOGRAPHY_TABLE,
        Material.FLETCHING_TABLE, Material.GRINDSTONE, Material.LECTERN, Material.SMITHING_TABLE, Material.STONECUTTER, Material.LOOM,
        Material.COMPOSTER, Material.CAULDRON, Material.BREWING_STAND, Material.HAY_BLOCK, Material.BOOKSHELF
    );

    public StructureDisappearManager(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void start() {
        if (checkTask != null) return;
        
        // Scan already loaded chunks
        for (Chunk chunk : Bukkit.getWorlds().get(0).getLoadedChunks()) {
            scanChunk(chunk);
        }
        
        checkTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!twistManager.isActive()) return;
            
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                Location playerLoc = player.getLocation();
                for (BoundingBox box : structures) {
                    if (removedStructures.contains(box)) continue;
                    
                    if (box.clone().expand(5.0).contains(playerLoc.toVector())) {
                        removeStructureEfficiently(box);
                        removedStructures.add(box);
                    }
                }
            }
        }, 0L, 5L);
    }

    public void stop() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
        structures.clear();
        removedStructures.clear();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!twistManager.isActive()) return;
        scanChunk(event.getChunk());
    }

    private void scanChunk(Chunk chunk) {
        Collection<GeneratedStructure> chunkStructures = chunk.getStructures();
        for (GeneratedStructure structure : chunkStructures) {
            structures.add(structure.getBoundingBox());
        }
    }

    private void removeStructureEfficiently(BoundingBox box) {
        int minX = (int) box.getMinX();
        int minY = (int) box.getMinY();
        int minZ = (int) box.getMinZ();
        int maxX = (int) box.getMaxX();
        int maxY = (int) box.getMaxY();
        int maxZ = (int) box.getMaxZ();
        
        org.bukkit.World world = plugin.getServer().getWorlds().get(0);
        int blocksPerTick = 500; // Efficient batch size

        // Re-implementing with a better stateful iterator
        removeInBatches(world, minX, minY, minZ, maxX, maxY, maxZ, blocksPerTick);
    }

    private void removeInBatches(org.bukkit.World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int batchSize) {
        AtomicInteger currentX = new AtomicInteger(minX);
        AtomicInteger currentY = new AtomicInteger(minY);
        AtomicInteger currentZ = new AtomicInteger(minZ);

        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            int count = 0;
            while (count < batchSize) {
                int x = currentX.get();
                int y = currentY.get();
                int z = currentZ.get();

                Block block = world.getBlockAt(x, y, z);
                Material type = block.getType();
                
                // Only remove building materials, NEVER terrain (dirt, grass block, stone)
                if (BUILDING_MATERIALS.contains(type)) {
                    block.setType(Material.AIR, false);
                }

                count++;
                
                // Advance coordinates
                if (currentZ.incrementAndGet() > maxZ) {
                    currentZ.set(minZ);
                    if (currentY.incrementAndGet() > maxY) {
                        currentY.set(minY);
                        if (currentX.incrementAndGet() > maxX) {
                            task.cancel();
                            return;
                        }
                    }
                }
            }
        }, 0L, 1L);
    }
}
