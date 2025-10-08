package com.Lino.blackMarket.gui;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.models.BlackMarketItem;
import com.Lino.blackMarket.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlackMarketGUI {

    private final BlackMarket plugin;
    private final Player player;
    private final Inventory inventory;
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat levelFormat = new DecimalFormat("#,##0");

    public BlackMarketGUI(BlackMarket plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        String title = ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.title"));
        this.inventory = Bukkit.createInventory(null, 54, title);

        setupGUI();
    }

    private void setupGUI() {
        fillBorders();
        displayItems();
        addAdminButton(); // Aggiunto pulsante admin
    }

    private void fillBorders() {
        ItemStack border = createBorderItem();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(45 + i, border);
        }

        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }

        ItemStack info = createInfoItem();
        inventory.setItem(4, info);
    }

    private void addAdminButton() {
        if (player.hasPermission("blackmarket.admin")) {
            ItemStack adminButton = new ItemStack(Material.COMMAND_BLOCK);
            ItemMeta meta = adminButton.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.admin-button-title")));
                List<String> lore = plugin.getMessageManager().getMessageList("gui.edit.admin-button-lore")
                        .stream().map(ColorUtil::colorize).collect(Collectors.toList());
                meta.setLore(lore);
                adminButton.setItemMeta(meta);
            }
            inventory.setItem(49, adminButton); // Bottom center
        }
    }

    private ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.info.title")));
            List<String> lore = new ArrayList<>();
            boolean useLevels = plugin.useLevels();

            for (String line : plugin.getMessageManager().getMessageList("gui.info.lore")) {
                String balance;
                if (useLevels) {
                    balance = levelFormat.format(player.getLevel()) + " levels";
                } else {
                    balance = "$" + priceFormat.format(plugin.getEconomy().getBalance(player));
                }
                lore.add(ColorUtil.colorize(line.replace("{balance}", balance)));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void displayItems() {
        List<BlackMarketItem> items = plugin.getBlackMarketManager().getTodayItems();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
        for (int i = 0; i < Math.min(items.size(), slots.length); i++) {
            BlackMarketItem bmItem = items.get(i);
            ItemStack displayItem = createMarketItem(bmItem);
            inventory.setItem(slots[i], displayItem);
        }
    }

    private ItemStack createMarketItem(BlackMarketItem bmItem) {
        ItemStack item = bmItem.getDisplayItem();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String displayName = meta.getDisplayName(); // Get name directly from custom item
            if (bmItem.hasDiscount()) {
                String discountText = plugin.getMessageManager().getMessage("gui.item.discount");
                displayName += " " + ColorUtil.colorize(discountText.replace("{percent}", String.valueOf(bmItem.getDiscountPercentage())));
            }
            meta.setDisplayName(displayName);

            List<String> lore = new ArrayList<>(meta.getLore() != null ? meta.getLore() : new ArrayList<>());
            lore.add("");

            boolean useLevels = plugin.useLevels();
            if (bmItem.hasDiscount()) {
                String originalPriceTemplate = plugin.getMessageManager().getMessage("gui.item.price-original");
                String discountedPriceTemplate = plugin.getMessageManager().getMessage("gui.item.price-discounted");
                String originalPriceText = useLevels ? levelFormat.format(bmItem.getExpPrice()) + " levels" : "$" + priceFormat.format(bmItem.getPrice());
                String discountedPriceText = useLevels ? levelFormat.format(bmItem.getDiscountedExpPrice()) + " levels" : "$" + priceFormat.format(bmItem.getDiscountedPrice());
                lore.add(ColorUtil.colorize(originalPriceTemplate.replace("{price}", originalPriceText)));
                lore.add(ColorUtil.colorize(discountedPriceTemplate.replace("{price}", discountedPriceText)));
            } else {
                String priceTemplate = plugin.getMessageManager().getMessage("gui.item.price");
                String priceText = useLevels ? levelFormat.format(bmItem.getExpPrice()) + " levels" : "$" + priceFormat.format(bmItem.getPrice());
                lore.add(ColorUtil.colorize(priceTemplate.replace("{price}", priceText)));
            }

            String stockMsg = plugin.getMessageManager().getMessage("gui.item.stock");
            lore.add(ColorUtil.colorize(stockMsg
                    .replace("{stock}", String.valueOf(plugin.getBlackMarketManager().getRemainingStock(bmItem.getId())))
                    .replace("{max}", String.valueOf(bmItem.getStock()))));
            lore.add("");

            if (plugin.getBlackMarketManager().getRemainingStock(bmItem.getId()) <= 0) {
                lore.add(ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.item.out-of-stock")));
            } else {
                lore.add(ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.item.click-to-buy")));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }
}