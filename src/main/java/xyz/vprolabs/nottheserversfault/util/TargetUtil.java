package xyz.vprolabs.nottheserversfault.util;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

import java.util.Optional;

public final class TargetUtil {

    private TargetUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isTarget(@NotNull Player player) {
        NotTheServersFault plugin = NotTheServersFault.getPlugin(NotTheServersFault.class);
        return !plugin.getTwistManager().isExcluded(player.getName());
    }

    public static Optional<Player> findTarget(@NotNull Server server) {
        NotTheServersFault plugin = NotTheServersFault.getPlugin(NotTheServersFault.class);
        return server.getOnlinePlayers().stream()
                .filter(TargetUtil::isTarget)
                .map(p -> (Player) p)
                .findAny();
    }
}
