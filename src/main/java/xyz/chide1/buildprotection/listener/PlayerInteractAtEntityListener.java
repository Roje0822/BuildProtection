package xyz.chide1.buildprotection.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import xyz.chide1.buildprotection.inventory.InventoryHandler;
import xyz.chide1.buildprotection.object.ProtectionRegion;
import xyz.chide1.buildprotection.storage.BuildProtectionRegionStorage;

public class PlayerInteractAtEntityListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getRightClicked().getType().equals(EntityType.ARMOR_STAND)) {
            for (ProtectionRegion protectionRegion : BuildProtectionRegionStorage.getProtectionRegions()) {
                if (event.getRightClicked().getLocation().equals(protectionRegion.getHead())) {
                    InventoryHandler.openMenu(player, protectionRegion, false);
                }
            }
        }
    }
}