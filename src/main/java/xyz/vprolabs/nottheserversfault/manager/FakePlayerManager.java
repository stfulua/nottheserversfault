package xyz.vprolabs.nottheserversfault.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages fake players that appear in the Tab list and Chat.
 * Designed to create a "ghostly" presence on the server.
 */
public final class FakePlayerManager {

    private final NotTheServersFault plugin;
    private final TwistManager twistManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitTask mainTask;
    
    private final List<String> fakeNames = Arrays.asList("Mine_Master", "Old_Steve", "Null_User", "Stalker_7", "LostSoul", "Unknown_User");
    private final List<String> messages = Arrays.asList(
        "Does anyone know the way to the nether?",
        "I think I'm lost... everything looks the same.",
        "Wait, did you hear that?",
        "Is anyone else seeing those eyes in the dark?",
        "I'm at 0, 0... where are you guys?",
        "This seed feels... wrong.",
        "Stop following me.",
        "I found a house but it's empty... just like mine."
    );

    private final Map<UUID, UUID> activeFakePlayers = new HashMap<>(); // Target UUID -> Fake Player UUID

    public FakePlayerManager(NotTheServersFault plugin, TwistManager twistManager) {
        this.plugin = plugin;
        this.twistManager = twistManager;
    }

    public void start() {
        if (mainTask != null) return;

        mainTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!twistManager.isActive()) return;

            TargetUtil.findTarget(plugin.getServer()).ifPresent(target -> {
                // Skip if player is lagging (Ping > 300)
                if (target.getPing() > 300) return;

                // ~15% chance every minute to trigger a fake player event
                if (ThreadLocalRandom.current().nextInt(100) < 15) {
                    triggerFakePlayerEvent(target);
                }
            });
        }, 1200L, 1200L); // Check every minute
    }

    public void stop() {
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        activeFakePlayers.forEach((targetId, fakeId) -> {
            Player target = plugin.getServer().getPlayer(targetId);
            if (target != null && target.isOnline()) {
                removeFakeFromTab(target, fakeId);
            }
        });
        activeFakePlayers.clear();
    }

    private void triggerFakePlayerEvent(Player target) {
        String name = fakeNames.get(ThreadLocalRandom.current().nextInt(fakeNames.size()));
        UUID fakeUuid = UUID.randomUUID();
        
        // Add to Tab and send Join Message
        addFakeToTab(target, name, fakeUuid);
        activeFakePlayers.put(target.getUniqueId(), fakeUuid);
        
        Component joinMsg = miniMessage.deserialize("<yellow>" + name + " joined the game");
        target.sendMessage(joinMsg);
        plugin.getLogger().info("[FakePlayer] " + name + " joined the game (Visible to: " + target.getName() + ")");

        // Send message after a random delay (10-30s)
        int messageDelay = ThreadLocalRandom.current().nextInt(200, 600);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!target.isOnline() || !activeFakePlayers.containsKey(target.getUniqueId())) return;
            
            String msg = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
            // Use white for name and message to match 1.21.x default chat style
            Component chatMsg = miniMessage.deserialize("<white><" + name + "> " + msg);
            target.sendMessage(chatMsg);
            
            // Log to console so admin can see what happened
            plugin.getLogger().info("[FakePlayer] <" + name + "> " + msg + " (Sent to: " + target.getName() + ")");
            
            // Remove after another delay (10-15s)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                removeFakeFromTab(target, fakeUuid);
                activeFakePlayers.remove(target.getUniqueId());
                plugin.getLogger().info("[FakePlayer] " + name + " left the game");
            }, 200L + ThreadLocalRandom.current().nextInt(100));
        }, messageDelay);
    }

    private void addFakeToTab(Player target, String name, UUID uuid) {
        UserProfile profile = new UserProfile(uuid, name, Collections.emptyList());
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
            profile, true, 50, GameMode.SURVIVAL, Component.text(name), null
        );
        WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(
            WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, info
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(target, packet);
    }

    private void removeFakeFromTab(Player target, UUID uuid) {
        WrapperPlayServerPlayerInfoRemove packet = new WrapperPlayServerPlayerInfoRemove(uuid);
        PacketEvents.getAPI().getPlayerManager().sendPacket(target, packet);
    }
}
