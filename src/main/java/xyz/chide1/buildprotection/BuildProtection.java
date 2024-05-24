package xyz.chide1.buildprotection;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.chide1.buildprotection.command.BuildProtectionCommand;
import xyz.chide1.buildprotection.command.BuildProtectionItemCommand;
import xyz.chide1.buildprotection.command.tabcomplete.BuildProtectionItemTabComplete;
import xyz.chide1.buildprotection.command.tabcomplete.BuildProtectionTabComplete;
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
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractAtEntityListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
        getServer().getPluginManager().registerEvents(new BlockInteractListener(), this);

        // Config
        saveDefaultConfig();

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