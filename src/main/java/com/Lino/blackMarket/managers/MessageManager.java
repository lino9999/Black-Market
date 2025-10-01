package com.Lino.blackMarket.managers;

import com.Lino.blackMarket.BlackMarket;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
                messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
                setDefaultMessages();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        }
    }

    private void setDefaultMessages() {
        messagesConfig.set("market.open", "<gradient:#FF0080:#8000FF>&l[BLACK MARKET]</gradient> &fThe shadows stir... The Black Market has opened its doors for the night!");
        messagesConfig.set("market.close", "<gradient:#8000FF:#FF0080>&l[BLACK MARKET]</gradient> &fDawn breaks... The Black Market vanishes into the shadows until nightfall.");

        messagesConfig.set("gui.title", "<gradient:#FF0080:#8000FF>&lBLACK MARKET</gradient>");
        messagesConfig.set("gui.info.title", "<gradient:#FDC830:#F37335>&lMarket Information</gradient>");
        messagesConfig.set("gui.info.lore", Arrays.asList(
                "<gradient:#ECE9E6:#FFFFFF>Welcome to the Black Market!</gradient>",
                "",
                "<gradient:#FDB813:#F94A29>Your Balance:</gradient> &f{balance}",
                "",
                "<gradient:#BDBBBE:#9D9D9F>Items refresh daily</gradient>",
                "<gradient:#BDBBBE:#9D9D9F>Limited stock available!</gradient>"
        ));

        messagesConfig.set("gui.item.discount", "<gradient:#00FF00:#FFFF00>&l[-{percent}%]</gradient>");
        messagesConfig.set("gui.item.price", "<gradient:#FDB813:#F94A29>Price:</gradient> &f{price}");
        messagesConfig.set("gui.item.price-original", "<gradient:#FF416C:#FF4B2B>&mPrice: {price}</gradient>");
        messagesConfig.set("gui.item.price-discounted", "<gradient:#11998e:#38ef7d>Discounted:</gradient> &f{price}");
        messagesConfig.set("gui.item.stock", "<gradient:#BDBBBE:#9D9D9F>Stock:</gradient> &f{stock}/{max}");
        messagesConfig.set("gui.item.click-to-buy", "<gradient:#00FF00:#00FFFF>&l➤ Click to purchase!</gradient>");
        messagesConfig.set("gui.item.out-of-stock", "<gradient:#FF0000:#8B0000>&l✖ Out of Stock!</gradient>");

        messagesConfig.set("purchase.success", "<gradient:#11998e:#38ef7d>&l[BLACK MARKET]</gradient> &fSuccessfully purchased {item} &ffor &f{price}!");
        messagesConfig.set("purchase.success-levels", "<gradient:#11998e:#38ef7d>&l[BLACK MARKET]</gradient> &fSuccessfully purchased {item} &ffor &f{price} levels!");
        messagesConfig.set("purchase.insufficient-funds", "<gradient:#FF416C:#FF4B2B>&l[BLACK MARKET]</gradient> &fYou need &f${price} &fto purchase this!");
        messagesConfig.set("purchase.insufficient-levels", "<gradient:#FF416C:#FF4B2B>&l[BLACK MARKET]</gradient> &fYou need &f{price} levels &fto purchase this!");
        messagesConfig.set("purchase.out-of-stock", "<gradient:#FF416C:#FF4B2B>&l[BLACK MARKET]</gradient> &fThis item is out of stock!");
        messagesConfig.set("purchase.error", "<gradient:#FF416C:#FF4B2B>&l[BLACK MARKET]</gradient> &fAn error occurred during purchase!");

        messagesConfig.set("command.player-only", "<gradient:#FF416C:#FF4B2B>This command can only be used by players!</gradient>");
        messagesConfig.set("command.market-closed", "<gradient:#FF6600:#FFCC00>&l[BLACK MARKET]</gradient> &fThe Black Market is closed! Come back at night (13000-23000 ticks).");
        messagesConfig.set("command.reload", "<gradient:#00FF00:#00FFFF>&l[BLACK MARKET]</gradient> &fConfiguration and messages reloaded!");

        messagesConfig.set("command.help", Arrays.asList(
                "<gradient:#FF0080:#8000FF>&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>",
                "   <gradient:#FF0080:#8000FF>&lBLACK MARKET HELP</gradient>",
                "<gradient:#FF0080:#8000FF>&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>",
                "",
                "<gradient:#FDC830:#F37335>Player Commands:</gradient>",
                "  &e/{label} &7- Open the Black Market",
                "  &e/{label} help &7- Show this help message",
                "",
                "<gradient:#FF416C:#FF4B2B>Admin Commands:{admin}</gradient>",
                "  &e/{label} reload &7- Reload configuration{admin}",
                "  &e/{label} forceopen &7- Force open the market{admin}",
                "",
                "&7Aliases: &f/bm, /blackm, /market",
                "<gradient:#FF0080:#8000FF>&l━━━━━━━━━━━━━━━━━━━━━━━━━━━━</gradient>"
        ));
        saveMessages();
    }

    public void reload() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        if (messagesConfig == null) {
            return "&cConfiguration error!";
        }
        String message = messagesConfig.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Message not found: " + path);
            return "&cMessage not found: " + path;
        }
        return message;
    }

    public List<String> getMessageList(String path) {
        if (messagesConfig == null) {
            return new ArrayList<>();
        }
        List<String> list = messagesConfig.getStringList(path);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list;
    }

    private void saveMessages() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save messages file!");
            e.printStackTrace();
        }
    }
}