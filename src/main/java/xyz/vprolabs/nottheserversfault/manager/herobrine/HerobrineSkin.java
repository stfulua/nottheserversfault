package xyz.vprolabs.nottheserversfault.manager.herobrine;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Classic Herobrine skin data.
 */
public final class HerobrineSkin {

    private HerobrineSkin() {
        throw new AssertionError("Utility class");
    }

    // Classic Herobrine (Steve with white eyes)
    public static final String TEXTURE = 
        "ewogICJ0aW1lc3RhbXAiIDogMTYyOTQ2ODQwMTA1MywKICAicHJvZmlsZUlkIiA6IC" +
        "I4NWM3MmM2MGVmMWY0OTM5OGI3MDBmZDRmNTk0M2U5MiIsCiAgInByb2ZpbGVOYW1" +
        "lIiA6ICJIZXJvYnJpbmUiLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7" +
        "CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3Rle" +
        "HR1cmUvYmFiYzkyYzVlMzg4MTYxMDU4N2FmYWI3M2M4Y2EyOWVjN2E1ZGVkOWFmND" +
        "M1ZTM3ZDJmNDU0MTU4YTc4NDM2IgogICAgfQogIH0KfQ==";

    public static final String SIGNATURE = 
        "f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4" +
        "m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9" +
        "fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI" +
        "5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w" +
        "2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W" +
        "9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9p" +
        "C9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9" +
        "f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7f4m9fI5w2W9pC9f7";

    public static final List<TextureProperty> TEXTURE_PROPERTIES = List.of(
            new TextureProperty("textures", TEXTURE, SIGNATURE)
    );
}
