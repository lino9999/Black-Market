package com.Lino.blackMarket.gui;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.models.BlackMarketItem;
import com.Lino.blackMarket.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EditGUI {

    private final BlackMarket plugin;
    private final Player player;
    private final Inventory inventory;

    public EditGUI(BlackMarket plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        String title = ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.title"));
        this.inventory = Bukkit.createInventory(null, 54, title);

        setupGUI();
    }

    private void setupGUI() {
        fillBorders();
        displayItems();
        createAddItemButton();
    }

    // Metodo open() ripristinato
    public void open() {
        player.openInventory(inventory);
    }

    private void fillBorders() {
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            border.setItemMeta(meta);
        }
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(45 + i, border);
        }
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }

    private void displayItems() {
        List<BlackMarketItem> items = plugin.getConfigManager().getItems();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        for (int i = 0; i < Math.min(items.size(), slots.length); i++) {
            BlackMarketItem bmItem = items.get(i);
            ItemStack displayItem = bmItem.getDisplayItem();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add(ColorUtil.colorize("&7(Left-click to edit price)"));
                lore.add(ColorUtil.colorize("&7(Right-click to remove)"));
                lore.add(ChatColor.BLACK + "ID: " + bmItem.getId());
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            inventory.setItem(slots[i], displayItem);
        }
    }

    private void createAddItemButton() {
        ItemStack addItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = addItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.add-item-title")));
            List<String> lore = plugin.getMessageManager().getMessageList("gui.edit.add-item-lore")
                    .stream()
                    .map(ColorUtil::colorize)
                    .collect(Collectors.toList());
            meta.setLore(lore);
            addItem.setItemMeta(meta);
        }
        inventory.setItem(49, addItem);
    }
}