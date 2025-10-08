package com.Lino.blackMarket.managers;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.models.BlackMarketItem;
import com.Lino.blackMarket.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class BlackMarketManager {

    private final BlackMarket plugin;
    private boolean isOpen = false;
    private BukkitTask scheduler;
    private List<BlackMarketItem> todayItems;
    private Map<String, Integer> purchasedStocks;
    private long currentDay;
    private List<BlackMarketItem> allItemsCache;

    public BlackMarketManager(BlackMarket plugin) {
        this.plugin = plugin;
        this.purchasedStocks = new HashMap<>();
        this.todayItems = new ArrayList<>();
        this.currentDay = getDayNumber();
        reloadItems();
    }

    public void reloadItems() {
        this.allItemsCache = plugin.getConfigManager().getItems();
        loadTodayItems();
    }

    public void startScheduler() {
        startRealTimeScheduler();
    }

    private void startRealTimeScheduler() {
        if (scheduler != null) {
            scheduler.cancel();
        }
        scheduler = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String timezone = plugin.getConfig().getString("settings.timezone", "UTC");
            ZoneId zoneId;
            try {
                zoneId = ZoneId.of(timezone);
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid timezone '" + timezone + "' in config.yml. Defaulting to UTC.");
                zoneId = ZoneId.of("UTC");
            }

            ZonedDateTime now = ZonedDateTime.now(zoneId);
            LocalTime currentTime = now.toLocalTime();

            List<String> schedules = plugin.getConfig().getStringList("settings.open-hours");
            boolean shouldBeOpen = false;

            for (String schedule : schedules) {
                String[] parts = schedule.split("-");
                if (parts.length == 2) {
                    try {
                        LocalTime startTime = LocalTime.parse(parts[0]);
                        LocalTime endTime = LocalTime.parse(parts[1]);

                        if (startTime.isBefore(endTime)) {
                            if (!currentTime.isBefore(startTime) && currentTime.isBefore(endTime)) {
                                shouldBeOpen = true;
                                break;
                            }
                        } else {
                            if (!currentTime.isBefore(startTime) || currentTime.isBefore(endTime)) {
                                shouldBeOpen = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid time format in config.yml: " + schedule);
                    }
                }
            }

            if (shouldBeOpen && !isOpen) {
                openMarket();
            } else if (!shouldBeOpen && isOpen) {
                closeMarket();
            }

            long newDay = getDayNumber(now);
            if (newDay != currentDay) {
                currentDay = newDay;
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

    public ZonedDateTime getNextOpeningTime() {
        String timezone = plugin.getConfig().getString("settings.timezone", "UTC");
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalTime currentTime = now.toLocalTime();

        List<LocalTime> startTimes = new ArrayList<>();
        for (String schedule : plugin.getConfig().getStringList("settings.open-hours")) {
            try {
                startTimes.add(LocalTime.parse(schedule.split("-")[0]));
            } catch (Exception ignored) {}
        }
        startTimes.sort(Comparator.naturalOrder());

        for (LocalTime startTime : startTimes) {
            if (startTime.isAfter(currentTime)) {
                return now.with(startTime);
            }
        }

        if (!startTimes.isEmpty()) {
            return now.plusDays(1).with(startTimes.get(0));
        }

        return null;
    }

    private void openMarket() {
        isOpen = true;
        String openMessage = plugin.getMessageManager().getMessage("market.open");
        Bukkit.broadcastMessage(ColorUtil.colorize(openMessage));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.6f, 0.8f);
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.3f, 0.5f);
        }
    }

    private void closeMarket() {
        isOpen = false;
        String closeMessage = plugin.getMessageManager().getMessage("market.close");
        Bukkit.broadcastMessage(ColorUtil.colorize(closeMessage));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.5f, 0.6f);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.4f, 0.7f);
        }
    }

    private long getDayNumber() {
        String timezone = plugin.getConfig().getString("settings.timezone", "UTC");
        return getDayNumber(ZonedDateTime.now(ZoneId.of(timezone)));
    }

    private long getDayNumber(ZonedDateTime now) {
        return now.toEpochSecond() / 86400L;
    }

    private void loadTodayItems() {
        if (allItemsCache == null) {
            allItemsCache = plugin.getConfigManager().getItems();
        }

        boolean useLevels = plugin.useLevels();
        List<BlackMarketItem> availableItems = allItemsCache.stream()
                .filter(item -> useLevels ? item.getExpPrice() > 0 : item.getPrice() > 0)
                .collect(Collectors.toList());

        if (availableItems.isEmpty()) {
            todayItems = new ArrayList<>();
            return;
        }

        Random random = new Random(currentDay);
        Collections.shuffle(availableItems, random);

        int maxItems = plugin.getConfig().getInt("settings.max-items-per-day", 9);
        int itemCount = Math.min(maxItems, availableItems.size());
        todayItems = new ArrayList<>(availableItems.subList(0, itemCount));

        double discountChance = plugin.getConfig().getDouble("settings.discount-chance", 0.2);
        double discountMin = plugin.getConfig().getDouble("settings.discount-min", 0.05);
        double discountMax = plugin.getConfig().getDouble("settings.discount-max", 0.30);

        for (BlackMarketItem item : todayItems) {
            item.setDiscount(0);
            if (random.nextDouble() < discountChance) {
                double discount = discountMin + (discountMax - discountMin) * random.nextDouble();
                item.setDiscount(discount);
            }
        }
    }

    public boolean isOpen() { return isOpen; }
    public List<BlackMarketItem> getTodayItems() { return new ArrayList<>(todayItems); }

    public int getRemainingStock(String itemId) {
        BlackMarketItem item = getItemById(itemId);
        if (item == null) return 0;
        int purchased = purchasedStocks.getOrDefault(itemId, 0);
        return Math.max(0, item.getStock() - purchased);
    }

    public boolean purchaseItem(String itemId, int amount) {
        BlackMarketItem item = getItemById(itemId);
        if (item == null) return false;
        int remaining = getRemainingStock(itemId);
        if (remaining < amount) return false;
        purchasedStocks.put(itemId, purchasedStocks.getOrDefault(itemId, 0) + amount);
        plugin.getDatabaseManager().savePurchase(itemId, amount, currentDay);
        return true;
    }

    private BlackMarketItem getItemById(String id) {
        return todayItems.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
    }
}