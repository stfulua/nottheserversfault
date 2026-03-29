package xyz.vprolabs.nottheserversfault.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
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
    public void onChat(AsyncChatEvent event) {
        if (lobbyManager.isInLobby(event.getPlayer())) {
            event.setCancelled(true);
        }
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
