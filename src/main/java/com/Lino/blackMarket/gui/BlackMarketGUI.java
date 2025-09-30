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

    private ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize("&8"));
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
                if (useLevels) {
                    line = line.replace("{balance}", levelFormat.format(player.getLevel()) + " levels");
                } else {
                    line = line.replace("{balance}", priceFormat.format(plugin.getEconomy().getBalance(player)));
                }
                lore.add(ColorUtil.colorize(line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void displayItems() {
        List<BlackMarketItem> items = plugin.getBlackMarketManager().getTodayItems();

        int[] slots = {10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43};

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
            String displayName = ColorUtil.colorize(bmItem.getDisplayName());

            if (bmItem.hasDiscount()) {
                String discountText = plugin.getMessageManager().getMessage("gui.item.discount");
                if (discountText != null && !discountText.contains("not found")) {
                    displayName += ColorUtil.colorize(" " + discountText
                            .replace("{percent}", String.valueOf(bmItem.getDiscountPercentage())));
                } else {
                    displayName += ColorUtil.colorize(" &a[-" + bmItem.getDiscountPercentage() + "%]");
                }
            }

            meta.setDisplayName(displayName);

            List<String> lore = new ArrayList<>();
            for (String line : bmItem.getLore()) {
                lore.add(ColorUtil.colorize(line));
            }

            lore.add("");

            int remainingStock = plugin.getBlackMarketManager().getRemainingStock(bmItem.getId());
            boolean isOutOfStock = remainingStock <= 0;
            boolean useLevels = plugin.useLevels();

            if (bmItem.hasDiscount()) {
                String originalPrice = plugin.getMessageManager().getMessage("gui.item.price-original");
                if (originalPrice != null && !originalPrice.contains("not found")) {
                    String priceText = useLevels ?
                            levelFormat.format((int)bmItem.getPrice()) + " levels" :
                            "$" + priceFormat.format(bmItem.getPrice());
                    lore.add(ColorUtil.colorize(originalPrice
                            .replace("{price}", priceText)));
                } else {
                    String priceText = useLevels ?
                            levelFormat.format((int)bmItem.getPrice()) + " levels" :
                            "$" + priceFormat.format(bmItem.getPrice());
                    lore.add(ColorUtil.colorize("&6Price: &c&m" + priceText));
                }

                String discountedPrice = plugin.getMessageManager().getMessage("gui.item.price-discounted");
                if (discountedPrice != null && !discountedPrice.contains("not found")) {
                    String priceText = useLevels ?
                            levelFormat.format((int)bmItem.getDiscountedPrice()) + " levels" :
                            "$" + priceFormat.format(bmItem.getDiscountedPrice());
                    lore.add(ColorUtil.colorize(discountedPrice
                            .replace("{price}", priceText)));
                } else {
                    String priceText = useLevels ?
                            levelFormat.format((int)bmItem.getDiscountedPrice()) + " levels" :
                            "$" + priceFormat.format(bmItem.getDiscountedPrice());
                    lore.add(ColorUtil.colorize("&6Discounted: &a" + priceText));
                }
            } else {
                String price = plugin.getMessageManager().getMessage("gui.item.price");
                if (price != null && !price.contains("not found")) {
                    String priceText = useLevels ?
                            levelFormat.format((int)bmItem.getPrice()) + " levels" :
                            "$" + priceFormat.format(bmItem.getPrice());
                    lore.add(ColorUtil.colorize(price
                            .replace("{price}", priceText)));
                } else {
                    String priceText = useLevels ?
                            levelFormat.format((int)bmItem.getPrice()) + " levels" :
                            "$" + priceFormat.format(bmItem.getPrice());
                    lore.add(ColorUtil.colorize("&6Price: &e" + priceText));
                }
            }

            String stockMsg = plugin.getMessageManager().getMessage("gui.item.stock");
            if (stockMsg != null && !stockMsg.contains("not found")) {
                lore.add(ColorUtil.colorize(stockMsg
                        .replace("{stock}", String.valueOf(remainingStock))
                        .replace("{max}", String.valueOf(bmItem.getStock()))));
            } else {
                lore.add(ColorUtil.colorize("&7Stock: &f" + remainingStock + "/" + bmItem.getStock()));
            }

            lore.add("");

            if (isOutOfStock) {
                String outOfStockMsg = plugin.getMessageManager().getMessage("gui.item.out-of-stock");
                if (outOfStockMsg != null && !outOfStockMsg.contains("not found")) {
                    lore.add(ColorUtil.colorize(outOfStockMsg));
                } else {
                    lore.add(ColorUtil.colorize("&c&lOut of Stock!"));
                }
            } else {
                String clickMsg = plugin.getMessageManager().getMessage("gui.item.click-to-buy");
                if (clickMsg != null && !clickMsg.contains("not found")) {
                    lore.add(ColorUtil.colorize(clickMsg));
                } else {
                    lore.add(ColorUtil.colorize("&a&lClick to purchase!"));
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }
}