package com.Lino.blackMarket.commands;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.gui.BlackMarketGUI;
import com.Lino.blackMarket.utils.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BlackMarketCommand implements CommandExecutor, TabCompleter {

    private final BlackMarket plugin;

    public BlackMarketCommand(BlackMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.player-only")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("blackmarket.use")) {
            player.sendMessage(ColorUtil.colorize("&cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!player.hasPermission("blackmarket.admin")) {
                    player.sendMessage(ColorUtil.colorize("&cYou don't have permission to reload!"));
                    return true;
                }
                plugin.getConfigManager().reload();
                plugin.getMessageManager().reload();
                player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.reload")));
                return true;
            }

            if (args[0].equalsIgnoreCase("forceopen") && player.hasPermission("blackmarket.admin")) {
                BlackMarketGUI gui = new BlackMarketGUI(plugin, player);
                gui.open();
                player.sendMessage(ColorUtil.colorize("&aForced open Black Market GUI"));
                return true;
            }
        }

        if (!plugin.getBlackMarketManager().isOpen()) {
            String closedMsg = plugin.getMessageManager().getMessage("command.market-closed");
            String scheduleMode = plugin.getConfig().getString("settings.schedule-mode", "minecraft-night");

            if (scheduleMode.equalsIgnoreCase("real-time")) {
                List<String> schedules = plugin.getConfig().getStringList("settings.open-hours");
                if (!schedules.isEmpty()) {
                    closedMsg = closedMsg.replace("(13000-23000 ticks)", "(" + String.join(", ", schedules) + ")");
                }
            }

            player.sendMessage(ColorUtil.colorize(closedMsg));
            return true;
        }

        BlackMarketGUI gui = new BlackMarketGUI(plugin, player);
        gui.open();

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("blackmarket.admin")) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            if ("forceopen".startsWith(args[0].toLowerCase())) {
                completions.add("forceopen");
            }
        }

        return completions;
    }
}