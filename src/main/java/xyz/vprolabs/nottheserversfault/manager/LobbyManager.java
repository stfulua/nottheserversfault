package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class LobbyManager {

    private final NotTheServersFault plugin;
    private final Map<UUID, BukkitTask> freezeTasks = new ConcurrentHashMap<>();
    private final Set<UUID> readyPlayers = ConcurrentHashMap.newKeySet();
    private final Vector zeroVector = new Vector(0, 0, 0);
    private final Location lobbyLoc;
    private final BossBar goalBar;
    
    private final float[] sequence = {
        0.594604f, 0.707107f, 0.890899f, 1.059463f, 
        1.189207f, 1.059463f, 0.890899f, 0.707107f
    }; 

    public LobbyManager(NotTheServersFault plugin) {
        this.plugin = plugin;
        World world = Bukkit.getWorlds().get(0);
        this.lobbyLoc = new Location(world, 0.5, 6700, 0.5);
        this.goalBar = Bukkit.createBossBar(
            "§6Your goal: §fGet a diamond block",
            BarColor.BLUE,
            BarStyle.SOLID
        );
        generatePlatform();
    }

    private void generatePlatform() {
        World world = lobbyLoc.getWorld();
        int lx = lobbyLoc.getBlockX();
        int ly = lobbyLoc.getBlockY();
        int lz = lobbyLoc.getBlockZ();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                world.getBlockAt(lx + x, ly - 1, lz + z).setType(Material.GLASS);
                if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                    for (int y = 0; y < 4; y++) {
                        world.getBlockAt(lx + x, ly + y, lz + z).setType(Material.BARRIER);
                    }
                }
            }
        }
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                world.getBlockAt(lx + x, ly + 4, lz + z).setType(Material.BARRIER);
            }
        }
    }

    public void sendToLobby(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        if (freezeTasks.containsKey(uuid)) return;

        player.teleport(lobbyLoc);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, PotionEffect.INFINITE_DURATION, 1, false, false, false));
        player.setGravity(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        goalBar.addPlayer(player);

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (!player.isOnline()) {
                    releaseFromLobby(player);
                    return;
                }
                
                if (player.getLocation().distanceSquared(lobbyLoc) > 25) {
                    player.teleport(lobbyLoc);
                }
                player.setVelocity(zeroVector);
                
                if (ticks % 20 == 0) {
                    int ping = player.getPing();
                    boolean isLagging = ping > 250;

                    if (!isLagging) {
                        int step = (ticks / 20) % sequence.length;
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, sequence[step]);
                        
                        if (ThreadLocalRandom.current().nextInt(10) == 0) {
                            spawnFirework(lobbyLoc.clone().add(
                                ThreadLocalRandom.current().nextDouble(-10, 10),
                                ThreadLocalRandom.current().nextDouble(5, 10),
                                ThreadLocalRandom.current().nextDouble(-10, 10)
                            ));
                        }
                    }
                    updateUI(player);
                }
                ticks++;
            }
        }, 0L, 1L);
        freezeTasks.put(uuid, task);
    }

    private void updateUI(Player player) {
        String title = "§6§lWAITING FOR GAME";
        String subtitle;
        String chunkyProgress = plugin.getChunkyManager().getProgressString();
        
        if (chunkyProgress != null) {
            title = "§c§lLOADING TERRAIN...";
            subtitle = "§fProgress: §e" + chunkyProgress;
        } else if (plugin.getTwistManager().isExcluded(player.getName())) {
            subtitle = "§fYou are a §7Spectator §f- Waiting...";
        } else if (readyPlayers.contains(player.getUniqueId())) {
            subtitle = "§aREADY! §fWaiting for others...";
        } else {
            subtitle = "§fType §a/start §fto ready up!";
        }

        player.sendTitle(title, subtitle, 0, 40, 10);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§cPlease turn volume §eUP §cbefore starting!"));
    }

    private void spawnFirework(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.addEffect(FireworkEffect.builder()
            .withColor(Color.fromRGB(ThreadLocalRandom.current().nextInt(0xFFFFFF)))
            .withFade(Color.WHITE)
            .with(FireworkEffect.Type.BALL_LARGE)
            .flicker(true)
            .build());
        fwm.setPower(1);
        fw.setFireworkMeta(fwm);
        fw.setShotAtAngle(true); 
    }

    public void setReady(@NotNull Player player) {
        if (plugin.getChunkyManager().isChunkyRunning()) {
            player.sendMessage("§cPlease wait for terrain to finish loading before starting!");
            return;
        }
        if (plugin.getTwistManager().isExcluded(player.getName())) {
            player.sendMessage("§cSpectators do not need to ready up!");
            return;
        }
        if (readyPlayers.contains(player.getUniqueId())) {
            player.sendMessage("§eYou are already ready!");
            return;
        }
        readyPlayers.add(player.getUniqueId());
        int current = readyPlayers.size();
        int total = (int) Bukkit.getOnlinePlayers().stream().filter(TargetUtil::isTarget).count();
        Bukkit.broadcastMessage("§a" + player.getName() + " §fis ready! (§e" + current + "§f/§e" + total + "§f)");
        checkStartCondition();
    }

    public void checkStartCondition() {
        if (plugin.getTwistManager().isStarted()) return;
        long targetCount = Bukkit.getOnlinePlayers().stream().filter(TargetUtil::isTarget).count();
        if (targetCount == 0) return;
        boolean allReady = Bukkit.getOnlinePlayers().stream()
                .filter(TargetUtil::isTarget)
                .allMatch(p -> readyPlayers.contains(p.getUniqueId()));

        if (allReady) {
            resetWorldState();
            Bukkit.broadcastMessage("§a§lALL PLAYERS READY! §fStarting challenge...");
            Bukkit.getOnlinePlayers().forEach(this::releaseFromLobby);
            plugin.getTwistManager().setStarted(true);
            plugin.getGraceManager().startCountdown();
        }
    }

    private void resetWorldState() {
        World world = Bukkit.getWorlds().get(0);
        world.setTime(0);
        world.setStorm(false);
        world.setThundering(false);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(5.0f);
            player.setFireTicks(0);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }
    }

    public void releaseFromLobby(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.setGravity(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        goalBar.removePlayer(player);
        BukkitTask task = freezeTasks.remove(uuid);
        if (task != null) task.cancel();
        if (player.isOnline()) {
            player.teleport(player.getWorld().getSpawnLocation());
            player.resetTitle();
        }
    }

    public void shutdown() {
        freezeTasks.values().forEach(BukkitTask::cancel);
        freezeTasks.clear();
        goalBar.removeAll();
    }

    public Location getLobbyLocation() { return lobbyLoc.clone(); }
    public boolean isInLobby(@NotNull Player player) { return freezeTasks.containsKey(player.getUniqueId()); }
}
