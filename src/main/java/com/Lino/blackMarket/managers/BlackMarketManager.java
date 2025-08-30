package com.Lino.blackMarket.managers;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.models.BlackMarketItem;
import com.Lino.blackMarket.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BlackMarketManager {

    private final BlackMarket plugin;
    private boolean isOpen = false;
    private BukkitTask scheduler;
    private List<BlackMarketItem> todayItems;
    private Map<String, Integer> purchasedStocks;
    private long currentDay;

    public BlackMarketManager(BlackMarket plugin) {
        this.plugin = plugin;
        this.purchasedStocks = new HashMap<>();
        this.todayItems = new ArrayList<>();
        this.currentDay = getDayNumber();
        loadTodayItems();
    }

    public void startScheduler() {
        scheduler = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            World world = Bukkit.getWorlds().get(0);
            long time = world.getTime();

            if (time >= 13000 && time < 23000 && !isOpen) {
                openMarket();
            } else if ((time >= 23000 || time < 13000) && isOpen) {
                closeMarket();
            }

            long newDay = getDayNumber();
            if (newDay != currentDay) {
                currentDay = newDay;
                loadTodayItems();
                purchasedStocks.clear();
            }
        }, 0L, 100L);
    }

    public void stopScheduler() {
        if (scheduler != null) {
            scheduler.cancel();
        }
    }

    private void openMarket() {
        isOpen = true;
        String openMessage = plugin.getMessageManager().getMessage("market.open");
        if (openMessage != null && !openMessage.contains("not found")) {
            Bukkit.broadcastMessage(ColorUtil.colorize(openMessage));
        } else {
            Bukkit.broadcastMessage(ColorUtil.colorize("&a&l[BLACK MARKET] &fThe Black Market is now OPEN!"));
        }
    }

    private void closeMarket() {
        isOpen = false;
        String closeMessage = plugin.getMessageManager().getMessage("market.close");
        if (closeMessage != null && !closeMessage.contains("not found")) {
            Bukkit.broadcastMessage(ColorUtil.colorize(closeMessage));
        } else {
            Bukkit.broadcastMessage(ColorUtil.colorize("&c&l[BLACK MARKET] &fThe Black Market is now CLOSED!"));
        }
    }

    private long getDayNumber() {
        World world = Bukkit.getWorlds().get(0);
        return world.getFullTime() / 24000L;
    }

    private void loadTodayItems() {
        List<BlackMarketItem> allItems = plugin.getConfigManager().getItems();
        if (allItems.isEmpty()) {
            todayItems = new ArrayList<>();
            return;
        }

        Random random = new Random(currentDay);
        Collections.shuffle(allItems, random);

        int maxItems = plugin.getConfig().getInt("settings.max-items-per-day", 9);
        int itemCount = Math.min(maxItems, allItems.size());

        todayItems = new ArrayList<>(allItems.subList(0, itemCount));

        double discountChance = plugin.getConfig().getDouble("settings.discount-chance", 0.2);
        double discountMin = plugin.getConfig().getDouble("settings.discount-min", 0.05);
        double discountMax = plugin.getConfig().getDouble("settings.discount-max", 0.30);

        for (BlackMarketItem item : todayItems) {
            if (random.nextDouble() < discountChance) {
                double discount = discountMin + (discountMax - discountMin) * random.nextDouble();
                item.setDiscount(discount);
            }
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public List<BlackMarketItem> getTodayItems() {
        return new ArrayList<>(todayItems);
    }

    public int getRemainingStock(String itemId) {
        BlackMarketItem item = getItemById(itemId);
        if (item == null) return 0;

        int purchased = purchasedStocks.getOrDefault(itemId, 0);
        return Math.max(0, item.getStock() - purchased);
    }

    public boolean purchaseItem(String itemId, int amount) {
        BlackMarketItem item = getItemById(itemId);
        if (item == null) return false;

        int purchased = purchasedStocks.getOrDefault(itemId, 0);
        int remaining = item.getStock() - purchased;

        if (remaining < amount) return false;

        purchasedStocks.put(itemId, purchased + amount);
        plugin.getDatabaseManager().savePurchase(itemId, amount, currentDay);
        return true;
    }

    private BlackMarketItem getItemById(String id) {
        for (BlackMarketItem item : todayItems) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }
}