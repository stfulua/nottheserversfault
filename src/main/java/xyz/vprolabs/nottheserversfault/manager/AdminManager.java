package xyz.vprolabs.nottheserversfault.manager;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import xyz.vprolabs.nottheserversfault.NotTheServersFault;
import xyz.vprolabs.nottheserversfault.manager.herobrine.HerobrineSkin;
import xyz.vprolabs.nottheserversfault.util.TargetUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminManager implements Listener {

    private final NotTheServersFault plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, PlayerProfile> originalProfiles = new HashMap<>();
    private boolean isHerobrine = false;
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

    public void toggleHerobrine(Player player) {
        isHerobrine = !isHerobrine;
        if (isHerobrine) {
            // Store original profile BEFORE changing
            originalProfiles.put(player.getUniqueId(), player.getPlayerProfile());

            PlayerProfile profile = player.getPlayerProfile();
            profile.setProperty(new ProfileProperty("textures", HerobrineSkin.TEXTURE, HerobrineSkin.SIGNATURE));
            player.setPlayerProfile(profile);

            player.sendMessage(Component.text("§aYou are now Herobrine!"));
            player.setGameMode(GameMode.CREATIVE);
            setVanish(player, false);
            giveWands(player);
        } else {
            player.sendMessage(Component.text("§cYou are no longer Herobrine!"));
            
            // Revert skin
            PlayerProfile original = originalProfiles.remove(player.getUniqueId());
            if (original != null) {
                player.setPlayerProfile(original);
            } else {
                player.setPlayerProfile(Bukkit.createProfile(player.getUniqueId(), player.getName()));
            }

            setVanish(player, true);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
        }
    }

    public void toggleVanish(Player player) {
        setVanish(player, !isVanished);
        player.sendMessage(Component.text(isVanished ? "§aYou are now vanished!" : "§cYou are now visible!"));
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

    private Player currentManualTarget = null;
    private boolean affectAll = false;

    private void giveWands(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(0, createWand(Material.BLAZE_ROD, "<red><bold>Wand of Chaos", 
            "<gray>Right-Click to force an <white>Inventory Shuffle <gray>on the target."));
        player.getInventory().setItem(1, createWand(Material.STICK, "<gold><bold>Wand of Smite", 
            "<gray>Right-Click to strike <white>Lightning <gray>near the target."));
        player.getInventory().setItem(2, createWand(Material.BONE, "<dark_purple><bold>Wand of Paranoia", 
            "<gray>Right-Click to play <white>Creeper Hiss <gray>behind the target."));
        player.getInventory().setItem(3, createWand(Material.RECOVERY_COMPASS, "<aqua><bold>Wand of Targeting", 
            "<gray>Left-Click: Cycle Target | Right-Click: Toggle 'All' Mode."));
        updateTargetLore(player);
    }

    private void updateTargetLore(Player player) {
        ItemStack item = player.getInventory().getItem(3);
        if (item == null || item.getType() != Material.RECOVERY_COMPASS) return;
        ItemMeta meta = item.getItemMeta();
        String targetName = affectAll ? "ALL PLAYERS" : (currentManualTarget == null ? "RANDOM" : currentManualTarget.getName());
        meta.lore(List.of(
            miniMessage.deserialize("<dark_gray>Server Console Item").decoration(TextDecoration.ITALIC, false),
            Component.empty(),
            miniMessage.deserialize("<gray>Current Target: <yellow>" + targetName).decoration(TextDecoration.ITALIC, false),
            miniMessage.deserialize("<gray>Left-Click: Cycle Target | Right-Click: Toggle 'All' Mode.").decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
    }

    private ItemStack createWand(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(miniMessage.deserialize(name).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
            miniMessage.deserialize("<dark_gray>Server Console Item").decoration(TextDecoration.ITALIC, false),
            Component.empty(),
            miniMessage.deserialize(lore).decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onWandUse(PlayerInteractEvent event) {
        if (!isExcluded(event.getPlayer()) || !isHerobrine) return;
        
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        Material type = item.getType();

        if (type == Material.RECOVERY_COMPASS) {
            event.setCancelled(true);
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                cycleTarget(event.getPlayer());
            } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                affectAll = !affectAll;
                event.getPlayer().sendMessage("§a[Console] Affect All Mode: " + (affectAll ? "§6ON" : "§cOFF"));
                updateTargetLore(event.getPlayer());
            }
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        List<Player> targets = getActiveTargets();
        if (targets.isEmpty()) return;

        if (type == Material.BLAZE_ROD) {
            targets.forEach(t -> plugin.getInventoryShuffleManager().shuffleManual(t));
            event.getPlayer().sendMessage("§a[Console] Triggered Shuffle!");
        } else if (type == Material.STICK) {
            targets.forEach(t -> t.getWorld().strikeLightningEffect(t.getLocation().add(3, 0, 3)));
            event.getPlayer().sendMessage("§a[Console] Triggered Smite!");
        } else if (type == Material.BONE) {
            targets.forEach(t -> t.playSound(t.getLocation().subtract(t.getLocation().getDirection()), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 0.5f));
            event.getPlayer().sendMessage("§a[Console] Triggered Paranoia!");
        }
    }

    private List<Player> getActiveTargets() {
        if (affectAll) {
            return Bukkit.getOnlinePlayers().stream().filter(TargetUtil::isTarget).map(p -> (Player) p).toList();
        }
        if (currentManualTarget != null && currentManualTarget.isOnline() && TargetUtil.isTarget(currentManualTarget)) {
            return List.of(currentManualTarget);
        }
        return TargetUtil.findTarget(Bukkit.getServer()).map(List::of).orElse(List.of());
    }

    private void cycleTarget(Player admin) {
        List<Player> players = Bukkit.getOnlinePlayers().stream().filter(TargetUtil::isTarget).map(p -> (Player) p).toList();
        if (players.isEmpty()) {
            currentManualTarget = null;
        } else {
            int index = (currentManualTarget == null) ? 0 : players.indexOf(currentManualTarget) + 1;
            if (index >= players.size()) index = 0;
            currentManualTarget = players.get(index);
        }
        admin.sendMessage("§a[Console] Target set to: §6" + (currentManualTarget == null ? "NONE" : currentManualTarget.getName()));
        updateTargetLore(admin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isExcluded(player)) {
            event.joinMessage(null);
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
            event.quitMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (isExcluded(event.getPlayer())) {
            event.message(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message()).toLowerCase();
        for (String excluded : plugin.getTwistManager().getExcludedPlayers()) {
            if (plainMessage.contains(excluded.toLowerCase())) {
                event.setCancelled(true);
                return;
            }
        }
        if (plainMessage.contains("herobrine")) {
            event.setCancelled(true);
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
