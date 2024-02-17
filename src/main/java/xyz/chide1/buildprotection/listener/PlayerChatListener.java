package xyz.chide1.buildprotection.listener;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import xyz.chide1.buildprotection.BuildProtection;
import xyz.chide1.buildprotection.inventory.InventoryHandler;
import xyz.chide1.buildprotection.inventory.page.PaginationHolder;
import xyz.chide1.buildprotection.message.MessageManager;
import xyz.chide1.buildprotection.message.MessageType;
import xyz.chide1.buildprotection.object.ProtectionRegion;

import javax.swing.plaf.basic.BasicButtonUI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerChatListener implements Listener {

    @Getter
    private static final Map<UUID, ProtectionRegion> chatMap = new HashMap<>();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!chatMap.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);
        Player target = Bukkit.getPlayer(event.getMessage());

        if (target == null) {
            PlayerChatListener.getChatMap().remove(player.getUniqueId());
            MessageManager.getInstance().getMessageAfterPrefix(MessageType.ERROR, "nonExistPlayer").ifPresent(player::sendMessage);
            return;
        }
        ProtectionRegion region = chatMap.get(player.getUniqueId());
        if (region.getBuilder().equals(target.getUniqueId())) {
            MessageManager.getInstance().getMessageAfterPrefix(MessageType.ERROR, "alreadyBuilder").ifPresent(message -> {
                String replacedMessage = message.replace("%player%", target.getName());
                player.sendMessage(replacedMessage);
            });
            return;
        }
        region.setBuilder(target.getUniqueId());
        PlayerChatListener.getChatMap().remove(player.getUniqueId());

        PaginationHolder.initProtectionList();
        MessageManager.getInstance().getMessageAfterPrefix(MessageType.NORMAL, "setBuilder").ifPresent(message -> {
            String replacedMessage = message.replace("%player%", target.getName());
            player.sendMessage(replacedMessage);
        });
        Bukkit.getScheduler().runTaskLater(BuildProtection.getInstance(), () -> {
            InventoryHandler.openProtectionList(player);
        }, 2);

    }
}
