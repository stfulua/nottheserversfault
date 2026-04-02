package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;

import java.util.concurrent.ThreadLocalRandom;

public class MobSpawnListener implements Listener {

    private final TwistManager twistManager;
    private final Plugin plugin;
    private final NamespacedKey sunImmuneKey;

    public MobSpawnListener(TwistManager twistManager, Plugin plugin) {
        this.twistManager = twistManager;
        this.plugin = plugin;
        this.sunImmuneKey = new NamespacedKey(plugin, "sun_immune");
        
        startDaytimeSpawner();
    }

    private void startDaytimeSpawner() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!twistManager.isActive()) return;
            
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                World world = player.getWorld();
                long time = world.getTime();
                
                // If daytime (roughly 0 to 12000)
                if (time < 12000) {
                    // Increased frequency: 50% chance every 2 seconds
                    if (ThreadLocalRandom.current().nextInt(2) == 0) {
                        spawnHostileNear(player);
                        // 30% chance for a second monster
                        if (ThreadLocalRandom.current().nextInt(10) < 3) {
                            spawnHostileNear(player);
                        }
                    }
                }
            }
        }, 0L, 40L); // Every 2 seconds
    }

    private void spawnHostileNear(Player player) {
        double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
        double distance = ThreadLocalRandom.current().nextDouble(24, 40);
        double x = Math.cos(angle) * distance;
        double z = Math.sin(angle) * distance;
        Location loc = player.getLocation().add(x, 0, z);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc) + 1);
        
        EntityType[] types = {
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, 
            EntityType.SPIDER, EntityType.HUSK, EntityType.STRAY, EntityType.WITCH
        };
        EntityType type = types[ThreadLocalRandom.current().nextInt(types.length)];
        
        Monster monster = (Monster) loc.getWorld().spawnEntity(loc, type);
        monster.getPersistentDataContainer().set(sunImmuneKey, PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!twistManager.isActive()) return;
        
        // Sun immunity for monsters
        if (event.getEntity() instanceof Monster monster) {
            monster.getPersistentDataContainer().set(sunImmuneKey, PersistentDataType.BYTE, (byte) 1);
        }
        
        // Random sizes for animals
        if (event.getEntity() instanceof Animals animals) {
            double scale = ThreadLocalRandom.current().nextDouble(0.2, 5.0);
            try {
                // GENERIC_SCALE is available on 1.20.5+
                AttributeInstance attribute = animals.getAttribute(Attribute.valueOf("GENERIC_SCALE"));
                if (attribute != null) {
                    attribute.setBaseValue(scale);
                }
            } catch (IllegalArgumentException ignored) {
                // If the attribute doesn't exist on this version/entity
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        if (!twistManager.isActive() || !(event.getEntity() instanceof Monster monster)) return;
        if (monster.getPersistentDataContainer().has(sunImmuneKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!twistManager.isActive() || !(event.getEntity() instanceof Monster monster)) return;
        if (monster.getPersistentDataContainer().has(sunImmuneKey, PersistentDataType.BYTE)) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE || 
                event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                event.setCancelled(true);
            }
        }
    }
}
