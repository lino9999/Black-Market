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
import java.util.stream.Collectors;

public class BlackMarketCommand implements CommandExecutor, TabCompleter {

    private final BlackMarket plugin;

    public BlackMarketCommand(BlackMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                showHelp(sender, label);
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("blackmarket.admin")) {
                    sender.sendMessage(ColorUtil.colorize("&cYou don't have permission to reload!"));
                    return true;
                }
                plugin.getConfigManager().reload();
                plugin.getMessageManager().reload();
                sender.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.reload")));
                return true;
            }

            if (args[0].equalsIgnoreCase("forceopen")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.player-only")));
                    return true;
                }

                if (!sender.hasPermission("blackmarket.admin")) {
                    sender.sendMessage(ColorUtil.colorize("&cYou don't have permission to force open!"));
                    return true;
                }

                Player player = (Player) sender;
                BlackMarketGUI gui = new BlackMarketGUI(plugin, player);
                gui.open();
                player.sendMessage(ColorUtil.colorize("&aForced open Black Market GUI"));
                return true;
            }

            sender.sendMessage(ColorUtil.colorize("&cUnknown subcommand! Use &e/" + label + " help &cfor available commands."));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.player-only")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("blackmarket.use")) {
            player.sendMessage(ColorUtil.colorize("&cYou don't have permission to use this command!"));
            return true;
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

    private void showHelp(CommandSender sender, String label) {
        List<String> helpMessages = plugin.getMessageManager().getMessageList("command.help");

        if (helpMessages.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize("&gradient:#FF0080:#8000FF>&l[BLACK MARKET] &fHelp"));
            sender.sendMessage("");
            sender.sendMessage(ColorUtil.colorize("&e/" + label + " &7- Open the Black Market"));
            sender.sendMessage(ColorUtil.colorize("&e/" + label + " help &7- Show this help message"));

            if (sender.hasPermission("blackmarket.admin") && sender.isOp()) {
                sender.sendMessage(ColorUtil.colorize("&e/" + label + " reload &7- Reload configuration"));
                sender.sendMessage(ColorUtil.colorize("&e/" + label + " forceopen &7- Force open the market"));
            }

            sender.sendMessage("");
            sender.sendMessage(ColorUtil.colorize("&7Aliases: &f/bm, /blackm, /market"));
        } else {
            for (String line : helpMessages) {
                if (line.contains("{admin}")) {
                    if (sender.hasPermission("blackmarket.admin") && sender.isOp()) {
                        sender.sendMessage(ColorUtil.colorize(line.replace("{admin}", "")));
                    }
                } else {
                    sender.sendMessage(ColorUtil.colorize(line));
                }
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();

            subCommands.add("help");

            if (sender.hasPermission("blackmarket.admin")) {
                subCommands.add("reload");
                if (sender instanceof Player) {
                    subCommands.add("forceopen");
                }
            }

            String input = args[0].toLowerCase();
            completions = subCommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return completions;
    }
}