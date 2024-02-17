package xyz.chide1.buildprotection.command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.chide1.buildprotection.message.MessageManager;
import xyz.chide1.buildprotection.message.MessageType;
import xyz.chide1.buildprotection.object.ProtectionItem;
import xyz.chide1.buildprotection.storage.BuildProtectionItemStorage;

public class BuildProtectionItemCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        MessageManager messageManager = MessageManager.getInstance();

        if (!(sender instanceof Player player)) {
            messageManager.getMessageAfterPrefix(MessageType.ERROR, "noConsoleCommand").ifPresent(sender::sendMessage);
            return true;
        }

        if (!player.isOp()) {
            messageManager.getMessageAfterPrefix(MessageType.ERROR,"noPermissionCommand").ifPresent(sender::sendMessage);
            return true;
        }

        // usage
        if (args.length == 0) {
            messageManager.getUsage(MessageType.NORMAL, "buildProtectionItemUsage").forEach(player::sendMessage);
            return true;
        }

        switch (args[0]) {
            case "생성" -> {
                ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                if (itemInMainHand.getType().equals(Material.AIR)) {
                    messageManager.getMessageAfterPrefix(MessageType.ERROR, "noItemInMainHand").ifPresent(player::sendMessage);
                    return true;
                }
                if (itemInMainHand.getAmount() != 1) {
                    messageManager.getMessageAfterPrefix(MessageType.ERROR, "itemMaxSize").ifPresent(player::sendMessage);
                    return true;
                }
                if (args.length != 2) {
                    messageManager.getMessageAfterPrefix(MessageType.ERROR, "wrongCommand").ifPresent(player::sendMessage);
                    return true;
                }
                ProtectionItem protectionItem = BuildProtectionItemStorage.getProtectionItem();

                switch (args[1]) {
                    case "Big" -> protectionItem.setProtectionBigItem(itemInMainHand);
                    case "Normal" -> protectionItem.setProtectionNormalItem(itemInMainHand);
                    case "Small" -> protectionItem.setProtectionSmallItem(itemInMainHand);
                    default -> {
                        messageManager.getMessageAfterPrefix(MessageType.ERROR, "wrongBuildProtectionItemSize").ifPresent(player::sendMessage);
                        return true;
                    }
                }

                BuildProtectionItemStorage.setProtectionItem(protectionItem);
                BuildProtectionItemStorage.getInstance().save();
                BuildProtectionItemStorage.getInstance().load();
                messageManager.getMessageAfterPrefix(MessageType.NORMAL, "setProtectionItemSize").ifPresent(message -> {
                    String replacedMessage = message.replace("%size%", args[1]);
                    player.sendMessage(replacedMessage);
                });
                return true;
            }

            case "머리" -> {
                ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                if (itemInMainHand.getType().equals(Material.AIR)) {
                    messageManager.getMessageAfterPrefix(MessageType.ERROR, "noItemInMainHand").ifPresent(player::sendMessage);
                    return true;
                }

                BuildProtectionItemStorage.getProtectionItem().setProtectionHead(itemInMainHand);
                messageManager.getMessageAfterPrefix(MessageType.NORMAL, "setProtectionItemHead").ifPresent(player::sendMessage);
                return true;
            }

            case "테두리바닥" -> {
                ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                if (itemInMainHand.getType().equals(Material.AIR)) {
                    messageManager.getMessageAfterPrefix(MessageType.ERROR, "noItemInMainHand").ifPresent(player::sendMessage);
                    return true;
                }

                BuildProtectionItemStorage.getProtectionItem().setOutLineFloor(itemInMainHand);
                messageManager.getMessageAfterPrefix(MessageType.NORMAL, "setProtectionItemOutLineFloor").ifPresent(player::sendMessage);
                return true;
            }

            case "테두리나머지" -> {
                ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                if (itemInMainHand.getType().equals(Material.AIR)) {
                    messageManager.getMessageAfterPrefix(MessageType.ERROR, "noItemInMainHand").ifPresent(player::sendMessage);
                    return true;
                }

                BuildProtectionItemStorage.getProtectionItem().setOutLine(itemInMainHand);
                messageManager.getMessageAfterPrefix(MessageType.NORMAL, "setProtectionItemOutLine").ifPresent(player::sendMessage);
                return true;
            }

            case "지급" -> {
                if (args.length > 3) {
                    messageManager.getMessageAfterPrefix(MessageType.ERROR, "wrongCommand").ifPresent(player::sendMessage);
                    return true;
                }
                int amount;
                if (args.length == 2) {
                    amount = 1;
                } else {
                    if (args[2].matches("^[0-9]+$")) {
                        amount = Integer.parseInt(args[2]);
                    } else {
                        messageManager.getMessageAfterPrefix(MessageType.ERROR, "wrongIntegerValue").ifPresent(player::sendMessage);
                        return true;
                    }
                }

                switch (args[1]) {
                    case "Big" -> {
                        ItemStack clone = BuildProtectionItemStorage.getProtectionItem().getProtectionBigItem().clone();
                        clone.setAmount(amount);
                        player.getInventory().addItem(clone);
                    }
                    case "Normal" -> {
                        ItemStack clone = BuildProtectionItemStorage.getProtectionItem().getProtectionNormalItem().clone();
                        clone.setAmount(amount);
                        player.getInventory().addItem(clone);
                    }
                    case "Small" -> {
                        ItemStack clone = BuildProtectionItemStorage.getProtectionItem().getProtectionSmallItem().clone();
                        clone.setAmount(amount);
                        player.getInventory().addItem(BuildProtectionItemStorage.getProtectionItem().getProtectionSmallItem());
                    }
                    default -> {
                        messageManager.getMessageAfterPrefix(MessageType.ERROR, "wrongProtectionItemSize").ifPresent(player::sendMessage);
                        return true;
                    }
                }
                messageManager.getMessageAfterPrefix(MessageType.NORMAL, "giveProtectionItemSize").ifPresent(message -> {
                    String replacedMessage = message.replace("%size%", args[1]);
                    player.sendMessage(replacedMessage);
                });
                return true;
            }
            case "머리지급" -> {
                player.getInventory().addItem(BuildProtectionItemStorage.getProtectionItem().getProtectionHead());
                messageManager.getMessageAfterPrefix(MessageType.NORMAL, "giveProtectionItemHad").ifPresent(player::sendMessage);
                return true;
            }
        }

        return false;
    }
}
