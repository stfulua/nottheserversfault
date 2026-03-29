package xyz.vprolabs.nottheserversfault.manager.herobrine;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class HerobrineSpawnTask implements Runnable {

    private final Plugin plugin;
    private final Player target;
    private static final List<TextureProperty> TEXTURES = List.of(new TextureProperty("textures", HerobrineSkin.TEXTURE, HerobrineSkin.SIGNATURE));

    public HerobrineSpawnTask(Plugin plugin, Player target) {
        this.plugin = plugin;
        this.target = target;
    }

    @Override
    public void run() {
        if (!target.isOnline()) return;

        Location loc = HerobrineLocationFinder.findLocation(target);
        if (loc != null) spawnHerobrine(target, loc);
    }

    private void spawnHerobrine(Player target, Location loc) {
        int entityId = SpigotReflectionUtil.generateEntityId();
        UUID uuid = UUID.randomUUID();

        UserProfile profile = new UserProfile(uuid, "Herobrine", TEXTURES);

        WrapperPlayServerPlayerInfoUpdate.PlayerInfo infoData = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                profile, true, 0, GameMode.SURVIVAL, Component.text("Herobrine"), null
        );

        WrapperPlayServerPlayerInfoUpdate infoAdd = new WrapperPlayServerPlayerInfoUpdate(
                WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, infoData
        );

        WrapperPlayServerSpawnEntity spawn = new WrapperPlayServerSpawnEntity(
                entityId, Optional.of(uuid), EntityTypes.PLAYER,
                HerobrineLocationConverter.toVector3d(loc),
                0f, 0f, 0f, 0, Optional.empty()
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(target, infoAdd);
        PacketEvents.getAPI().getPlayerManager().sendPacket(target, spawn);

        // Randomize sound based on intensity
        int encounterType = ThreadLocalRandom.current().nextInt(10);
        if (encounterType < 3) { // 30% chance for a loud "jumpscare" encounter
            target.playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.4f, 0.5f);
            target.playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.3f, 0.5f);
        } else {
            target.playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.2f, 0.5f);
        }

        // Random duration (3-8 seconds)
        long durationTicks = 60L + ThreadLocalRandom.current().nextInt(100);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!target.isOnline()) return;
            WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(entityId);
            WrapperPlayServerPlayerInfoRemove infoRemove = new WrapperPlayServerPlayerInfoRemove(uuid);

            PacketEvents.getAPI().getPlayerManager().sendPacket(target, destroy);
            PacketEvents.getAPI().getPlayerManager().sendPacket(target, infoRemove);
        }, durationTicks);
    }
}
