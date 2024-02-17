package xyz.chide1.buildprotection.inventory.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ProtectionPage {
    private List<ItemStack> itemStacks;
}
