package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.vprolabs.nottheserversfault.manager.LobbyManager;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

public class PlayerJoinListener implements Listener {

    private final LobbyManager lobbyManager;
    private final TwistManager twistManager;

    public PlayerJoinListener(LobbyManager lobbyManager, TwistManager twistManager) {
        this.lobbyManager = lobbyManager;
        this.twistManager = twistManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Handle joining while dead or in a broken state
        if (player.isDead()) {
            player.spigot().respawn();
        }

        if (!twistManager.isStarted()) {
            lobbyManager.sendToLobby(player);
        }
    }
}
