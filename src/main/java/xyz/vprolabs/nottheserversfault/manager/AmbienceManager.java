package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitTask;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages ambient "haunting" events like creepy sounds and phantom interactions.
 */
public final class AmbienceManager implements Listener {

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private BukkitTask hauntTask;

    public AmbienceManager(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void start() {
        if (hauntTask != null) return;
        
        hauntTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!twistManager.isActive()) return;
            
            TargetUtil.findTarget(plugin.getServer()).ifPresent(target -> {
                if (!target.isOnline()) return;
                
                int chance = ThreadLocalRandom.current().nextInt(100);
                if (chance < 40) { // 40% chance every 2 minutes for a haunt event
                    triggerHaunt(target);
                }
            });
        }, 2400L, 2400L); 
    }

    public void playScarySound(Player target) {
        Sound[] creepy = {
            Sound.ENTITY_CREEPER_PRIMED, 
            Sound.ENTITY_ENDERMAN_STARE,
            Sound.ENTITY_ZOMBIE_AMBIENT,
            Sound.ENTITY_PHANTOM_SWOOP,
            Sound.ENTITY_GHAST_SCREAM,
            Sound.ENTITY_WOLF_GROWL
        };
        Sound sound = creepy[ThreadLocalRandom.current().nextInt(creepy.length)];
        target.playSound(target.getLocation(), sound, 1.0f, 0.5f);
    }

    private void triggerHaunt(Player target) {
        int type = ThreadLocalRandom.current().nextInt(3);
        switch (type) {
            case 0: // Sound jumpscare
                Sound[] creepy = {
                    Sound.ENTITY_CREEPER_PRIMED, 
                    Sound.ENTITY_ENDERMAN_STARE,
                    Sound.BLOCK_WOODEN_DOOR_OPEN,
                    Sound.ENTITY_ZOMBIE_AMBIENT,
                    Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE,
                    Sound.ENTITY_PHANTOM_SWOOP,
                    Sound.ENTITY_PLAYER_ATTACK_NODAMAGE,
                    Sound.BLOCK_ANVIL_LAND
                };
                Sound sound = creepy[ThreadLocalRandom.current().nextInt(creepy.length)];
                target.playSound(
                    target.getLocation().subtract(target.getLocation().getDirection().multiply(3)),
                    sound, 0.7f, 0.5f
                );
                break;
            case 1: // Phantom Interaction (toggling doors/trapdoors)
                interactNearby(target);
                break;
            case 2: // Whispers and distorted portal sounds
                target.playSound(target.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.2f, 0.1f);
                break;
        }
    }

    private void interactNearby(Player target) {
        Location loc = target.getLocation();
        for (int x = -5; x <= 5; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -5; z <= 5; z++) {
                    Block block = loc.clone().add(x, y, z).getBlock();
                    if (block.getBlockData() instanceof Openable openable) {
                        boolean original = openable.isOpen();
                        openable.setOpen(!original);
                        block.setBlockData(openable);
                        
                        Sound sound = block.getType().name().contains("TRAPDOOR") ? Sound.BLOCK_WOODEN_TRAPDOOR_OPEN : Sound.BLOCK_WOODEN_DOOR_OPEN;
                        target.playSound(block.getLocation(), sound, 0.8f, 1.0f);

                        // Revert after 1 second
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            openable.setOpen(original);
                            block.setBlockData(openable);
                        }, 20L);
                        return;
                    }
                }
            }
        }
    }

    public void stop() {
        if (hauntTask != null) {
            hauntTask.cancel();
            hauntTask = null;
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!twistManager.isActive() || !TargetUtil.isTarget(event.getPlayer())) return;
        
        Block block = event.getBlock();
        if (block.getType() == Material.OAK_LOG || block.getType() == Material.STONE) {
            if (ThreadLocalRandom.current().nextInt(5) == 0) {
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_HURT, 0.2f, 0.5f);
            }
        }
    }
}
