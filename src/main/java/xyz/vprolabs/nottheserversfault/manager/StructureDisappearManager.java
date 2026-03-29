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

    private static final EnumSet<Material> BUILDING_MATERIALS = EnumSet.noneOf(Material.class);

    static {
        for (Material mat : Material.values()) {
            String name = mat.name();
            // Wood types
            if (name.contains("LOG") || name.contains("WOOD") || name.contains("PLANKS") || 
                name.contains("STAIRS") || name.contains("SLAB") || name.contains("FENCE") || 
                name.contains("DOOR") || name.contains("TRAPDOOR") || name.contains("PRESSURE_PLATE") || 
                name.contains("BUTTON") || name.contains("SIGN") || name.contains("HANGING_SIGN")) {
                BUILDING_MATERIALS.add(mat);
            }
            // Stone/Brick types
            if (name.contains("COBBLESTONE") || name.contains("STONE_BRICK") || name.contains("BRICKS") || 
                name.contains("TERRACOTTA") || name.contains("CONCRETE") || name.contains("WOOL") || 
                name.contains("CARPET") || name.contains("GLASS") || name.contains("LANTERN") || 
                name.contains("WALL") || name.contains("CHISELED") || name.contains("POLISHED")) {
                BUILDING_MATERIALS.add(mat);
            }
            // Utility/Furniture
            if (name.equals("CHEST") || name.equals("FURNACE") || name.equals("CRAFTING_TABLE") || 
                name.equals("TORCH") || name.equals("WALL_TORCH") || name.equals("BELL") || 
                name.equals("BARREL") || name.equals("SMOKER") || name.equals("BLAST_FURNACE") || 
                name.contains("TABLE") || name.contains("GRINDSTONE") || name.equals("LECTERN") || 
                name.equals("STONECUTTER") || name.equals("LOOM") || name.equals("COMPOSTER") || 
                name.equals("CAULDRON") || name.equals("BREWING_STAND") || name.equals("HAY_BLOCK") || 
                name.equals("BOOKSHELF") || name.equals("BED") || name.contains("CANDLE")) {
                BUILDING_MATERIALS.add(mat);
            }
        }
        // Exclude specific terrain materials that might be caught by broad filters
        BUILDING_MATERIALS.remove(Material.STONE);
        BUILDING_MATERIALS.remove(Material.DIRT);
        BUILDING_MATERIALS.remove(Material.GRASS_BLOCK);
        BUILDING_MATERIALS.remove(Material.SAND);
        BUILDING_MATERIALS.remove(Material.GRAVEL);
        BUILDING_MATERIALS.remove(Material.WATER);
        BUILDING_MATERIALS.remove(Material.LAVA);
    }

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
