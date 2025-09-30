package com.Lino.blackMarket.listeners;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.models.BlackMarketItem;
import com.Lino.blackMarket.utils.ColorUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GUIListener implements Listener {

    private final BlackMarket plugin;

    public GUIListener(BlackMarket plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.title"));

        if (!event.getView().getTitle().equals(title)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.NETHER_STAR) {
            return;
        }

        int slot = event.getSlot();
        BlackMarketItem item = getItemBySlot(slot);

        if (item == null) {
            return;
        }

        processPurchase(player, item);
    }

    private BlackMarketItem getItemBySlot(int slot) {
        int[] slots = {10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43};

        int index = -1;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) {
                index = i;
                break;
            }
        }

        if (index == -1) return null;

        List<BlackMarketItem> items = plugin.getBlackMarketManager().getTodayItems();

        if (index >= items.size()) return null;

        return items.get(index);
    }

    private void processPurchase(Player player, BlackMarketItem item) {
        int remainingStock = plugin.getBlackMarketManager().getRemainingStock(item.getId());

        if (remainingStock <= 0) {
            String msg = plugin.getMessageManager().getMessage("purchase.out-of-stock");
            if (msg != null && !msg.contains("not found")) {
                player.sendMessage(ColorUtil.colorize(msg));
            } else {
                player.sendMessage(ColorUtil.colorize("&cThis item is out of stock!"));
            }
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 0.9f);
            player.closeInventory();
            return;
        }

        double price = item.hasDiscount() ? item.getDiscountedPrice() : item.getPrice();
        boolean useLevels = plugin.useLevels();

        if (useLevels) {
            int levelPrice = (int) price;
            int playerLevels = player.getLevel();

            if (playerLevels < levelPrice) {
                String msg = plugin.getMessageManager().getMessage("purchase.insufficient-levels");
                if (msg != null && !msg.contains("not found")) {
                    player.sendMessage(ColorUtil.colorize(msg
                            .replace("{price}", String.valueOf(levelPrice))
                            .replace("{current}", String.valueOf(playerLevels))));
                } else {
                    player.sendMessage(ColorUtil.colorize("&cYou need &e" + levelPrice + " levels &cto purchase this item! You have &e" + playerLevels + " levels"));
                }
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 0.8f);
                return;
            }

            if (!plugin.getBlackMarketManager().purchaseItem(item.getId(), 1)) {
                String msg = plugin.getMessageManager().getMessage("purchase.error");
                if (msg != null && !msg.contains("not found")) {
                    player.sendMessage(ColorUtil.colorize(msg));
                } else {
                    player.sendMessage(ColorUtil.colorize("&cAn error occurred during purchase!"));
                }
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.8f);
                return;
            }

            player.setLevel(playerLevels - levelPrice);

            for (String command : item.getCommands()) {
                String finalCommand = command.replace("{player}", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            }

            String msg = plugin.getMessageManager().getMessage("purchase.success-levels");
            if (msg != null && !msg.contains("not found")) {
                player.sendMessage(ColorUtil.colorize(msg
                        .replace("{item}", item.getDisplayName())
                        .replace("{price}", String.valueOf(levelPrice))));
            } else {
                player.sendMessage(ColorUtil.colorize("&aSuccessfully purchased " + item.getDisplayName() + " &afor &e" + levelPrice + " levels"));
            }

        } else {
            Economy economy = plugin.getEconomy();

            if (!economy.has(player, price)) {
                String msg = plugin.getMessageManager().getMessage("purchase.insufficient-funds");
                if (msg != null && !msg.contains("not found")) {
                    player.sendMessage(ColorUtil.colorize(msg.replace("{price}", String.format("%.2f", price))));
                } else {
                    player.sendMessage(ColorUtil.colorize("&cYou need &e$" + String.format("%.2f", price) + " &cto purchase this item!"));
                }
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 0.8f);
                return;
            }

            if (!plugin.getBlackMarketManager().purchaseItem(item.getId(), 1)) {
                String msg = plugin.getMessageManager().getMessage("purchase.error");
                if (msg != null && !msg.contains("not found")) {
                    player.sendMessage(ColorUtil.colorize(msg));
                } else {
                    player.sendMessage(ColorUtil.colorize("&cAn error occurred during purchase!"));
                }
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.8f);
                return;
            }

            economy.withdrawPlayer(player, price);

            for (String command : item.getCommands()) {
                String finalCommand = command.replace("{player}", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            }

            String msg = plugin.getMessageManager().getMessage("purchase.success");
            if (msg != null && !msg.contains("not found")) {
                player.sendMessage(ColorUtil.colorize(msg
                        .replace("{item}", item.getDisplayName())
                        .replace("{price}", String.format("%.2f", price))));
            } else {
                player.sendMessage(ColorUtil.colorize("&aSuccessfully purchased " + item.getDisplayName() + " &afor &e$" + String.format("%.2f", price)));
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 0.8f);

        player.closeInventory();
    }
}