package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
            // Broad building material list
            if (name.contains("LOG") || name.contains("WOOD") || name.contains("PLANKS") || 
                name.contains("STAIRS") || name.contains("SLAB") || name.contains("FENCE") || 
                name.contains("DOOR") || name.contains("TRAPDOOR") || name.contains("PRESSURE_PLATE") || 
                name.contains("BUTTON") || name.contains("SIGN") || name.contains("HANGING_SIGN") ||
                name.contains("COBBLESTONE") || name.contains("STONE_BRICK") || name.contains("BRICKS") || 
                name.contains("TERRACOTTA") || name.contains("CONCRETE") || name.contains("WOOL") || 
                name.contains("CARPET") || name.contains("GLASS") || name.contains("LANTERN") || 
                name.contains("WALL") || name.contains("CHISELED") || name.contains("POLISHED") ||
                name.equals("CHEST") || name.equals("FURNACE") || name.equals("CRAFTING_TABLE") || 
                name.equals("TORCH") || name.equals("WALL_TORCH") || name.equals("BELL") || 
                name.equals("BARREL") || name.equals("SMOKER") || name.equals("BLAST_FURNACE") || 
                name.contains("TABLE") || name.contains("GRINDSTONE") || name.equals("LECTERN") || 
                name.equals("STONECUTTER") || name.equals("LOOM") || name.equals("COMPOSTER") || 
                name.equals("CAULDRON") || name.equals("BREWING_STAND") || name.equals("HAY_BLOCK") || 
                name.equals("BOOKSHELF") || name.equals("BED") || name.contains("CANDLE")) {
                BUILDING_MATERIALS.add(mat);
            }
        }
        BUILDING_MATERIALS.remove(Material.STONE);
        BUILDING_MATERIALS.remove(Material.DIRT);
        BUILDING_MATERIALS.remove(Material.GRASS_BLOCK);
        BUILDING_MATERIALS.remove(Material.SAND);
        BUILDING_MATERIALS.remove(Material.GRAVEL);
        BUILDING_MATERIALS.remove(Material.WATER);
        BUILDING_MATERIALS.remove(Material.LAVA);
        BUILDING_MATERIALS.remove(Material.AIR);
        BUILDING_MATERIALS.remove(Material.CAVE_AIR);
        BUILDING_MATERIALS.remove(Material.VOID_AIR);
    }

    public StructureDisappearManager(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void start() {
        if (checkTask != null) return;
        
        for (Chunk chunk : Bukkit.getWorlds().get(0).getLoadedChunks()) {
            scanChunk(chunk);
        }
        
        checkTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!twistManager.isActive()) return;
            
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                Location playerLoc = player.getLocation();
                for (BoundingBox box : structures) {
                    if (removedStructures.contains(box)) continue;
                    
                    // Trigger distance: 50-100 blocks.
                    if (box.clone().expand(100.0).contains(playerLoc.toVector())) {
                        removeStructureAsync(box);
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
        // Broadly capture all structures
        Collection<GeneratedStructure> chunkStructures = chunk.getStructures();
        for (GeneratedStructure structure : chunkStructures) {
            String typeName = structure.getStructure().getKey().getKey().toLowerCase();
            if (typeName.contains("village") || typeName.contains("outpost") || typeName.contains("mansion") || typeName.contains("temple")) {
                structures.add(structure.getBoundingBox());
            }
        }
    }

    private void removeStructureAsync(BoundingBox box) {
        World world = plugin.getServer().getWorlds().get(0);
        
        // Calculate chunk ranges
        int minChunkX = ((int) box.getMinX()) >> 4;
        int maxChunkX = ((int) box.getMaxX()) >> 4;
        int minChunkZ = ((int) box.getMinZ()) >> 4;
        int maxChunkZ = ((int) box.getMaxZ()) >> 4;

        List<Chunk> affectedChunks = new ArrayList<>();
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (world.isChunkLoaded(cx, cz)) {
                    affectedChunks.add(world.getChunkAt(cx, cz));
                }
            }
        }

        // Processing 4 chunks per tick for ULTRA speed
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (affectedChunks.isEmpty()) {
                task.cancel();
                return;
            }

            int chunksThisTick = Math.min(affectedChunks.size(), 4);
            for (int i = 0; i < chunksThisTick; i++) {
                Chunk chunk = affectedChunks.remove(0);
                clearBuildingMaterialsInChunk(chunk, box);
            }
        }, 0L, 1L);
    }

    private void clearBuildingMaterialsInChunk(Chunk chunk, BoundingBox structureBox) {
        int minY = (int) structureBox.getMinY();
        int maxY = (int) structureBox.getMaxY();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (BUILDING_MATERIALS.contains(block.getType())) {
                        block.setType(Material.AIR, false);
                    }
                }
            }
        }
    }
}
