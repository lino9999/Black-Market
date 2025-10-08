package com.Lino.blackMarket.managers;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.models.BlackMarketItem;
import com.Lino.blackMarket.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConfigManager {

    private final BlackMarket plugin;
    private FileConfiguration config;

    public ConfigManager(BlackMarket plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public List<BlackMarketItem> getItems() {
        List<BlackMarketItem> items = new ArrayList<>();
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) return items;

        for (String id : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(id);
            if (itemSection == null) continue;

            ItemStack itemStack = itemSection.getItemStack("item");

            if (itemStack == null && itemSection.contains("material")) {
                String materialName = itemSection.getString("material", "STONE");
                Material material = Material.getMaterial(materialName);
                if (material == null) material = Material.STONE;
                itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ColorUtil.colorize(itemSection.getString("display-name")));
                    List<String> lore = itemSection.getStringList("lore");
                    meta.setLore(lore.stream().map(ColorUtil::colorize).collect(Collectors.toList()));
                    itemStack.setItemMeta(meta);
                }
            }

            if (itemStack == null) continue;

            double price = itemSection.getDouble("price", 0.0);
            int expPrice = itemSection.getInt("exp-price", 0);
            int stock = itemSection.getInt("stock", 1);
            List<String> commands = itemSection.getStringList("commands");

            BlackMarketItem bmItem = new BlackMarketItem(id, itemStack, price, expPrice, stock, commands);
            items.add(bmItem);
        }
        return items;
    }

    public void addItem(ItemStack item, String currency, String price) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String path = "items." + id;

        config.set(path + ".item", item);
        config.set(path + ".price", 0.0);
        config.set(path + ".exp-price", 0);
        updateItemPrice(id, currency, price);
        config.set(path + ".stock", 1);
        config.set(path + ".commands", new ArrayList<String>());
        plugin.saveConfig();
    }

    public void updateItemPrice(String itemId, String currency, String price) {
        String path = "items." + itemId;
        if (config.getConfigurationSection(path) == null) return;

        if ("vault".equals(currency)) {
            config.set(path + ".price", Double.parseDouble(price));
        } else {
            config.set(path + ".exp-price", (int) Double.parseDouble(price));
        }
        plugin.saveConfig();
    }

    public void removeItem(String itemId) {
        config.set("items." + itemId, null);
        plugin.saveConfig();
    }
}