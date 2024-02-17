package xyz.chide1.buildprotection;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.chide1.buildprotection.command.BuildProtectionCommand;
import xyz.chide1.buildprotection.command.BuildProtectionItemCommand;
import xyz.chide1.buildprotection.command.tabcomplete.BuildProtectionItemTabComplete;
import xyz.chide1.buildprotection.command.tabcomplete.BuildProtectionTabComplete;
import xyz.chide1.buildprotection.config.ConfigManager;
import xyz.chide1.buildprotection.inventory.page.PaginationHolder;
import xyz.chide1.buildprotection.listener.*;
import xyz.chide1.buildprotection.message.MessageManager;
import xyz.chide1.buildprotection.storage.BuildProtectionItemStorage;
import xyz.chide1.buildprotection.storage.BuildProtectionRegionStorage;

import java.io.File;

public class BuildProtection extends JavaPlugin {

    @Getter
    private static BuildProtection instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {

        // Commands
        getCommand("건차").setExecutor(new BuildProtectionCommand());
        getCommand("건차").setTabCompleter(new BuildProtectionTabComplete());
        getCommand("건차아이템").setExecutor(new BuildProtectionItemCommand());
        getCommand("건차아이템").setTabCompleter(new BuildProtectionItemTabComplete());

        // Listeners
        Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractAtEntityListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryCloseListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockInteractListener(), this);

        // Config
        saveDefaultConfig();
        ConfigManager.getInstance().initialize(getConfig());

        // Message
        if (!new File(getDataFolder(), "messages.yml").exists()) {
            saveResource("messages.yml", false);
        }
        MessageManager.getInstance().initialize(YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml")));

        // Data
        BuildProtectionItemStorage.getInstance().load();
        BuildProtectionRegionStorage.getInstance().loadAll();
        PaginationHolder.initProtectionList();

    }

    @Override
    public void onDisable() {
        BuildProtectionItemStorage.getInstance().save();
        BuildProtectionRegionStorage.getInstance().saveAll();
    }
}
