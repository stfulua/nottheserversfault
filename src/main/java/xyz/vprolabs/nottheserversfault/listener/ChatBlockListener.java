package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import xyz.vprolabs.nottheserversfault.manager.LobbyManager;

public class ChatBlockListener implements Listener {

    private final LobbyManager lobbyManager;

    public ChatBlockListener(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (lobbyManager.isInLobby(event.getPlayer())) {
            if (!event.getMessage().toLowerCase().startsWith("/start")) {
                event.setCancelled(true);
            }
        }
    }
}
