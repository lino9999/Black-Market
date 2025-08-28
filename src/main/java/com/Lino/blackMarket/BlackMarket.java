package com.Lino.blackMarket;

import com.Lino.blackMarket.commands.BlackMarketCommand;
import com.Lino.blackMarket.listeners.GUIListener;
import com.Lino.blackMarket.managers.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlackMarket extends JavaPlugin {

    private static BlackMarket instance;
    private Economy economy;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private BlackMarketManager blackMarketManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Vault economy not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        databaseManager = new DatabaseManager(this);
        blackMarketManager = new BlackMarketManager(this);

        getCommand("blackmarket").setExecutor(new BlackMarketCommand(this));
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        blackMarketManager.startScheduler();
    }

    @Override
    public void onDisable() {
        if (blackMarketManager != null) {
            blackMarketManager.stopScheduler();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static BlackMarket getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public BlackMarketManager getBlackMarketManager() {
        return blackMarketManager;
    }
}