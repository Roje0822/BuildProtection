package xyz.chide1.buildprotection.storage;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.stringtemplate.v4.ST;
import xyz.chide1.buildprotection.BuildProtection;
import xyz.chide1.buildprotection.object.ProtectionRegion;
import xyz.chide1.buildprotection.object.RegionSize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BuildProtectionRegionStorage {

    @Getter
    private static final BuildProtectionRegionStorage instance = new BuildProtectionRegionStorage();

    @Getter
    private static final List<ProtectionRegion> protectionRegions = new ArrayList<>();
    private final File directory = new File(BuildProtection.getInstance().getDataFolder(), "region");

    private BuildProtectionRegionStorage() {}

    public void loadAll() {
        if (!directory.exists()) directory.mkdir();
        for (File file : directory.listFiles()) {
            load(file);
        }
    }

    public void saveAll() {
        protectionRegions.remove(null);
        List<String> regionUuids = new ArrayList<>();
        protectionRegions.forEach(region -> {
            regionUuids.add(region.getUuid().toString());
        });
        for (File file : directory.listFiles()) {
            if (!regionUuids.contains(file.getName().replace(".yml", ""))) file.delete();
        }

        protectionRegions.forEach(this::save);
    }

    public void load(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<String> owners = yaml.getStringList("owners");
        List<UUID> ownersUUID = new ArrayList<>();
        owners.forEach(owner -> {
            ownersUUID.add(UUID.fromString(owner));
        });
        ProtectionRegion protectionRegion = new ProtectionRegion(
                UUID.fromString(yaml.getString("builder")),
                ownersUUID,
                yaml.getLocation("minLocation"),
                yaml.getLocation("maxLocation"),
                yaml.getLocation("head"),
                RegionSize.valueOf(yaml.getString("size")),
                UUID.fromString(yaml.getString("uuid")),
                Bukkit.getWorld(yaml.getString("world")),
                UUID.fromString(yaml.getString("entity"))
        );
        protectionRegions.add(protectionRegion);
    }

    public void save(ProtectionRegion region) {
        File regionFile = new File(directory, region.getUuid() + ".yml");

        if (!regionFile.exists()) {
            try {
                regionFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        final List<String> owners = new ArrayList<>();
        region.getOwners().forEach(uuid -> {
            owners.add(uuid.toString());
        });
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(regionFile);
        yaml.set("builder", region.getBuilder().toString());
        yaml.set("owners", owners);
        yaml.set("minLocation", region.getMinLocation());
        yaml.set("maxLocation", region.getMaxLocation());
        yaml.set("head", region.getHead());
        yaml.set("size", region.getSize().name());
        yaml.set("uuid", region.getUuid().toString());
        yaml.set("world", region.getWorld().getName());
        yaml.set("entity", region.getEntityUUID().toString());

        try {
            yaml.save(regionFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProtectionRegion getRegion(UUID uuid) {
        for (ProtectionRegion region : protectionRegions) {
            if (region.getUuid().equals(uuid)) return region;
        }
        return null;
    }

    public static List<ProtectionRegion> getRegionBySize(UUID uuid, RegionSize size) {
        List<ProtectionRegion> temp = new ArrayList<>();
        for (ProtectionRegion region : protectionRegions) {
            if (region.getBuilder().equals(uuid) && region.getSize().equals(size)) {
                temp.add(region);
            }
        }
        return temp;
    }

}
