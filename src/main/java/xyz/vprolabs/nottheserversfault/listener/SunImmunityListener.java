package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;

public class SunImmunityListener implements Listener {

    private final TwistManager twistManager;
    private final NamespacedKey sunImmuneKey;

    public SunImmunityListener(TwistManager twistManager, Plugin plugin) {
        this.twistManager = twistManager;
        this.sunImmuneKey = new NamespacedKey(plugin, "sun_immune");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        if (!twistManager.isActive() || !(event.getEntity() instanceof Monster monster)) return;

        Byte isImmune = monster.getPersistentDataContainer().get(sunImmuneKey, PersistentDataType.BYTE);
        if (isImmune != null && isImmune == (byte) 1) {
            event.setCancelled(true);
        }
    }
}
