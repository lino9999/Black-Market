package com.Lino.blackMarket.managers;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.models.BlackMarketItem;
import com.Lino.blackMarket.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        String scheduleMode = plugin.getConfig().getString("settings.schedule-mode", "minecraft-night");
        if (scheduleMode.equalsIgnoreCase("minecraft-night")) {
            startMinecraftScheduler();
        } else if (scheduleMode.equalsIgnoreCase("real-time")) {
            startRealTimeScheduler();
        }
    }

    private void startMinecraftScheduler() {
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

    private void startRealTimeScheduler() {
        scheduler = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String timezone = plugin.getConfig().getString("settings.timezone", "UTC");
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            LocalTime currentTime = now.toLocalTime();

            List<String> schedules = plugin.getConfig().getStringList("settings.open-hours");
            boolean shouldBeOpen = false;

            for (String schedule : schedules) {
                String[] parts = schedule.split("-");
                if (parts.length == 2) {
                    LocalTime startTime = LocalTime.parse(parts[0]);
                    LocalTime endTime = LocalTime.parse(parts[1]);

                    if (startTime.isBefore(endTime)) {
                        if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                            shouldBeOpen = true;
                            break;
                        }
                    } else {
                        if (currentTime.isAfter(startTime) || currentTime.isBefore(endTime)) {
                            shouldBeOpen = true;
                            break;
                        }
                    }
                }
            }

            if (shouldBeOpen && !isOpen) {
                openMarket();
            } else if (!shouldBeOpen && isOpen) {
                closeMarket();
            }

            int currentDayOfYear = now.getDayOfYear();
            if (currentDayOfYear != currentDay) {
                currentDay = currentDayOfYear;
                loadTodayItems();
                purchasedStocks.clear();
            }
        }, 0L, 200L);
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

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.6f, 0.8f);
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.3f, 0.5f);
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

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.5f, 0.6f);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.4f, 0.7f);
        }
    }

    private long getDayNumber() {
        String scheduleMode = plugin.getConfig().getString("settings.schedule-mode", "minecraft-night");
        if (scheduleMode.equalsIgnoreCase("minecraft-night")) {
            World world = Bukkit.getWorlds().get(0);
            return world.getFullTime() / 24000L;
        } else {
            String timezone = plugin.getConfig().getString("settings.timezone", "UTC");
            ZoneId zoneId = ZoneId.of(timezone);
            return ZonedDateTime.now(zoneId).getDayOfYear();
        }
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