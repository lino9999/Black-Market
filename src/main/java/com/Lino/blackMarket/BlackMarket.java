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

        saveDefaultConfig();
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        configManager = new ConfigManager(this);

        boolean useLevels = getConfig().getBoolean("settings.use-levels", false);

        if (!useLevels) {
            if (!setupEconomy()) {
                getLogger().severe("Vault economy not found! Disabling plugin or enable use-levels in config.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        messageManager = new MessageManager(this);
        databaseManager = new DatabaseManager(this);
        blackMarketManager = new BlackMarketManager(this);

        BlackMarketCommand commandExecutor = new BlackMarketCommand(this);
        if (getCommand("blackmarket") != null) {
            getCommand("blackmarket").setExecutor(commandExecutor);
            getCommand("blackmarket").setTabCompleter(commandExecutor);
        } else {
            getLogger().severe("Failed to register blackmarket command!");
        }

        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        blackMarketManager.startScheduler();

        getLogger().info("Black Market plugin has been enabled!");
        if (useLevels) {
            getLogger().info("Using experience levels as currency");
        } else {
            getLogger().info("Using Vault economy as currency");
        }
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

    public boolean useLevels() {
        return getConfig().getBoolean("settings.use-levels", false);
    }
}