package com.tu.dnc;

import com.tu.dnc.commands.DNCCommand;
import com.tu.dnc.listeners.ChatListener;
import com.tu.dnc.listeners.GUIListener;
import com.tu.dnc.placeholder.DNCExpansion;
import com.tu.dnc.storage.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DisplayNameColorPlugin extends JavaPlugin {

    private static DisplayNameColorPlugin instance;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;
        this.dataManager = new DataManager(this);

        DNCCommand command = new DNCCommand(this);
        getCommand("dnc").setExecutor(command);
        getCommand("dnc").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new DNCExpansion(this).register();
            getLogger().info("Da dang ky PlaceholderAPI expansion: %dnc_color%, %dnc_name%");
        } else {
            getLogger().warning("Khong tim thay PlaceholderAPI - TAB se khong hien mau ten. Cai PlaceholderAPI de dung day du tinh nang.");
        }

        getLogger().info("DisplayNameColor da khoi dong. Dang quan ly " + dataManager.getAllEntries().size() + " DNC.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveConfig();
            dataManager.saveData();
        }
    }

    public static DisplayNameColorPlugin getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
