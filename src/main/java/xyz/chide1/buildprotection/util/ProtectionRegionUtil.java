package xyz.chide1.buildprotection.util;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import xyz.chide1.buildprotection.config.ConfigManager;
import xyz.chide1.buildprotection.config.ConfigType;
import xyz.chide1.buildprotection.inventory.page.PaginationHolder;
import xyz.chide1.buildprotection.object.ProtectionItem;
import xyz.chide1.buildprotection.object.ProtectionRegion;
import xyz.chide1.buildprotection.object.RegionSize;
import xyz.chide1.buildprotection.storage.BuildProtectionItemStorage;
import xyz.chide1.buildprotection.storage.BuildProtectionRegionStorage;

import java.util.*;

public class ProtectionRegionUtil {

    @Getter
    private static final ProtectionRegionUtil instance = new ProtectionRegionUtil();

    private ProtectionRegionUtil() {
    }

    public void createRegion(Player player, Location center, RegionSize size) {
        ConfigManager configManager = ConfigManager.getInstance();
        ProtectionItem protectionItem = BuildProtectionItemStorage.getProtectionItem();
        int radius = 0;
        int height = 0;

        switch (size) {
            case BIG -> {
                radius = configManager.getInt(ConfigType.REGION, "bigSizeRadius");
                height = configManager.getInt(ConfigType.REGION, "bigHeight");
            }
            case NORMAL -> {
                radius = configManager.getInt(ConfigType.REGION, "normalSizeRadius");
                height = configManager.getInt(ConfigType.REGION, "normalHeight");
            }
            case SMALL -> {
                radius = configManager.getInt(ConfigType.REGION, "smallSizeRadius");
                height = configManager.getInt(ConfigType.REGION, "smallHeight");
            }
        }

        // 블럭 설치
        Location minLocation = center.clone().add(-radius, 0, -radius);

        center.add(0.5, 1, 0.5);
        Entity entity = center.getWorld().spawnEntity(center, EntityType.ARMOR_STAND);
        ArmorStand armorStand = (ArmorStand) entity;
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.getEquipment().setHelmet(protectionItem.getProtectionHead());
        armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
        armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.ADDING_OR_CHANGING);

        ProtectionRegion protectionRegion = new ProtectionRegion(
                player.getUniqueId(),
                List.of(player.getUniqueId()),
                minLocation,
                center.clone().add(radius, height -1, radius),
                center,
                size,
                UUID.randomUUID(),
                center.getWorld(),
                entity.getUniqueId()
        );

        setOutLine(size, center, false);

