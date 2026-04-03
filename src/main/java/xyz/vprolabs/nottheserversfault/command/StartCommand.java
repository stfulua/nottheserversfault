package xyz.vprolabs.nottheserversfault.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.vprolabs.nottheserversfault.manager.GraceManager;
import xyz.vprolabs.nottheserversfault.manager.LobbyManager;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;

import java.util.Collections;
import java.util.List;

public class StartCommand implements CommandExecutor, TabCompleter {

    private final LobbyManager lobbyManager;
    private final GraceManager graceManager;
    private final TwistManager twistManager;

    public StartCommand(LobbyManager lobbyManager, GraceManager graceManager, TwistManager twistManager) {
        this.lobbyManager = lobbyManager;
        this.graceManager = graceManager;
        this.twistManager = twistManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!lobbyManager.isInLobby(player)) {
            player.sendMessage("§cYou must be in the lobby to use this command!");
            return true;
        }

        lobbyManager.setReady(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
