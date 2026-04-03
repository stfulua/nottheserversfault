package xyz.vprolabs.nottheserversfault.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class FakePlayerManager {

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private BukkitTask mainTask;
    
    private final List<String> fakeNames = Arrays.asList(
        "Mine_Master", "Old_Steve", "Null_User", "Stalker_7", "LostSoul", 
        "Unknown_User", "VoidWalker", "Shadow_Miner", "GrimReaper66", "TheWatcher",
        "Herobrine_Fan", "MissingTexture", "System_Error", "GlitchedOut", "PhantomLink",
        "Entity_303", "Loner_MC", "SilentStep", "EchoInDark", "CursedSteve"
    );

    private final List<String> messages = Arrays.asList(
        "Does anyone know the way to the nether?",
        "I think I'm lost... everything looks the same.",
        "Wait, did you hear that?",
        "Is anyone else seeing those eyes in the dark?",
        "I'm at 0, 0... where are you guys?",
        "This seed feels... wrong.",
        "Stop following me.",
        "I found a house but it's empty... just like mine.",
        "The fog is getting thicker.",
        "Why is it so quiet here?",
        "I keep hearing footsteps behind me.",
        "My torch just went out on its own.",
        "There's something in the walls.",
        "Don't look back.",
        "It's watching us.",
        "The deepslate is different here.",
        "I saw a player skin I didn't recognize.",
        "Is the server lagging or is it just me?",
        "The caves are breathing.",
        "I shouldn't have come here."
    );

    private final Set<UUID> playersInSequence = new HashSet<>();
    private final Map<UUID, Long> nextEventTime = new HashMap<>();

    public FakePlayerManager(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void start() {
        if (mainTask != null) return;
        mainTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!twistManager.isActive()) return;
            long now = System.currentTimeMillis();
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (!TargetUtil.isTarget(target)) continue;
                if (playersInSequence.contains(target.getUniqueId())) continue;
                
                long next = nextEventTime.getOrDefault(target.getUniqueId(), 0L);
                if (now >= next) {
                    triggerFakePlayerSequence(target);
                }
            }
        }, 100L, 100L); // Check every 5 seconds
    }

    public void stop() {
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        playersInSequence.clear();
        nextEventTime.clear();
    }

    private void triggerFakePlayerSequence(Player target) {
        playersInSequence.add(target.getUniqueId());
        
        String name = fakeNames.get(ThreadLocalRandom.current().nextInt(fakeNames.size()));
        UUID fakeUuid = UUID.randomUUID();
        
        // 1. Join
        addFakeToTab(target, name, fakeUuid);
        target.sendMessage("§e" + name + " joined the game");

        // 2. Wait random seconds (3-10s) -> Message
        int messageDelay = ThreadLocalRandom.current().nextInt(60, 200);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!target.isOnline()) {
                endSequence(target);
                return;
            }
            
            String msg = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
            target.sendMessage("§f<" + name + "> " + msg);
            
            // 3. Wait random seconds (5-15s) -> Leave Message
            int leaveDelay = ThreadLocalRandom.current().nextInt(100, 300);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (target.isOnline()) {
                    target.sendMessage("§e" + name + " left the game");
                    removeFakeFromTab(target, fakeUuid);
                }
                
                // 4. Cooldown (3-8 minutes)
                int cooldownMinutes = ThreadLocalRandom.current().nextInt(3, 9);
                nextEventTime.put(target.getUniqueId(), System.currentTimeMillis() + (cooldownMinutes * 60000L));
                endSequence(target);
            }, leaveDelay);
        }, messageDelay);
    }

    private void endSequence(Player target) {
        playersInSequence.remove(target.getUniqueId());
    }

    private void addFakeToTab(Player target, String name, UUID uuid) {
        UserProfile profile = new UserProfile(uuid, name, Collections.emptyList());
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
            profile, true, 50, GameMode.SURVIVAL, Component.text(name), null
        );
        WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(
            EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, 
                       WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
                       WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME), 
            info
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(target, packet);
    }

    private void removeFakeFromTab(Player target, UUID uuid) {
        WrapperPlayServerPlayerInfoRemove packet = new WrapperPlayServerPlayerInfoRemove(uuid);
        PacketEvents.getAPI().getPlayerManager().sendPacket(target, packet);
    }
}
