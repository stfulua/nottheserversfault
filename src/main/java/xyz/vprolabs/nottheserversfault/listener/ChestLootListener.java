package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;

import java.util.Collections;

public class ChestLootListener implements Listener {

    private final TwistManager twistManager;

    public ChestLootListener(TwistManager twistManager) {
        this.twistManager = twistManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        if (twistManager.isActive()) {
            Collections.shuffle(event.getLoot());
        }
    }
}
