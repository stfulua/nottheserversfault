package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.manager.LobbyManager;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

public class PlayerJoinListener implements Listener {

    private final NotTheServersFault plugin;
    private final LobbyManager lobbyManager;
    private final TwistManager twistManager;

    public PlayerJoinListener(NotTheServersFault plugin, LobbyManager lobbyManager, TwistManager twistManager) {
        this.plugin = plugin;
        this.lobbyManager = lobbyManager;
        this.twistManager = twistManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Cancel reset timer if a target joins
        if (TargetUtil.isTarget(player)) {
            plugin.getWorldResetManager().cancelResetTimer();
        }

        // Handle joining while dead or in a broken state
        if (player.isDead()) {
            player.spigot().respawn();
        }

        if (!twistManager.isStarted()) {
            lobbyManager.sendToLobby(player);
        }
    }
}
