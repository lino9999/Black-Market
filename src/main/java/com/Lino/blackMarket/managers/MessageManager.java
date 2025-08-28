package com.Lino.blackMarket.managers;

import com.Lino.blackMarket.BlackMarket;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageManager {

    private final BlackMarket plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public MessageManager(BlackMarket plugin) {
        this.plugin = plugin;
        createMessagesFile();
    }

    private void createMessagesFile() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            try {
                messagesFile.createNewFile();
                setDefaultMessages();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void setDefaultMessages() {
        messagesConfig.set("market.open", "&gradient:#FF0080:#8000FF>&l[BLACK MARKET] &fThe shadows stir... The Black Market has opened its doors for the night!");
        messagesConfig.set("market.close", "&gradient:#8000FF:#FF0080>&l[BLACK MARKET] &fDawn breaks... The Black Market vanishes into the shadows until nightfall.");

        messagesConfig.set("gui.title", "&gradient:#FF0080:#8000FF>&lBLACK MARKET");
        messagesConfig.set("gui.info.title", "&gradient:#FFD700:#FFA500>&lMarket Information");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("&7Welcome to the Black Market!");
        infoLore.add("&7");
        infoLore.add("&6Your Balance: &e${balance}");
        infoLore.add("&7");
        infoLore.add("&7Items refresh daily");
        infoLore.add("&7Limited stock available!");
        messagesConfig.set("gui.info.lore", infoLore);

        messagesConfig.set("gui.item.discount", "&gradient:#00FF00:#FFFF00>&l[-{percent}%]");
        messagesConfig.set("gui.item.price", "&6Price: &e${price}");
        messagesConfig.set("gui.item.price-original", "&6Price: &c&m${price}");
        messagesConfig.set("gui.item.price-discounted", "&6Discounted: &a${price}");
        messagesConfig.set("gui.item.stock", "&7Stock: &f{stock}/{max}");
        messagesConfig.set("gui.item.click-to-buy", "&gradient:#00FF00:#00FFFF>&l➤ Click to purchase!");

        messagesConfig.set("purchase.success", "&gradient:#00FF00:#00FFFF>&l[BLACK MARKET] &aSuccessfully purchased {item} &afor &e${price}!");
        messagesConfig.set("purchase.insufficient-funds", "&gradient:#FF0000:#FF6600>&l[BLACK MARKET] &cYou need &e${price} &cto purchase this item!");
        messagesConfig.set("purchase.out-of-stock", "&gradient:#FF0000:#FF6600>&l[BLACK MARKET] &cThis item is out of stock!");
        messagesConfig.set("purchase.error", "&gradient:#FF0000:#FF6600>&l[BLACK MARKET] &cAn error occurred during purchase!");

        messagesConfig.set("command.player-only", "&cThis command can only be used by players!");
        messagesConfig.set("command.market-closed", "&gradient:#FF6600:#FFCC00>&l[BLACK MARKET] &cThe Black Market is closed! Come back at night (13000-23000 ticks).");
        messagesConfig.set("command.reload", "&gradient:#00FF00:#00FFFF>&l[BLACK MARKET] &aConfiguration and messages reloaded!");

        saveMessages();
    }

    public void reload() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        return messagesConfig.getString(path, "&cMessage not found: " + path);
    }

    public List<String> getMessageList(String path) {
        return messagesConfig.getStringList(path);
    }

    private void saveMessages() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}