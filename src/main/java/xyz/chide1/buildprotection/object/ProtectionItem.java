package xyz.chide1.buildprotection.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@Getter
@Setter
public class ProtectionItem {
    private ItemStack protectionBigItem;
    private ItemStack protectionNormalItem;
    private ItemStack protectionSmallItem;
    private ItemStack protectionHead;
    private ItemStack outLineFloor;
    private ItemStack outLine;
}
