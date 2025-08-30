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

            if (args[0].equalsIgnoreCase("testcolors") && player.hasPermission("blackmarket.admin")) {
                player.sendMessage(ColorUtil.colorize("&aTesting color codes:"));
                player.sendMessage(ColorUtil.colorize("&1Dark Blue &2Dark Green &3Dark Aqua &4Dark Red"));
                player.sendMessage(ColorUtil.colorize("&5Purple &6Gold &7Gray &8Dark Gray"));
                player.sendMessage(ColorUtil.colorize("&9Blue &aGreen &bAqua &cRed"));
                player.sendMessage(ColorUtil.colorize("&dPink &eYellow &fWhite"));
                player.sendMessage(ColorUtil.colorize("&l&6Bold Gold &r&o&bItalic Aqua"));
                player.sendMessage(ColorUtil.colorize("&#FF0080Hex Color Test"));
                player.sendMessage(ColorUtil.colorize("&gradient:#FF0000:#00FF00>Red to Green Gradient"));
                player.sendMessage(ColorUtil.colorize("&gradient:#FF0080:#8000FF>&l[BLACK MARKET]"));
                player.sendMessage(ColorUtil.colorize("&gradient:#FFD700:#FF0000>Gold to Red Gradient Test"));
                return true;
            }
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
            if ("forceopen".startsWith(args[0].toLowerCase())) {
                completions.add("forceopen");
            }
            if ("testcolors".startsWith(args[0].toLowerCase())) {
                completions.add("testcolors");
            }
        }

        return completions;
    }
}