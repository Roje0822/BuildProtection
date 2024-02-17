package xyz.chide1.buildprotection.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.chide1.buildprotection.BuildProtection;
import xyz.chide1.buildprotection.config.ConfigManager;
import xyz.chide1.buildprotection.config.ConfigType;
import xyz.chide1.buildprotection.inventory.InventoryHandler;
import xyz.chide1.buildprotection.listener.BlockBreakListener;
import xyz.chide1.buildprotection.listener.PlayerChatListener;
import xyz.chide1.buildprotection.message.MessageManager;
import xyz.chide1.buildprotection.message.MessageType;
import xyz.chide1.buildprotection.object.ProtectionRegion;
import xyz.chide1.buildprotection.storage.BuildProtectionRegionStorage;
import xyz.chide1.buildprotection.util.ProtectionRegionUtil;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class BuildProtectionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        MessageManager message = MessageManager.getInstance();
        ConfigManager config = ConfigManager.getInstance();

        if (!(sender instanceof Player player)) {
            message.getMessageAfterPrefix(MessageType.ERROR, "noConsoleCommand").ifPresent(sender::sendMessage);
            return true;

        }

        if (!player.isOp()) {
            message.getMessageAfterPrefix(MessageType.ERROR, "noPermissionCommand").ifPresent(sender::sendMessage);
            return true;
        }

        // usage
        if (args.length == 0) {
            message.getUsage(MessageType.NORMAL, "buildProtectionUsage").forEach(player::sendMessage);
            return true;
        }

        switch (args[0]) {

            case "목록" -> {
                if (!InventoryHandler.getPlayerInventoryMap().isEmpty()) {
                    message.getMessageAfterPrefix(MessageType.ERROR, "alreadyUseListGui").ifPresent(player::sendMessage);
                    return true;
                }
                InventoryHandler.openProtectionList(player);
                if (PlayerChatListener.getChatMap().containsKey(player.getUniqueId())) {
                    PlayerChatListener.getChatMap().remove(player.getUniqueId());
                }
                return true;
            }

            case "리로드" -> {
                message.initialize(YamlConfiguration.loadConfiguration(new File(
                        BuildProtection.getInstance().getDataFolder(), "messages.yml")));
                BuildProtection.getInstance().saveDefaultConfig();
                config.initialize(BuildProtection.getInstance().getConfig());

                message.getMessageAfterPrefix(MessageType.NORMAL, "reloadConfig").ifPresent(Bukkit::broadcastMessage);
                config.getMessage(ConfigType.REGION_LOCATION_LIMIT, "min").ifPresent(player::sendMessage);
                return true;
            }

            case "관리자" -> {
                List<UUID> adminModePlayers = BlockBreakListener.getAdminModePlayers();
                if (!adminModePlayers.contains(player.getUniqueId())) {
                    adminModePlayers.add(player.getUniqueId());
                    message.getMessageAfterPrefix(MessageType.NORMAL, "enableAdminMode").ifPresent(player::sendMessage);
                } else {
                    adminModePlayers.remove(player.getUniqueId());
                    message.getMessageAfterPrefix(MessageType.NORMAL, "unableAdminMode").ifPresent(player::sendMessage);
                }
                return true;
            }

            case "설정" -> {
                if (args.length == 1) {
                    message.getMessageAfterPrefix(MessageType.ERROR, "wrongCommand").ifPresent(player::sendMessage);
                    return true;
                }

                if (args[1].equals("권한")) {
                    InventoryHandler.openOwnerBlockSettingInventory(player);
                    return true;
                } else if (args[1].equals("일반")) {
                    InventoryHandler.openGeneralBlockSettingInventory(player);
                    return true;
                } else {
                    message.getMessageAfterPrefix(MessageType.ERROR, "wrongCommand").ifPresent(player::sendMessage);
                    return true;
                }
            }
        }

        return false;
    }
}
