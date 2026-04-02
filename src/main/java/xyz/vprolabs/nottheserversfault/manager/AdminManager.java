package xyz.vprolabs.nottheserversfault.manager;

import org.bukkit.profile.PlayerProfile;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminManager implements Listener {

    private final NotTheServersFault plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, PlayerProfile> originalProfiles = new HashMap<>();
    private boolean isVanished = true;

    public AdminManager(NotTheServersFault plugin) {
        this.plugin = plugin;
        setupTeam();
    }

    private boolean isExcluded(Player player) {
        return plugin.getTwistManager().isExcluded(player.getName());
    }

    private void setupTeam() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam("ntsf_hidden");
        if (team == null) {
            team = board.registerNewTeam("ntsf_hidden");
        }
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    }

    private void addAdminToTeam(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam("ntsf_hidden");
        if (team != null && !team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    public void toggleVanish(Player player) {
        setVanish(player, !isVanished);
        String msg = isVanished ? "§aYou are now vanished!" : "§cYou are now visible!";
        plugin.getAudiences().player(player).sendMessage(Component.text(msg));
    }

    private void setVanish(Player admin, boolean vanish) {
        this.isVanished = vanish;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (isExcluded(online)) continue;
            if (vanish) {
                online.hidePlayer(plugin, admin);
            } else {
                online.showPlayer(plugin, admin);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isExcluded(player)) {
            event.setJoinMessage(null);
            addAdminToTeam(player);
            if (isVanished) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.hidePlayer(plugin, player);
                }
            }
        } else {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (isExcluded(online) && isVanished) {
                    player.hidePlayer(plugin, online);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        if (isExcluded(event.getPlayer())) {
            event.setQuitMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (isExcluded(event.getPlayer())) {
            // Standard Bukkit doesn't have event.message(null) for advancements
            // This was a Paper-ism. On Spigot, we just ignore it as we can't easily cancel the broadcast here
            // without more complex logic or just disabling advancement announcements globally.
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage().toLowerCase();
        for (String excluded : plugin.getTwistManager().getExcludedPlayers()) {
            if (message.contains(excluded.toLowerCase())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        List<String> excluded = plugin.getTwistManager().getExcludedPlayers();
        event.getCompletions().removeIf(completion -> excluded.contains(completion.toLowerCase()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSend(PlayerCommandSendEvent event) {
        if (isExcluded(event.getPlayer())) return;
        List<String> excluded = plugin.getTwistManager().getExcludedPlayers();
        event.getCommands().removeIf(command -> {
            for (String ex : excluded) {
                if (command.contains(ex)) return true;
            }
            return false;
        });
    }
}
