package xyz.vprolabs.nottheserversfault.manager;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class LobbyManager {

    private final NotTheServersFault plugin;
    private final Map<UUID, BukkitTask> freezeTasks = new ConcurrentHashMap<>();
    private final Set<UUID> readyPlayers = ConcurrentHashMap.newKeySet();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
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
        this.goalBar = BossBar.bossBar(
            miniMessage.deserialize("<gold>Your goal: <white>Get a diamond block"),
            1.0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.PROGRESS
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
        plugin.getAudiences().player(player).showBossBar(goalBar);

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
                
                int ping = player.getPing();
                boolean isLagging = ping > 250;

                if (!isLagging && ThreadLocalRandom.current().nextInt(60) == 0) {
                    spawnFirework(lobbyLoc.clone().add(
                        ThreadLocalRandom.current().nextDouble(-15, 15),
                        ThreadLocalRandom.current().nextDouble(5, 15),
                        ThreadLocalRandom.current().nextDouble(-15, 15)
                    ));
                }

                if (!isLagging && ticks % 8 == 0) {
                    int step = (ticks / 8) % sequence.length;
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.6f, sequence[step]);
                    if (ticks % 16 == 0) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.4f, sequence[step] * 0.5f);
                    }
                }

                if (ticks % 20 == 0) {
                    updateUI(player);
                }
                ticks++;
            }
        }, 0L, 1L);
        freezeTasks.put(uuid, task);
    }

    private void updateUI(Player player) {
        Component titleText = miniMessage.deserialize("<gradient:gold:yellow><bold>WAITING FOR GAME");
        boolean isExcluded = plugin.getTwistManager().isExcluded(player.getName());
        Component subtitleText;
        String chunkyProgress = plugin.getChunkyManager().getProgressString();
        
        if (chunkyProgress != null) {
            titleText = miniMessage.deserialize("<gradient:red:gold><bold>LOADING TERRAIN...");
            subtitleText = miniMessage.deserialize("<white>Progress: <yellow>" + chunkyProgress);
        } else if (isExcluded) {
            subtitleText = miniMessage.deserialize("<white>You are a <gray>Spectator <white>- Waiting for players...");
        } else if (readyPlayers.contains(player.getUniqueId())) {
            subtitleText = miniMessage.deserialize("<green>READY! <white>Waiting for others...");
        } else {
            subtitleText = miniMessage.deserialize("<white>Type <green>/start <white>to ready up!");
        }

        plugin.getAudiences().player(player).showTitle(Title.title(titleText, subtitleText));
        plugin.getAudiences().player(player).sendActionBar(miniMessage.deserialize("<red>Please turn the game volume <yellow>Up <red>before starting!"));
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
            plugin.getAudiences().player(player).sendMessage(miniMessage.deserialize("<red>Please wait for terrain to finish loading before starting!"));
            return;
        }
        if (plugin.getTwistManager().isExcluded(player.getName())) {
            plugin.getAudiences().player(player).sendMessage(miniMessage.deserialize("<red>Spectators do not need to ready up!"));
            return;
        }
        if (readyPlayers.contains(player.getUniqueId())) {
            plugin.getAudiences().player(player).sendMessage(miniMessage.deserialize("<yellow>You are already ready!"));
            return;
        }
        readyPlayers.add(player.getUniqueId());
        int totalTargets = (int) Bukkit.getOnlinePlayers().stream().filter(TargetUtil::isTarget).count();
        int readyCount = readyPlayers.size();
        plugin.getAudiences().all().sendMessage(miniMessage.deserialize("<green>" + player.getName() + " <white>is ready! (<yellow>" + readyCount + "<white>/<yellow>" + totalTargets + "<white>)"));
        checkStartCondition();
    }

    public void checkStartCondition() {
        if (plugin.getTwistManager().isStarted()) return;
        List<Player> targets = Bukkit.getOnlinePlayers().stream()
                .filter(TargetUtil::isTarget)
                .map(p -> (Player) p)
                .toList();
        if (targets.isEmpty()) return;
        boolean allReady = targets.stream().allMatch(p -> readyPlayers.contains(p.getUniqueId()));
        if (allReady) {
            plugin.getAudiences().all().sendMessage(miniMessage.deserialize("<green><bold>ALL PLAYERS READY! <white>Starting challenge..."));
            plugin.getServer().getOnlinePlayers().forEach(this::releaseFromLobby);
            plugin.getTwistManager().setStarted(true);
            plugin.getGraceManager().startCountdown();
        }
    }

    public void releaseFromLobby(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.setGravity(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        plugin.getAudiences().player(player).hideBossBar(goalBar);
        BukkitTask task = freezeTasks.remove(uuid);
        if (task != null) task.cancel();
        if (player.isOnline()) {
            player.teleport(player.getWorld().getSpawnLocation());
            plugin.getAudiences().player(player).clearTitle();
        }
    }

    public void shutdown() {
        freezeTasks.values().forEach(BukkitTask::cancel);
        freezeTasks.clear();
        for (UUID uuid : freezeTasks.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.setGravity(true);
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.setAllowFlight(false);
                player.setFlying(false);
                plugin.getAudiences().player(player).hideBossBar(goalBar);
            }
        }
    }

    public Location getLobbyLocation() { return lobbyLoc.clone(); }
    public boolean isInLobby(@NotNull Player player) { return freezeTasks.containsKey(player.getUniqueId()); }
}
