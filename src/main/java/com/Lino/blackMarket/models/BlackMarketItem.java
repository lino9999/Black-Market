package com.Lino.blackMarket.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BlackMarketItem {

    private final String id;
    private final ItemStack itemStack; // The complete item with all NBT data
    private final double price;
    private final int expPrice;
    private final int stock;
    private final List<String> commands;
    private double discount = 0.0;

    public BlackMarketItem(String id, ItemStack itemStack, double price, int expPrice, int stock, List<String> commands) {
        this.id = id;
        this.itemStack = itemStack;
        this.price = price;
        this.expPrice = expPrice;
        this.stock = stock;
        this.commands = commands;
    }

    public ItemStack getDisplayItem() {
        return itemStack.clone();
    }

    public String getDisplayName() {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        // Fallback for items with no custom name
        return itemStack.getType().name().replace("_", " ").toLowerCase();
    }

    public List<String> getLore() {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && meta.hasLore()) {
            return meta.getLore();
        }
        return new ArrayList<>();
    }

    public String getId() { return id; }
    public double getPrice() { return price; }
    public int getExpPrice() { return expPrice; }
    public int getStock() { return stock; }
    public List<String> getCommands() { return commands; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public boolean hasDiscount() { return discount > 0; }
    public int getDiscountPercentage() { return (int) (discount * 100); }
    public double getDiscountedPrice() { return price * (1 - discount); }
    public int getDiscountedExpPrice() { return (int) (expPrice * (1 - discount)); }
}