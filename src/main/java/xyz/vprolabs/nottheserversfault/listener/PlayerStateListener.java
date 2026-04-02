package xyz.vprolabs.nottheserversfault.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.manager.LobbyManager;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

public class PlayerStateListener implements Listener {

    private final NotTheServersFault plugin;
    private final LobbyManager lobbyManager;
    private final TwistManager twistManager;

    public PlayerStateListener(NotTheServersFault plugin, LobbyManager lobbyManager, TwistManager twistManager) {
        this.plugin = plugin;
        this.lobbyManager = lobbyManager;
        this.twistManager = twistManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!TargetUtil.isTarget(player)) return;

        twistManager.incrementDeaths();

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

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c§lDeath Count: §e" + twistManager.getDeathCount()));

        if (!twistManager.isStarted()) {
            lobbyManager.sendToLobby(player);
            event.setRespawnLocation(lobbyManager.getLobbyLocation()); 
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (!TargetUtil.isTarget(player)) return;

        if (twistManager.isStarted() && !twistManager.isFinished()) {
            if (event.getNewGameMode() != GameMode.SURVIVAL) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        if (!twistManager.isStarted()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                lobbyManager.checkStartCondition();
            }, 1L);
        }
    }
}
