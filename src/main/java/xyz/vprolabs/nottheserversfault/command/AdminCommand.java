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
            sender.sendMessage("§cOnly players can run this command.");
            return true;
        }

        if (!player.hasPermission("ntsf.admin") && !plugin.getTwistManager().isExcluded(player.getName())) {
            plugin.getAudiences().player(player).sendMessage(Component.text("§cNo permission."));
            return true;
        }

        if (args.length == 0) {
            plugin.getAudiences().player(player).sendMessage(Component.text("§cUsage: /ntsf <vanish>"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "vanish":
                adminManager.toggleVanish(player);
                break;
            default:
                plugin.getAudiences().player(player).sendMessage(Component.text("§cUnknown subcommand. Usage: /ntsf <vanish>"));
                break;
        }

        return true;
    }
}
