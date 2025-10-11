package com.Lino.blackMarket.listeners;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.gui.EditGUI;
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
import java.util.Map;

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

        if (clickedItem.getType() == Material.COMMAND_BLOCK && player.hasPermission("blackmarket.admin")) {
            player.closeInventory();
            EditGUI editGUI = new EditGUI(plugin, player);
            editGUI.open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            return;
        }

        if (clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.NETHER_STAR) {
            return;
        }

        BlackMarketItem item = getItemBySlot(event.getSlot());
        if (item == null) {
            return;
        }

        processPurchase(player, item);
    }

    private BlackMarketItem getItemBySlot(int slot) {
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
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
        if (plugin.getBlackMarketManager().getRemainingStock(item.getId()) <= 0) {
            player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("purchase.out-of-stock")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        boolean useLevels = plugin.useLevels();
        String coloredItemName = item.getDisplayName();

        if (useLevels) {
            int price = item.hasDiscount() ? item.getDiscountedExpPrice() : item.getExpPrice();
            if (player.getLevel() < price) {
                player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("purchase.insufficient-levels").replace("{price}", String.valueOf(price))));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            if (tryPurchase(player, item, coloredItemName, String.valueOf(price))) {
                player.setLevel(player.getLevel() - price);
            }
        } else {
            double price = item.hasDiscount() ? item.getDiscountedPrice() : item.getPrice();
            Economy economy = plugin.getEconomy();
            if (!economy.has(player, price)) {
                player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("purchase.insufficient-funds").replace("{price}", String.format("%.2f", price))));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            if (tryPurchase(player, item, coloredItemName, String.format("%.2f", price))) {
                economy.withdrawPlayer(player, price);
            }
        }
    }

    private boolean tryPurchase(Player player, BlackMarketItem item, String coloredItemName, String price) {
        if (!plugin.getBlackMarketManager().purchaseItem(item.getId(), 1)) {
            player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("purchase.error")));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 1.0f);
            return false;
        }

        // Se l'item non ha comandi, consegnalo al giocatore
        if (item.getCommands() == null || item.getCommands().isEmpty()) {
            giveItemToPlayer(player, item.getDisplayItem());
        }

        // Esegui sempre i comandi se presenti
        for (String command : item.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
        }

        String messagePath = plugin.useLevels() ? "purchase.success-levels" : "purchase.success";
        player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage(messagePath)
                .replace("{item}", coloredItemName)
                .replace("{price}", price)));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
        player.closeInventory();
        return true;
    }

    private void giveItemToPlayer(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
            player.sendMessage(ColorUtil.colorize("&cYour inventory was full, so the item was dropped at your feet!"));
        }
    }
}