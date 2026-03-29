package xyz.vprolabs.nottheserversfault.listener;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.manager.LobbyManager;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

public class PlayerStateListener implements Listener {

    private final NotTheServersFault plugin;
    private final LobbyManager lobbyManager;
    private final TwistManager twistManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PlayerStateListener(NotTheServersFault plugin, LobbyManager lobbyManager, TwistManager twistManager) {
        this.plugin = plugin;
        this.lobbyManager = lobbyManager;
        this.twistManager = twistManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!TargetUtil.isTarget(player)) return;

        // Increment deaths
        twistManager.incrementDeaths();

        // Ensure drop inventory is kept if in lobby
        if (!twistManager.isStarted()) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!TargetUtil.isTarget(player)) return;

        // Show death count
        player.sendActionBar(miniMessage.deserialize("<red><bold>Death Count: <yellow>" + twistManager.getDeathCount()));

        if (!twistManager.isStarted()) {
            // Force back to lobby state
            lobbyManager.sendToLobby(player);
            // Set respawn location to lobby
            event.setRespawnLocation(lobbyManager.getLobbyLocation()); 
        } else {
            // If game started, ensure they are in Survival
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (!TargetUtil.isTarget(player)) return;

        // Prevent leaving survival if game is active or lobby if not started
        if (twistManager.isStarted() && !twistManager.isFinished()) {
            if (event.getNewGameMode() != GameMode.SURVIVAL) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        if (!twistManager.isStarted()) {
            // Check if remaining players are all ready
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                lobbyManager.checkStartCondition();
            }, 1L);
        }
    }
}
