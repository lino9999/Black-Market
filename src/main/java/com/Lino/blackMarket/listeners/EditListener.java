package com.Lino.blackMarket.listeners;

import com.Lino.blackMarket.BlackMarket;
import com.Lino.blackMarket.gui.EditGUI;
import com.Lino.blackMarket.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditListener implements Listener {

    private final BlackMarket plugin;
    private final Map<UUID, ItemStack> itemsToAdd = new HashMap<>();
    private final Map<UUID, String> currencyChoice = new HashMap<>();
    private final Map<UUID, String> editingItemId = new HashMap<>();
    private final Map<UUID, String> removingItemId = new HashMap<>();

    public EditListener(BlackMarket plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        String editTitle = ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.title"));
        String addTitle = ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.add-item-title"));
        String currencyTitle = ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.currency-choice-title"));
        String removeConfirmTitle = ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.remove-confirm-title"));

        if (title.equals(editTitle)) {
            handleEditGUIClick(event, player);
        } else if (title.equals(addTitle)) {
            handleAddItemGUIClick(event, player);
        } else if (title.equals(currencyTitle)) {
            handleCurrencyGUIClick(event, player);
        } else if (title.equals(removeConfirmTitle)) {
            handleRemoveConfirmGUIClick(event, player);
        }
    }

    private String getItemIdFromLore(ItemStack item) {
        if (item == null || item.getItemMeta() == null || item.getItemMeta().getLore() == null) {
            return null;
        }
        for (String line : item.getItemMeta().getLore()) {
            String strippedLine = ChatColor.stripColor(line);
            if (strippedLine.startsWith("ID: ")) {
                return strippedLine.substring(4);
            }
        }
        return null;
    }


    private void handleEditGUIClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();

        if (event.getSlot() == 49) { // Add item button
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            openAddItemGUI(player);
            return;
        }

        if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }

        String itemId = getItemIdFromLore(clickedItem);
        if (itemId == null) return;

        if (event.getClick() == ClickType.LEFT) { // Edit Price
            editingItemId.put(player.getUniqueId(), itemId);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            openCurrencyChoiceGUI(player);
        } else if (event.getClick() == ClickType.RIGHT) { // Remove Item
            removingItemId.put(player.getUniqueId(), itemId);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            openRemoveConfirmGUI(player, clickedItem);
        }
    }

    private void handleAddItemGUIClick(InventoryClickEvent event, Player player) {
        if (event.getSlot() == 22) { // Confirm button
            event.setCancelled(true);
            ItemStack item = event.getInventory().getItem(13);
            if (item != null && item.getType() != Material.AIR) {
                itemsToAdd.put(player.getUniqueId(), item.clone());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                openCurrencyChoiceGUI(player);
            } else {
                player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.edit.no-item-in-slot")));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.5f);
            }
            return;
        }
        if (event.getClickedInventory() == event.getView().getTopInventory() && event.getSlot() != 13) {
            event.setCancelled(true);
        }
    }

    private void handleCurrencyGUIClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        String currency = null;
        if (event.getSlot() == 20) currency = "vault";
        if (event.getSlot() == 24) currency = "levels";

        if (currency != null) {
            currencyChoice.put(player.getUniqueId(), currency);
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
            player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.edit.enter-price-chat")));
        }
    }

    private void handleRemoveConfirmGUIClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        String itemId = removingItemId.remove(player.getUniqueId());
        if (itemId == null) {
            player.closeInventory();
            return;
        }

        if (event.getSlot() == 11) { // Confirm
            plugin.getConfigManager().removeItem(itemId);
            plugin.getConfigManager().reload();
            plugin.getBlackMarketManager().reloadItems();
            player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.edit.item-removed")));
            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.5f);
            player.closeInventory();
            new EditGUI(plugin, player); // Re-open edit GUI
        } else if (event.getSlot() == 15) { // Cancel
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
            player.closeInventory();
            new EditGUI(plugin, player); // Re-open edit GUI
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (currencyChoice.containsKey(playerId)) {
            event.setCancelled(true);
            String message = event.getMessage();

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (message.equalsIgnoreCase("cancel")) {
                    clearState(playerId);
                    player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.edit.cancelled")));
                    return;
                }
                try {
                    double price = Double.parseDouble(message);
                    if (price <= 0) {
                        player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.edit.invalid-price")));
                        return;
                    }

                    String currency = currencyChoice.get(playerId);
                    String itemIdToEdit = editingItemId.get(playerId);

                    if (itemIdToEdit != null) {
                        plugin.getConfigManager().updateItemPrice(itemIdToEdit, currency, String.valueOf(price));
                        player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.edit.item-price-updated")));
                    } else {
                        ItemStack item = itemsToAdd.get(playerId);
                        plugin.getConfigManager().addItem(item, currency, String.valueOf(price));
                        player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.edit.item-added")));
                    }

                    plugin.getConfigManager().reload();
                    plugin.getBlackMarketManager().reloadItems();
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                    clearState(playerId);

                } catch (NumberFormatException e) {
                    player.sendMessage(ColorUtil.colorize(plugin.getMessageManager().getMessage("command.edit.invalid-price")));
                }
            });
        }
    }

    private void clearState(UUID playerId) {
        itemsToAdd.remove(playerId);
        currencyChoice.remove(playerId);
        editingItemId.remove(playerId);
        removingItemId.remove(playerId);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearState(event.getPlayer().getUniqueId());
    }

    private void openAddItemGUI(Player player) {
        String title = ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.add-item-title"));
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }
        gui.setItem(13, null);

        ItemStack confirm = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = confirm.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.confirm")));
            confirm.setItemMeta(meta);
        }
        gui.setItem(22, confirm);

        player.openInventory(gui);
    }

    private void openCurrencyChoiceGUI(Player player) {
        String title = ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.currency-choice-title"));
        Inventory gui = Bukkit.createInventory(null, 45, title);

        ItemStack vault = new ItemStack(Material.GOLD_INGOT);
        ItemMeta vaultMeta = vault.getItemMeta();
        if (vaultMeta != null) {
            vaultMeta.setDisplayName(ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.currency-vault")));
            vault.setItemMeta(vaultMeta);
        }

        ItemStack levels = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta levelsMeta = levels.getItemMeta();
        if (levelsMeta != null) {
            levelsMeta.setDisplayName(ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.currency-levels")));
            levels.setItemMeta(levelsMeta);
        }

        gui.setItem(20, vault);
        gui.setItem(24, levels);
        player.openInventory(gui);
    }

    private void openRemoveConfirmGUI(Player player, ItemStack item) {
        String title = ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.remove-confirm-title"));
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack confirm = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName(ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.remove-confirm-button")));
            confirm.setItemMeta(confirmMeta);
        }
        gui.setItem(11, confirm);

        gui.setItem(13, item);

        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(ColorUtil.colorize(plugin.getMessageManager().getMessage("gui.edit.remove-cancel-button")));
            cancel.setItemMeta(cancelMeta);
        }
        gui.setItem(15, cancel);
        player.openInventory(gui);
    }
}