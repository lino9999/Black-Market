package com.Lino.blackMarket.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BlackMarketItem {

    private final String id;
    private final String displayName;
    private final Material material;
    private final List<String> lore;
    private final double price;
    private final int stock;
    private final List<String> commands;
    private double discount = 0.0;

    public BlackMarketItem(String id, String displayName, Material material, List<String> lore,
                           double price, int stock, List<String> commands) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.lore = lore;
        this.price = price;
        this.stock = stock;
        this.commands = commands;
    }

    public ItemStack getDisplayItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public List<String> getLore() {
        return lore;
    }

    public double getPrice() {
        return price;
    }

    public double getDiscountedPrice() {
        return price * (1 - discount);
    }

    public int getStock() {
        return stock;
    }

    public List<String> getCommands() {
        return commands;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public boolean hasDiscount() {
        return discount > 0;
    }

    public int getDiscountPercentage() {
        return (int) (discount * 100);
    }
}