package xyz.chide1.buildprotection.storage;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import xyz.chide1.buildprotection.BuildProtection;
import xyz.chide1.buildprotection.object.AllowedBlock;
import xyz.chide1.buildprotection.object.ProtectionItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuildProtectionItemStorage {

    @Getter
    private static final BuildProtectionItemStorage instance = new BuildProtectionItemStorage();

    private static final File DATA_DIRECTORY = new File(BuildProtection.getInstance().getDataFolder(), "data");

    @Getter
    @Setter
    private static ProtectionItem protectionItem;

    @Getter
    @Setter
    private static AllowedBlock allowedBlock;

    private BuildProtectionItemStorage() {}

    public void load() {
        if (!DATA_DIRECTORY.exists()) {
            DATA_DIRECTORY.mkdir();
        }
        File protectionItemFile = new File(DATA_DIRECTORY, "protectionItem.yml");
        File allowedBlockFile = new File(DATA_DIRECTORY, "allowedBlock.yml");
        YamlConfiguration yaml;

        if (!protectionItemFile.exists()) {
            protectionItem = new ProtectionItem(new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.COPPER_INGOT),
                    new ItemStack(Material.IRON_INGOT), new ItemStack(Material.BEACON),
                    new ItemStack(Material.RED_CONCRETE), new ItemStack(Material.RED_STAINED_GLASS));
        } else {
            yaml = YamlConfiguration.loadConfiguration(protectionItemFile);
            protectionItem = new ProtectionItem(yaml.getItemStack("big-item"), yaml.getItemStack("normal-item"),
                    yaml.getItemStack("small-item"), yaml.getItemStack("head-item"),
                    yaml.getItemStack("outLineFloor-item"), yaml.getItemStack("outLine-item"));
        }

        yaml = YamlConfiguration.loadConfiguration(allowedBlockFile);
        List<Material> ownerMaterials = new ArrayList<>();
        List<Material> generalMaterials = new ArrayList<>();
        for (String material : yaml.getStringList("ownerMaterials")) {
            ownerMaterials.add(Material.valueOf(material));
        }
        for (String material : yaml.getStringList("generalMaterials")) {
            generalMaterials.add(Material.valueOf(material));
        }

        allowedBlock = new AllowedBlock(ownerMaterials, generalMaterials);
    }

    public void save() {
        if (!DATA_DIRECTORY.exists()) DATA_DIRECTORY.mkdir();
        File protectionItemFile = new File(DATA_DIRECTORY, "protectionItem.yml");
        File allowedBlockFile = new File(DATA_DIRECTORY, "allowedBlock.yml");

        // create exception
        if (!protectionItemFile.exists()) {
            try {
                protectionItemFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!allowedBlockFile.exists()) {
            try {
                allowedBlockFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration itemYaml = YamlConfiguration.loadConfiguration(protectionItemFile);
        itemYaml.set("big-item", protectionItem.getProtectionBigItem());
        itemYaml.set("normal-item", protectionItem.getProtectionNormalItem());
        itemYaml.set("small-item", protectionItem.getProtectionSmallItem());
        itemYaml.set("head-item", protectionItem.getProtectionHead());
        itemYaml.set("outLineFloor-item", protectionItem.getOutLineFloor());
        itemYaml.set("outLine-item", protectionItem.getOutLine());

        // save
        try {
            itemYaml.save(protectionItemFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        YamlConfiguration blockYaml = YamlConfiguration.loadConfiguration(allowedBlockFile);
        List<String> ownerBlocksString = new ArrayList<>();
        allowedBlock.getOwnerMaterial().forEach(material -> {
            ownerBlocksString.add(material.name());
        });
        List<String> generalBlocksString = new ArrayList<>();
        allowedBlock.getGeneralMaterial().forEach(material -> {
            generalBlocksString.add(material.name());
        });

        blockYaml.set("ownerMaterials", ownerBlocksString);
        blockYaml.set("generalMaterials", generalBlocksString);

        try {
            blockYaml.save(allowedBlockFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
