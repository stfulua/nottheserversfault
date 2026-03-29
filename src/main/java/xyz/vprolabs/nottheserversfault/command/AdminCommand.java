package xyz.vprolabs.nottheserversfault.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.manager.AdminManager;

public class AdminCommand implements CommandExecutor {

    private final AdminManager adminManager;
    private final NotTheServersFault plugin;

    public AdminCommand(NotTheServersFault plugin, AdminManager adminManager) {
        this.plugin = plugin;
        this.adminManager = adminManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("§cOnly players can run this command."));
            return true;
        }

        if (!player.hasPermission("ntsf.admin") && !plugin.getTwistManager().isExcluded(player.getName())) {
            player.sendMessage(Component.text("§cNo permission."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("§cUsage: /ntsf <herobrine|vanish>"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "herobrine":
                adminManager.toggleHerobrine(player);
                break;
            case "vanish":
                adminManager.toggleVanish(player);
                break;
            default:
                player.sendMessage(Component.text("§cUnknown subcommand. Usage: /ntsf <herobrine|vanish>"));
                break;
        }

        return true;
    }
}
