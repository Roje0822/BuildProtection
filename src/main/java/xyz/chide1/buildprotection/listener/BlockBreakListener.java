package xyz.chide1.buildprotection.listener;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.Vector;
import xyz.chide1.buildprotection.BuildProtection;
import xyz.chide1.buildprotection.message.MessageManager;
import xyz.chide1.buildprotection.message.MessageType;
import xyz.chide1.buildprotection.object.ProtectionRegion;
import xyz.chide1.buildprotection.storage.BuildProtectionItemStorage;
import xyz.chide1.buildprotection.storage.BuildProtectionRegionStorage;
import xyz.chide1.buildprotection.util.ProtectionRegionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlockBreakListener implements Listener {

    @Getter
    private static final List<UUID> adminModePlayers = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ProtectionRegionUtil protectionRegionUtil = ProtectionRegionUtil.getInstance();
        FileConfiguration config = BuildProtection.getInstance().getConfig();
        MessageManager message = MessageManager.getInstance();
        Block block = event.getBlock();

        if (block.getType().equals(BuildProtectionItemStorage.getProtectionItem().getOutLineFloor().getType())
            && adminModePlayers.contains(player.getUniqueId())) {
            List<Vector> outLineFloor;
            for (ProtectionRegion region : BuildProtectionRegionStorage.getProtectionRegions()) {
                if (region.getHead().distance(region.getHead()) < 2 * config.getInt("region.bigSizeRadius")) {
                    outLineFloor = protectionRegionUtil.getOutLineFloor(region);
                    for (Vector vector : outLineFloor) {
                        if (vector.getBlockX() == block.getX() && vector.getBlockY() == block.getY() && vector.getBlockZ() == block.getZ()) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }

        for (ProtectionRegion region : BuildProtectionRegionStorage.getProtectionRegions()) {
            if (region.getWorld().equals(event.getBlock().getLocation().getWorld())) {
                if (region.getHead().distance(region.getHead()) < 2 * config.getInt("region.bigSizeRadius") + 1) {
                    if (adminModePlayers.contains(player.getUniqueId())) return;
                    if (!region.getOwners().contains(player.getUniqueId())) {
                        List<Vector> vectors;
                        if (config.getBoolean("region.enableBreakInRegion"))
                            vectors = protectionRegionUtil.calculateRegion(region);
                        else vectors = protectionRegionUtil.calculateOutLine(region);

                        for (Vector vector : vectors) {
                            if (vector.getBlockX() == block.getX() && vector.getBlockY() == block.getY() && vector.getBlockZ() == block.getZ()) {
                                if (!event.isCancelled()) {
                                    message.getMessageAfterPrefix(MessageType.ERROR, "noBreakOrPlace").ifPresent(s -> {
                                        String replacedMessage = s.replace("%player%", Bukkit.getOfflinePlayer(region.getBuilder()).getName());
                                        player.sendMessage(replacedMessage);
                                    });
                                }
                                event.setCancelled(true);
                            }
                        }
                    }
                    else {
                        List<Vector> outLineFloor = protectionRegionUtil.getOutLineFloor(region);
                        for (Vector vector : outLineFloor) {
                            if (vector.getBlockX() == block.getX() && vector.getBlockY() == block.getY() && vector.getBlockZ() == block.getZ()) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
