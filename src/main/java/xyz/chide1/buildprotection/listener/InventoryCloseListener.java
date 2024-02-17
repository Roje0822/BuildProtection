package xyz.chide1.buildprotection.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.chide1.buildprotection.inventory.InventoryHandler;
import xyz.chide1.buildprotection.inventory.InventoryType;
import xyz.chide1.buildprotection.message.MessageManager;
import xyz.chide1.buildprotection.message.MessageType;
import xyz.chide1.buildprotection.object.AllowedBlock;
import xyz.chide1.buildprotection.storage.BuildProtectionItemStorage;

import java.util.ArrayList;
import java.util.List;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (!InventoryHandler.getPlayerInventoryMap().containsKey(player.getUniqueId())) return;

        InventoryType inventoryType = InventoryHandler.getPlayerInventoryMap().get(player.getUniqueId()).getB();
        if (inventoryType.equals(InventoryType.GENERAL_BLOCK_SETTING_MENU)) {
            Inventory inventory = event.getInventory();
            List<Material> generalBlocks = new ArrayList<>();
            for (ItemStack content : inventory.getContents()) {
                if (content != null) {
                    generalBlocks.add(content.getType());
                }
            }
            BuildProtectionItemStorage.setAllowedBlock(new AllowedBlock(BuildProtectionItemStorage.getAllowedBlock().getOwnerMaterial(), generalBlocks));
            MessageManager.getInstance().getMessageAfterPrefix(MessageType.NORMAL, "setInteractGeneralBlock").ifPresent(player::sendMessage);
        }
        else if (inventoryType.equals(InventoryType.OWNER_BLOCK_SETTING_MENU)) {
            Inventory inventory = event.getInventory();
            List<Material> ownerBlocks = new ArrayList<>();
            for (ItemStack content : inventory.getContents()) {
                if (content != null) {
                    ownerBlocks.add(content.getType());
                }
            }
            BuildProtectionItemStorage.setAllowedBlock(new AllowedBlock(ownerBlocks, BuildProtectionItemStorage.getAllowedBlock().getGeneralMaterial()));
            MessageManager.getInstance().getMessageAfterPrefix(MessageType.NORMAL, "setInteractOwnerBlock").ifPresent(player::sendMessage);
        }

        InventoryHandler.getPlayerInventoryMap().remove(player.getUniqueId());
    }
}
