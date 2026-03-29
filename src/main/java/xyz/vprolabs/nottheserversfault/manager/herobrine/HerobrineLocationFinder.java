package xyz.vprolabs.nottheserversfault.manager.herobrine;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class HerobrineLocationFinder {

    private static final int MIN_DISTANCE = 80;
    private static final int MAX_DISTANCE = 120;

    private HerobrineLocationFinder() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Location findLocation(Player target) {
        Location playerLoc = target.getLocation();
        List<Location> candidates = new ArrayList<>();
        List<Location> treeCandidates = new ArrayList<>();
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        for (int i = 0; i < 50; i++) {
            Location candidate = generateCandidate(playerLoc, rand);
            if (isValidSpawn(candidate)) {
                candidates.add(candidate);
                if (hasTreeNearby(candidate)) {
                    treeCandidates.add(candidate);
                }
            }
        }

        if (!treeCandidates.isEmpty()) {
            return treeCandidates.get(rand.nextInt(treeCandidates.size()));
        }
        if (!candidates.isEmpty()) {
            return candidates.get(rand.nextInt(candidates.size()));
        }
        return null;
    }

    private static Location generateCandidate(Location center, ThreadLocalRandom rand) {
        double angle = rand.nextDouble() * 2 * Math.PI;
        int distance = rand.nextInt(MIN_DISTANCE, MAX_DISTANCE + 1);

        int x = center.getBlockX() + (int) (Math.cos(angle) * distance);
        int z = center.getBlockZ() + (int) (Math.sin(angle) * distance);
        int y = center.getWorld().getHighestBlockYAt(x, z);

        return new Location(center.getWorld(), x + 0.5, y, z + 0.5);
    }

    private static boolean isValidSpawn(Location loc) {
        Block block = loc.getBlock();
        return block.getType().isSolid() && block.getRelative(0, 1, 0).getType().isAir();
    }

    private static boolean hasTreeNearby(Location loc) {
        World world = loc.getWorld();
        int bx = loc.getBlockX();
        int by = loc.getBlockY();
        int bz = loc.getBlockZ();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 4; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    Material type = world.getBlockAt(bx + dx, by + dy, bz + dz).getType();
                    if (type.name().contains("LOG") || type.name().contains("LEAVES")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