        // 객체 관리
        BuildProtectionRegionStorage.getProtectionRegions().add(protectionRegion);
        PaginationHolder.initProtectionList();

        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.5f);
    }

    public ProtectionRegion getProtectionRegion(Location center, RegionSize size) {
        ConfigManager configManager = ConfigManager.getInstance();
        int radius = 0;
        int height = 0;

        switch (size) {
            case BIG -> {
                radius = configManager.getInt(ConfigType.REGION, "bigSizeRadius");
                height = configManager.getInt(ConfigType.REGION, "bigHeight");
            }
            case NORMAL -> {
                radius = configManager.getInt(ConfigType.REGION, "normalSizeRadius");
                height = configManager.getInt(ConfigType.REGION, "normalHeight");
            }
            case SMALL -> {
                radius = configManager.getInt(ConfigType.REGION, "smallSizeRadius");
                height = configManager.getInt(ConfigType.REGION, "smallHeight");
            }
        }

        // 블럭 설치
        Location minLocation = center.clone().add(-radius, 0, -radius);
        Location maxLocation = center.clone().add(radius, height, radius);
        ProtectionRegion tempRegion = new ProtectionRegion(
                null,
                null,
                minLocation,
                maxLocation,
                center,
                size,
                null,
                center.getWorld(),
                null
        );
        return tempRegion;
    }

    public List<Vector> calculateOutLine(ProtectionRegion region) {
        Location minLocation = region.getMinLocation();
        Location maxLocation = region.getMaxLocation();
        List<Vector> outLine = new ArrayList<>();

        // X축 Y축
        for (int i = minLocation.getBlockX(); i <= maxLocation.getBlockX(); i++) {
            for (int j = minLocation.getBlockY(); j < maxLocation.getBlockY(); j++) {
                outLine.add(new Vector(i, j, minLocation.getBlockZ()));
                outLine.add(new Vector(i, j, maxLocation.getBlockZ()));
            }
        }

        // X축 Z축
        for (int i = minLocation.getBlockX(); i <= maxLocation.getBlockX(); i++) {
            for (int j = minLocation.getBlockZ(); j <= maxLocation.getBlockZ(); j++) {
                outLine.add(new Vector(i, minLocation.getBlockY(), j));
                outLine.add(new Vector(i, maxLocation.getBlockY(), j));
            }
        }

        // Y축 Z축
        for (int i = minLocation.getBlockY(); i < maxLocation.getBlockY(); i++) {
            for (int j = minLocation.getBlockZ(); j <= maxLocation.getBlockZ(); j++) {
                outLine.add(new Vector(minLocation.getBlockX(), i, j));
                outLine.add(new Vector(maxLocation.getBlockX(), i, j));
            }
        }

        return outLine;
    }

    public List<Vector> calculateRegion(ProtectionRegion region) {
        Location minLocation = region.getMinLocation();
        Location maxLocation = region.getMaxLocation();
        List<Vector> inRegion = new ArrayList<>();

        for (int x = minLocation.getBlockX(); x <= maxLocation.getBlockX(); x++) {
            for (int y = minLocation.getBlockY(); y < maxLocation.getBlockY(); y++) {
                for (int z = minLocation.getBlockZ(); z <= maxLocation.getBlockZ(); z++) {
                    inRegion.add(new Vector(x, y, z));
                }
            }
        }

        return inRegion;
    }

    public List<Vector> getOutLineFloor(ProtectionRegion region) {
        Location minLocation = region.getMinLocation();
        Location maxLocation = region.getMaxLocation();
        List<Vector> outLine = new ArrayList<>();

        for (int i = minLocation.getBlockX(); i <= maxLocation.getBlockX(); i++) {
            for (int j = minLocation.getBlockZ(); j <= maxLocation.getBlockZ(); j++) {
                if (i == minLocation.getBlockX() || i == maxLocation.getBlockX()) {
                    outLine.add(new Vector(i, minLocation.getBlockY(), j));
                }
                if (j == minLocation.getBlockZ() || j == maxLocation.getBlockZ()) {
                    outLine.add(new Vector(i, minLocation.getBlockY(), j));
                }
            }
        }

        return outLine;
    }

    public boolean isOverLap(ProtectionRegion region) {
        List<Vector> vectors = calculateOutLine(region);
        ConfigManager config = ConfigManager.getInstance();

        for (ProtectionRegion protectionRegion : BuildProtectionRegionStorage.getProtectionRegions()) {
            if (region.getWorld().equals(protectionRegion.getWorld())) {
                List<Vector> vectors1 = calculateOutLine(protectionRegion);
                if (protectionRegion.getHead().distance(region.getHead()) < 3 * config.getInt(ConfigType.REGION, "bigSizeRadius")) {
                    for (Vector vector : vectors) {
                        for (Vector vector1 : vectors1) {
                            if (vector.getBlockX() == vector1.getBlockX() && vector.getBlockY() == vector1.getBlockY() && vector.getBlockZ() == vector1.getBlockZ())
                                return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public void withDrawRegion(ProtectionRegion region) {
        Player builder = Bukkit.getPlayer(region.getBuilder());
        ItemStack item = switch (region.getSize()) {
            case BIG -> BuildProtectionItemStorage.getProtectionItem().getProtectionBigItem();
            case NORMAL -> BuildProtectionItemStorage.getProtectionItem().getProtectionNormalItem();
            case SMALL -> BuildProtectionItemStorage.getProtectionItem().getProtectionSmallItem();
        };
        builder.getInventory().addItem(item);
        deleteRegion(region);
    }

    public void deleteRegion(ProtectionRegion region) {
        if (Bukkit.getEntity(region.getEntityUUID()) == null) return;
        Bukkit.getEntity(region.getEntityUUID()).remove();
        setOutLine(region.getSize(), region.getHead(), true);

        BuildProtectionRegionStorage.getProtectionRegions().remove(region);
        PaginationHolder.initProtectionList();
    }

    public int getRegionAmount(Player player, RegionSize size) {
        return (int) BuildProtectionRegionStorage.getProtectionRegions().stream().filter(region -> region.getSize().equals(size))
                .filter(region -> region.getBuilder().equals(player.getUniqueId())).count();
    }

    public int getRegionLimit(RegionSize size) {
        ConfigManager config = ConfigManager.getInstance();
        return config.getInt(ConfigType.REGION_LIMIT, size.name().toLowerCase());
    }

    private void setOutLine(RegionSize size, Location center, boolean delete) {
        ConfigManager configManager = ConfigManager.getInstance();
        ProtectionItem protectionItem = BuildProtectionItemStorage.getProtectionItem();
        int radius = 0;
        int height = 0;

        switch (size) {
            case BIG -> {
                radius = configManager.getInt(ConfigType.REGION, "bigSizeRadius");
                height = configManager.getInt(ConfigType.REGION, "bigHeight");
            }
            case NORMAL -> {
                radius = configManager.getInt(ConfigType.REGION, "normalSizeRadius");
                height = configManager.getInt(ConfigType.REGION, "normalHeight");
            }
            case SMALL -> {
                radius = configManager.getInt(ConfigType.REGION, "smallSizeRadius");
                height = configManager.getInt(ConfigType.REGION, "smallHeight");
            }
        }

        // 블럭 설치
        Location minLocation;
        Location maxLocation;
        Material outLineFloor;
        Material outLine;

        if (delete) {
            minLocation = center.clone().add(-radius , -1, -radius);
            maxLocation = center.clone().add(-radius, height -1, -radius);
            outLineFloor = Material.AIR;
            outLine = Material.AIR;
        } else {
            minLocation = center.clone().add(-radius , -1, -radius);
            maxLocation = center.clone().add(-radius, height -1, -radius);
            outLineFloor = protectionItem.getOutLineFloor().getType();
            outLine = protectionItem.getOutLine().getType();
        }

        {
            Location tempLocation;

            for (int i = 0; i < radius * 2; i++) {
                minLocation.getBlock().setType(outLineFloor);
                maxLocation.getBlock().setType(outLine);
                minLocation.add(1, 0, 0);
                maxLocation.add(1, 0, 0);
            }
            tempLocation = minLocation.clone();
            for (int i = 0; i < height; i++) {
                tempLocation.getBlock().setType(outLine);
                tempLocation.add(0, 1, 0);
            }

            for (int i = 0; i < radius * 2; i++) {
                minLocation.getBlock().setType(outLineFloor);
                maxLocation.getBlock().setType(outLine);
                minLocation.add(0, 0, 1);
                maxLocation.add(0, 0, 1);
            }
            tempLocation = minLocation.clone();
            for (int i = 0; i < height; i++) {
                tempLocation.getBlock().setType(outLine);
                tempLocation.add(0, 1, 0);
            }

            for (int i = 0; i < radius * 2; i++) {
                minLocation.getBlock().setType(outLineFloor);
                maxLocation.getBlock().setType(outLine);
                minLocation.add(-1, 0, 0);
                maxLocation.add(-1, 0, 0);
            }
            tempLocation = minLocation.clone();
            for (int i = 0; i < height; i++) {
                tempLocation.getBlock().setType(outLine);
                tempLocation.add(0, 1, 0);
            }

            for (int i = 0; i < radius * 2; i++) {
                minLocation.getBlock().setType(outLineFloor);
                maxLocation.getBlock().setType(outLine);
                minLocation.add(0, 0, -1);
                maxLocation.add(0, 0, -1);
            }
            tempLocation = minLocation.clone();
            tempLocation.add(0, 1, 0);
            for (int i = 0; i < height; i++) {
                tempLocation.getBlock().setType(outLine);
                tempLocation.add(0, 1, 0);
            }
        }
    }

    public ProtectionRegion getRegionByLocation(Location location) {
        for (ProtectionRegion region : BuildProtectionRegionStorage.getProtectionRegions()) {
            if (region.getWorld().equals(location.getWorld())) {
                if (region.getHead().distance(region.getHead()) < 2 * ConfigManager.getInstance().getInt(ConfigType.REGION, "bigSizeRadius") + 1) {
                    List<Vector> vectors = calculateRegion(region);
                    for (Vector vector : vectors) {
                        if (location.getBlockX() == vector.getBlockX() && location.getBlockY() == vector.getBlockY() && location.getBlockZ() == vector.getBlockZ()) {
                            return region;
                        }
                    }
                }
            }
        }
        return null;
    }
}
