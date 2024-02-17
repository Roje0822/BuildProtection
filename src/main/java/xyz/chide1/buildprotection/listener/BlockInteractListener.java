package xyz.chide1.buildprotection.listener;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.chide1.buildprotection.message.MessageManager;
import xyz.chide1.buildprotection.message.MessageType;
import xyz.chide1.buildprotection.object.AllowedBlock;
import xyz.chide1.buildprotection.object.ProtectionRegion;
import xyz.chide1.buildprotection.storage.BuildProtectionItemStorage;
import xyz.chide1.buildprotection.util.ProtectionRegionUtil;

public class BlockInteractListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        AllowedBlock allowedBlock = BuildProtectionItemStorage.getAllowedBlock();

        // If block is in Region, region variablize
        if (block == null) return;
        if (BlockBreakListener.getAdminModePlayers().contains(player.getUniqueId())) return;
        ProtectionRegion region = ProtectionRegionUtil.getInstance().getRegionByLocation(block.getLocation());
        if (region == null) return;
        if (region.getBuilder().equals(player.getUniqueId())) return;

        if (region.getOwners().contains(player.getUniqueId())) {
            if (allowedBlock.getOwnerMaterial() == null) return;
            if (allowedBlock.getOwnerMaterial().contains(block.getType())) {
                event.setCancelled(true);
                MessageManager.getInstance().getMessageAfterPrefix(MessageType.ERROR, "interactInRegion").ifPresent(message -> {
                    String replacedMessage = message.replace("%player%", Bukkit.getOfflinePlayer(region.getBuilder()).getName());
                    player.sendMessage(replacedMessage);
                });
                return;
            }
        }
        else {
            if (allowedBlock.getGeneralMaterial() == null) return;
            if (allowedBlock.getGeneralMaterial().contains(block.getType())) {
                event.setCancelled(true);
                MessageManager.getInstance().getMessageAfterPrefix(MessageType.ERROR, "interactInRegion").ifPresent(message -> {
                    String replacedMessage = message.replace("%player%", Bukkit.getOfflinePlayer(region.getBuilder()).getName());
                    player.sendMessage(replacedMessage);
                });
                return;
            }
        }

    }
}
