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

        if (args.length > 0 && args[0].equalsIgnoreCase("reload") && player.hasPermission("blackmarket.admin")) {
            plugin.getConfigManager().reload();
            plugin.getMessageManager().reload();
            player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.reload")));
            return true;
        }

        if (!plugin.getBlackMarketManager().isOpen()) {
            player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.market-closed")));
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
        }

        return completions;
    }
}