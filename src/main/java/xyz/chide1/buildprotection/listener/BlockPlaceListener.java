package xyz.chide1.buildprotection.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;
import xyz.chide1.buildprotection.config.ConfigManager;
import xyz.chide1.buildprotection.config.ConfigType;
import xyz.chide1.buildprotection.message.MessageManager;
import xyz.chide1.buildprotection.message.MessageType;
import xyz.chide1.buildprotection.object.ProtectionRegion;
import xyz.chide1.buildprotection.storage.BuildProtectionRegionStorage;
import xyz.chide1.buildprotection.util.ProtectionRegionUtil;

import java.util.List;

public class BlockPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ProtectionRegionUtil protectionRegionUtil = ProtectionRegionUtil.getInstance();
        ConfigManager config = ConfigManager.getInstance();
        MessageManager message = MessageManager.getInstance();
        Block block = event.getBlock();

        for (ProtectionRegion region : BuildProtectionRegionStorage.getProtectionRegions()) {
            if (region.getWorld().equals(event.getBlock().getLocation().getWorld())) {
                if (BlockBreakListener.getAdminModePlayers().contains(player.getUniqueId())) return;
                if (block.getLocation().distance(region.getHead()) < 3 * config.getInt(ConfigType.REGION, "bigSizeRadius") && !region.getOwners().contains(player.getUniqueId())) {
                    List<Vector> vectors = protectionRegionUtil.calculateRegion(region);
                    if (event.getBlock().getType().equals(Material.IRON_DOOR)) {
                        if (!(region.getBuilder().equals(player.getUniqueId()) || (Boolean.valueOf(config.getMessage(ConfigType.REGION, "isIronDoorPlaceOwner").get())
                                && region.getOwners().contains(player.getUniqueId())))) {

                            for (Vector vector : vectors) {
                                if (vector.getBlockX() == block.getX() && vector.getBlockY() == block.getY() && vector.getBlockZ() == block.getZ()) {
                                    message.getMessageAfterPrefix(MessageType.ERROR, "placeIronDoorInRegion").ifPresent(s -> {
                                        String replacedMessage = s.replace("%player%", Bukkit.getOfflinePlayer(region.getBuilder()).getName());
                                        player.sendMessage(replacedMessage);
                                    });
                                    event.setCancelled(true);
                                }
                            }
                        }

                    } else {
                        vectors.clear();
                        if (Boolean.valueOf(config.getMessage(ConfigType.REGION, "enablePlaceInRegion").get()))
                            vectors = protectionRegionUtil.calculateRegion(region);
                        else vectors = protectionRegionUtil.calculateOutLine(region);

                        for (Vector vector : vectors) {
                            if (vector.getBlockX() == block.getX() && vector.getBlockY() == block.getY() && vector.getBlockZ() == block.getZ()) {
                                message.getMessageAfterPrefix(MessageType.ERROR, "noBreakOrPlace").ifPresent(s -> {
                                    String replacedMessage = s.replace("%player%", Bukkit.getOfflinePlayer(region.getBuilder()).getName());
                                    player.sendMessage(replacedMessage);
                                });
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }
}
