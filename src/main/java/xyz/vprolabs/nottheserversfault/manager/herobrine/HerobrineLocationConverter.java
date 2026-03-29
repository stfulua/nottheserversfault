package xyz.vprolabs.nottheserversfault.manager.herobrine;

import com.github.retrooper.packetevents.util.Vector3d;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Converts Bukkit locations to PacketEvents vectors.
 */
public final class HerobrineLocationConverter {

    private HerobrineLocationConverter() {
        throw new AssertionError("Utility class");
    }

    /**
     * Converts a Bukkit Location to PacketEvents Vector3d.
     *
     * @param loc the Bukkit location
     * @return the PacketEvents vector
     */
    public static @NotNull Vector3d toVector3d(@NotNull Location loc) {
        return new Vector3d(loc.getX(), loc.getY(), loc.getZ());
    }
}
