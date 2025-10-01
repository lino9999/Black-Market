package com.Lino.blackMarket.managers;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.models.BlackMarketItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {

    private final BlackMarket plugin;
    private FileConfiguration config;

    public ConfigManager(BlackMarket plugin) {
        this.plugin = plugin;
        reload();
        createDefaultConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    private void createDefaultConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!config.contains("settings")) {
            setDefaults();
        }
    }

    private void setDefaults() {
        config.set("settings.use-levels", false);
        config.set("settings.max-items-per-day", 9);
        config.set("settings.discount-chance", 0.25);
        config.set("settings.discount-min", 0.10);
        config.set("settings.discount-max", 0.50);

        config.set("settings.schedule-mode", "minecraft-night");
        config.set("settings.timezone", "UTC");
        config.set("settings.open-hours", Arrays.asList("20:00-23:59", "00:00-02:00"));

        config.set("items.diamond_sword.display-name", "<gradient:#00c6ff:#0072ff>&lDiamond Sword</gradient>");
        config.set("items.diamond_sword.material", "DIAMOND_SWORD");
        config.set("items.diamond_sword.price", 1000.0);
        config.set("items.diamond_sword.exp-price", 15);
        config.set("items.diamond_sword.stock", 3);
        config.set("items.diamond_sword.lore", Arrays.asList("&7A powerful weapon", "&7forged from diamonds"));
        config.set("items.diamond_sword.commands", Arrays.asList("give {player} diamond_sword 1"));

        config.set("items.god_apple.display-name", "<gradient:#FDC830:#F37335>&lEnchanted Golden Apple</gradient>");
        config.set("items.god_apple.material", "ENCHANTED_GOLDEN_APPLE");
        config.set("items.god_apple.price", 2500.0);
        config.set("items.god_apple.exp-price", 30);
        config.set("items.god_apple.stock", 1);
        config.set("items.god_apple.lore", Arrays.asList("&7The ultimate healing item", "&7Grants incredible powers"));
        config.set("items.god_apple.commands", Arrays.asList("give {player} enchanted_golden_apple 1"));

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
            double price = itemSection.getDouble("price", 1000.0);
            int expPrice = itemSection.getInt("exp-price", 20);
            int stock = itemSection.getInt("stock", 1);
            List<String> commands = itemSection.getStringList("commands");

            BlackMarketItem item = new BlackMarketItem(id, displayName, material, lore, price, expPrice, stock, commands);
            items.add(item);
        }
        return items;
    }
}