package xyz.chide1.buildprotection.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.chide1.buildprotection.BuildProtection;
import xyz.chide1.buildprotection.message.MessageManager;
import xyz.chide1.buildprotection.message.MessageType;
import xyz.chide1.buildprotection.object.ProtectionItem;
import xyz.chide1.buildprotection.object.ProtectionRegion;
import xyz.chide1.buildprotection.object.RegionSize;
import xyz.chide1.buildprotection.storage.BuildProtectionItemStorage;
import xyz.chide1.buildprotection.util.ProtectionRegionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class InteractListener implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        ProtectionItem protectionItem = BuildProtectionItemStorage.getProtectionItem();
        MessageManager messageContent = MessageManager.getInstance();
        ProtectionRegionUtil protectionRegionUtil = ProtectionRegionUtil.getInstance();

        if (!itemInMainHand.isSimilar(protectionItem.getProtectionBigItem()) &&
                !itemInMainHand.isSimilar(protectionItem.getProtectionNormalItem()) &&
                !itemInMainHand.isSimilar(protectionItem.getProtectionSmallItem())) {
            return;
        }

        if (event.getAction().equals(Action.LEFT_CLICK_AIR)) return;
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            messageContent.getMessageAfterPrefix(MessageType.ERROR, "targetAtAir").ifPresent(player::sendMessage);
            return;
        }
        if (event.getHand().equals(EquipmentSlot.OFF_HAND)) return;

        // World Exception
        FileConfiguration config = BuildProtection.getInstance().getConfig();
        List<?> whiteListWorld = config.getList("world.whiteList");
        if (!whiteListWorld.contains(event.getClickedBlock().getLocation().getWorld().getName())) {
            messageContent.getMessageAfterPrefix(MessageType.ERROR, "wrongWorld").ifPresent(player::sendMessage);
            return;
        }

        // WorldGuard Region Exception
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        List<?> blackListWorldGuardRegions = config.getList("worldGuard.blackList");
        Collection<ProtectedRegion> regions = new ArrayList<>();

        blackListWorldGuardRegions.forEach(s -> {
            String blackListRegion = s.toString().split(", ")[0];
            String worldName = s.toString().split(", ")[1];
            ProtectedRegion region = container.get(BukkitAdapter.adapt(Bukkit.getWorld(worldName))).getRegion(blackListRegion);
            if (region != null) regions.add(region);
        });

        // CoolDown Exception
        if (ProtectionRegionUtil.getRegionCoolDown().containsKey(player.getUniqueId()) && ProtectionRegionUtil.getRegionCoolDown().get(player.getUniqueId()) != 0) {
            messageContent.getMessageAfterPrefix(MessageType.ERROR, "notPassCoolDown").ifPresent(message -> {
                String replacedMessage = message.replace("%second%", ProtectionRegionUtil.getRegionCoolDown().get(player.getUniqueId()).toString());
                player.sendMessage(replacedMessage);
            });
            return;
        }

        if (itemInMainHand.isSimilar(protectionItem.getProtectionBigItem())) {
            if (regionException(RegionSize.BIG, event.getClickedBlock().getLocation(), player, regions)) return;
            protectionRegionUtil.createRegion(player, event.getClickedBlock().getLocation(), RegionSize.BIG);
            handleProtectionItemUsage(player, itemInMainHand, messageContent, RegionSize.BIG);
        } else if (itemInMainHand.isSimilar(protectionItem.getProtectionNormalItem())) {
            if (regionException(RegionSize.NORMAL, event.getClickedBlock().getLocation(), player, regions)) return;
            protectionRegionUtil.createRegion(player, event.getClickedBlock().getLocation(), RegionSize.NORMAL);
            handleProtectionItemUsage(player, itemInMainHand, messageContent, RegionSize.NORMAL);
        } else if (itemInMainHand.isSimilar(protectionItem.getProtectionSmallItem())) {
            if (regionException(RegionSize.SMALL, event.getClickedBlock().getLocation(), player, regions)) return;
            protectionRegionUtil.createRegion(player, event.getClickedBlock().getLocation(), RegionSize.SMALL);
            handleProtectionItemUsage(player, itemInMainHand, messageContent, RegionSize.SMALL);
        }

    }

    private boolean regionException(RegionSize size, Location location, Player player, Collection<ProtectedRegion> regions) {
        ProtectionRegionUtil instance = ProtectionRegionUtil.getInstance();
        ProtectionRegion tempRegion = instance.getProtectionRegion(location, size);
        MessageManager message = MessageManager.getInstance();
        FileConfiguration config = BuildProtection.getInstance().getConfig();
        BlockVector3 minBlockVector = BlockVector3.at(tempRegion.getMinLocation().getBlockX(), tempRegion.getMinLocation().getBlockY(), tempRegion.getMinLocation().getBlockZ());
        BlockVector3 maxBlockVector = BlockVector3.at(tempRegion.getMaxLocation().getBlockX(), tempRegion.getMaxLocation().getBlockY(), tempRegion.getMaxLocation().getBlockZ());
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("temp", minBlockVector, maxBlockVector);
        List<ProtectedRegion> intersectingRegions = region.getIntersectingRegions(regions);

        if (config.getBoolean("regionLocationLimit.min")) {
            if (Math.abs(location.getBlockX()) < config.getInt("regionLocationLimit.minLocation")
                    || Math.abs(location.getBlockZ()) < config.getInt("regionLocationLimit.minLocation")) {
                message.getMessageAfterPrefix(MessageType.ERROR, "min").ifPresent(s -> {
                    String replaced = s.replace("%min%", "" + config.getInt("regionLocationLimit.minLocation"));
                    player.sendMessage(replaced);
                });
                return true;
            }
        }
        else if (config.getBoolean("regionLocationLimit.max")) {
            if (Math.abs(location.getBlockX()) > config.getInt("regionLocationLimit.maxLocation")
                    || Math.abs(location.getBlockZ()) > config.getInt("regionLocationLimit.maxLocation")) {
                message.getMessageAfterPrefix(MessageType.ERROR, "max").ifPresent(s -> {
                    String replaced = s.replace("%max%", "" + config.getInt("regionLocationLimit.maxLocation"));
                    player.sendMessage(replaced);
                });
                return true;
            }
        }

        if (!intersectingRegions.isEmpty()) {
            message.getMessageAfterPrefix(MessageType.ERROR, "intersectWorldGuardRegion").ifPresent(player::sendMessage);
            return true;
        }
        if (instance.isOverLap(tempRegion)) {
            message.getMessageAfterPrefix(MessageType.ERROR, "overLapProtection").ifPresent(player::sendMessage);
            return true;
        }
        if (instance.getRegionAmount(player, size) >= instance.getRegionLimit(size)) {
            message.getMessageAfterPrefix(MessageType.ERROR, "limitRegion").ifPresent(s -> {
                String replacedMessage = s.replace("%size%", size.getKey()).replace("%current%", "" + instance.getRegionAmount(player, size))
                        .replace("%limit%", "" + instance.getRegionLimit(size));
                player.sendMessage(replacedMessage);
            });
            return true;
        }

        return false;
    }

    private void handleProtectionItemUsage(Player player, ItemStack itemInMainHand, MessageManager messageManager, RegionSize size) {
        ProtectionRegionUtil instance = ProtectionRegionUtil.getInstance();
        int amount = itemInMainHand.getAmount();
        itemInMainHand.setAmount(amount - 1);
        player.getInventory().setItemInMainHand(itemInMainHand);

        messageManager.getMessageAfterPrefix(MessageType.NORMAL, "createRegion").ifPresent(player::sendMessage);
        messageManager.getMessage(MessageType.NORMAL, "checkSize").ifPresent(message -> {
            String replaceMessage = message.replace("%size%", size.getKey()).replace("%current%", "" + instance.getRegionAmount(player, size))
                    .replace("%max%", "" + instance.getRegionLimit(size));
            player.sendMessage(replaceMessage);
        });
    }
}
