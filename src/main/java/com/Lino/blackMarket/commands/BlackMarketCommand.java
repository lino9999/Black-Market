package com.Lino.blackMarket.commands;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.gui.BlackMarketGUI;
import com.Lino.blackMarket.gui.EditGUI;
import com.Lino.blackMarket.utils.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "help":
                    showHelp(sender, label);
                    return true;

                case "reload":
                    if (!sender.hasPermission("blackmarket.admin")) {
                        sender.sendMessage(ColorUtil.colorize("&cYou don't have permission to do that."));
                        return true;
                    }
                    plugin.reloadConfig();
                    plugin.getConfigManager().reload();
                    plugin.getMessageManager().reload();
                    plugin.getBlackMarketManager().reloadItems();
                    plugin.getBlackMarketManager().stopScheduler();
                    plugin.getBlackMarketManager().startScheduler();
                    sender.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.reload")));
                    return true;

                case "forceopen":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.player-only")));
                        return true;
                    }
                    if (!sender.hasPermission("blackmarket.admin")) {
                        sender.sendMessage(ColorUtil.colorize("&cYou don't have permission to do that."));
                        return true;
                    }
                    new BlackMarketGUI(plugin, (Player) sender).open();
                    return true;

                case "edit":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.player-only")));
                        return true;
                    }
                    if (!sender.hasPermission("blackmarket.admin")) {
                        sender.sendMessage(ColorUtil.colorize("&cYou don't have permission to do that."));
                        return true;
                    }
                    new EditGUI(plugin, (Player) sender).open();
                    return true;

                case "forcenew":
                    if (!sender.hasPermission("blackmarket.admin")) {
                        sender.sendMessage(ColorUtil.colorize("&cYou don't have permission to do that."));
                        return true;
                    }
                    plugin.getBlackMarketManager().forceRefreshItems();
                    sender.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.forcenew")));
                    return true;
            }

            sender.sendMessage(ColorUtil.colorize("&cUnknown subcommand. Use /" + label + " help."));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.player-only")));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("blackmarket.use")) {
            player.sendMessage(ColorUtil.colorize("&cYou don't have permission to use this command."));
            return true;
        }

        if (!plugin.getBlackMarketManager().isOpen()) {
            String closedMsg = plugin.getMessageManager().getMessage("command.market-closed");
            ZonedDateTime nextOpening = plugin.getBlackMarketManager().getNextOpeningTime();
            if (nextOpening != null) {
                String timezone = plugin.getConfig().getString("settings.timezone", "UTC");
                ZoneId zoneId = ZoneId.of(timezone);
                Duration duration = Duration.between(ZonedDateTime.now(zoneId), nextOpening);

                long hours = duration.toHours();
                long minutes = duration.toMinutesPart();
                String countdown = "";
                if (hours > 0) countdown += hours + (hours == 1 ? " hour" : " hours");
                if (minutes > 0) {
                    if (!countdown.isEmpty()) countdown += " and ";
                    countdown += minutes + (minutes == 1 ? " minute" : " minutes");
                }
                if (countdown.isEmpty() && duration.toSeconds() > 0) countdown = "less than a minute";
                else if (countdown.isEmpty()) countdown = "any moment now";
                closedMsg = closedMsg.replace("{countdown}", countdown);
            } else {
                closedMsg = closedMsg.replace("{countdown}", "a configured time");
            }
            player.sendMessage(ColorUtil.colorize(closedMsg));
            return true;
        }

        new BlackMarketGUI(plugin, player).open();
        return true;
    }

    private void showHelp(CommandSender sender, String label) {
        List<String> helpMessages = plugin.getMessageManager().getMessageList("command.help");
        boolean hasAdminPerms = sender.hasPermission("blackmarket.admin");
        for (String line : helpMessages) {
            line = line.replace("{label}", label);
            if (line.contains("{admin}")) {
                if (hasAdminPerms) sender.sendMessage(ColorUtil.colorize(line.replace("{admin}", "")));
            } else {
                sender.sendMessage(ColorUtil.colorize(line));
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(List.of("help"));
            if (sender.hasPermission("blackmarket.admin")) {
                subCommands.addAll(Arrays.asList("reload", "edit", "forceopen", "forcenew"));
            }
            return subCommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}