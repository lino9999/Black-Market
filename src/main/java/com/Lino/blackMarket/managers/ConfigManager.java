package com.Lino.blackMarket.managers;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.models.BlackMarketItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final BlackMarket plugin;
    private FileConfiguration config;

    public ConfigManager(BlackMarket plugin) {
        this.plugin = plugin;
        createDefaultConfig();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    private void createDefaultConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!plugin.getConfig().contains("settings")) {
            setDefaults();
        }
    }

    private void setDefaults() {
        config.set("settings.max-items-per-day", 9);
        config.set("settings.discount-chance", 0.25);
        config.set("settings.discount-min", 0.10);
        config.set("settings.discount-max", 0.50);

        config.set("items.diamond_sword.display-name", "&b&lDiamond Sword");
        config.set("items.diamond_sword.material", "DIAMOND_SWORD");
        config.set("items.diamond_sword.price", 1500.0);
        config.set("items.diamond_sword.stock", 3);
        List<String> lore = new ArrayList<>();
        lore.add("&7A powerful weapon");
        lore.add("&7forged from diamonds");
        config.set("items.diamond_sword.lore", lore);
        List<String> commands = new ArrayList<>();
        commands.add("give {player} diamond_sword 1");
        config.set("items.diamond_sword.commands", commands);

        config.set("items.god_apple.display-name", "&6&lEnchanted Golden Apple");
        config.set("items.god_apple.material", "ENCHANTED_GOLDEN_APPLE");
        config.set("items.god_apple.price", 5000.0);
        config.set("items.god_apple.stock", 1);
        lore = new ArrayList<>();
        lore.add("&7The ultimate healing item");
        lore.add("&7Grants incredible powers");
        config.set("items.god_apple.lore", lore);
        commands = new ArrayList<>();
        commands.add("give {player} enchanted_golden_apple 1");
        config.set("items.god_apple.commands", commands);

        plugin.saveConfig();
    }

    public List<BlackMarketItem> getItems() {
        List<BlackMarketItem> items = new ArrayList<>();

        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) return items;

        for (String id : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(id);
            if (itemSection == null) continue;

            String displayName = itemSection.getString("display-name", "&cUnknown Item");
            String materialName = itemSection.getString("material", "STONE");
            Material material = Material.getMaterial(materialName);

            if (material == null) {
                material = Material.STONE;
            }

            List<String> lore = itemSection.getStringList("lore");
            double price = itemSection.getDouble("price", 100.0);
            int stock = itemSection.getInt("stock", 1);

            List<String> commands = itemSection.getStringList("commands");
            if (commands.isEmpty() && itemSection.contains("command")) {
                Object commandObj = itemSection.get("command");
                if (commandObj instanceof String) {
                    commands.add((String) commandObj);
                } else if (commandObj instanceof List) {
                    commands = (List<String>) commandObj;
                }
            }

            BlackMarketItem item = new BlackMarketItem(id, displayName, material, lore, price, stock, commands);
            items.add(item);
        }

        return items;
    }
}