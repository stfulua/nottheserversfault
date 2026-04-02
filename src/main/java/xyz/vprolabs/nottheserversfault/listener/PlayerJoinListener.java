package xyz.vprolabs.nottheserversfault.listener;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PlayerJoinListener(NotTheServersFault plugin, LobbyManager lobbyManager, TwistManager twistManager) {
        this.plugin = plugin;
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
            
            // Notify about player count
            int current = (int) Bukkit.getOnlinePlayers().stream().filter(TargetUtil::isTarget).count();
            int total = Bukkit.getMaxPlayers(); // Or a custom number if you prefer
            
            plugin.getAudiences().all().sendMessage(miniMessage.deserialize(
                "<gray>Waiting for Players <white>(<yellow>" + current + "<white>/<yellow>" + current + "<white>)"
            ));
            // Note: Using current/current as placeholder since "all-players" target count 
            // is dynamic. If you have a specific target count in mind, replace the second current.
        }
    }
}
