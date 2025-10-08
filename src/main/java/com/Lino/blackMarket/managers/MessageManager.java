package com.Lino.blackMarket.managers;

import com.Lino.blackMarket.BlackMarket;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class MessageManager {

    private final BlackMarket plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public MessageManager(BlackMarket plugin) {
        this.plugin = plugin;
        saveDefaultMessages();
    }

    public void reload() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        if (plugin.getResource("messages.yml") != null) {
            InputStreamReader defConfigStream = new InputStreamReader(plugin.getResource("messages.yml"), StandardCharsets.UTF_8);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            messagesConfig.setDefaults(defConfig);
        }
    }

    public void saveDefaultMessages() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        String message = messagesConfig.getString(path, "&cMessage not found: " + path);
        if (message.contains("not found")) {
            plugin.getLogger().warning("Message not found in messages.yml: " + path);
        }
        return message;
    }

    public List<String> getMessageList(String path) {
        if (!messagesConfig.isList(path)) {
            return Collections.singletonList(getMessage(path));
        }
        return messagesConfig.getStringList(path);
    }
}