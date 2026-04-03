package xyz.vprolabs.nottheserversfault.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.manager.TwistManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final NotTheServersFault plugin;

    public AdminCommand(NotTheServersFault plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("ntsf.admin")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6§lNotTheServersFault Admin");
            sender.sendMessage("§e/ntsf reload §7- Reload config");
            sender.sendMessage("§e/ntsf spectator add <player> §7- Add player as spectator");
            sender.sendMessage("§e/ntsf spectator remove <player> §7- Remove spectator");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        if (subCommand.equals("reload")) {
            plugin.getTwistManager().loadConfig();
            sender.sendMessage("§aNotTheServersFault configuration reloaded.");
            return true;
        }
        
        if (subCommand.equals("spectator")) {
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /ntsf spectator <add|remove> <player>");
                return true;
            }
            String action = args[1].toLowerCase();
            String targetName = args[2].toLowerCase();

            TwistManager twistManager = plugin.getTwistManager();
            List<String> excludedPlayers = new ArrayList<>(plugin.getConfig().getStringList("settings.excluded-players"));
            
            if (action.equals("add")) {
                if (!excludedPlayers.contains(targetName)) {
                    excludedPlayers.add(targetName);
                    plugin.getConfig().set("settings.excluded-players", excludedPlayers);
                    plugin.saveConfig();
                    twistManager.loadConfig(); // Refresh
                    sender.sendMessage("§aAdded " + targetName + " as a spectator.");
                    Player target = Bukkit.getPlayerExact(targetName);
                    if (target != null) {
                        target.sendMessage("§eYou have been added as a Spectator.");
                    }
                } else {
                    sender.sendMessage("§c" + targetName + " is already a spectator.");
                }
            } else if (action.equals("remove")) {
                if (excludedPlayers.contains(targetName)) {
                    excludedPlayers.remove(targetName);
                    plugin.getConfig().set("settings.excluded-players", excludedPlayers);
                    plugin.saveConfig();
                    twistManager.loadConfig(); // Refresh
                    sender.sendMessage("§aRemoved " + targetName + " from spectators.");
                    Player target = Bukkit.getPlayerExact(targetName);
                    if (target != null) {
                        target.sendMessage("§eYou have been removed from Spectator list.");
                    }
                } else {
                    sender.sendMessage("§c" + targetName + " is not a spectator.");
                }
            } else {
                sender.sendMessage("§cUsage: /ntsf spectator <add|remove> <player>");
            }
            return true;
        }

        sender.sendMessage("§cUnknown subcommand. Usage: /ntsf <reload|spectator>");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("ntsf.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) completions.add("reload");
            if ("spectator".startsWith(args[0].toLowerCase())) completions.add("spectator");
            return completions;
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("spectator")) {
            List<String> completions = new ArrayList<>();
            if ("add".startsWith(args[1].toLowerCase())) completions.add("add");
            if ("remove".startsWith(args[1].toLowerCase())) completions.add("remove");
            return completions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("spectator")) {
            return null; // returning null auto-completes online player names
        }
        
        return Collections.emptyList();
    }
}