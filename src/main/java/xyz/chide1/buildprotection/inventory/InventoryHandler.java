package xyz.chide1.buildprotection.inventory;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.chide1.buildprotection.builder.ItemBuilder;
import xyz.chide1.buildprotection.inventory.page.PaginationHolder;
import xyz.chide1.buildprotection.inventory.page.ProtectionPage;
import xyz.chide1.buildprotection.message.MessageManager;
import xyz.chide1.buildprotection.message.MessageType;
import xyz.chide1.buildprotection.object.ProtectionRegion;
import xyz.chide1.buildprotection.storage.BuildProtectionItemStorage;
import xyz.chide1.buildprotection.storage.BuildProtectionRegionStorage;
import xyz.chide1.buildprotection.util.Tuple;

import java.util.*;

public class InventoryHandler {

    @Getter
    private static final Map<UUID, Tuple<ProtectionRegion, InventoryType>> playerInventoryMap = new HashMap<>();

    @Getter
    private static final Map<UUID, Integer> playerPageMap = new HashMap<>();

    public static void openMenu(Player player, ProtectionRegion region, boolean isPermission) {
        player.closeInventory();
        OfflinePlayer builder = Bukkit.getOfflinePlayer(region.getBuilder());
        Inventory inventory = Bukkit.createInventory(null, 36,  "§r"+ builder.getName() + "님의 소유");

        String isOnline;
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§f").build());
        }
        for (int i = 27; i < 36; i++) {
            inventory.setItem(i, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("§f").build());
        }
        List<UUID> owners = new ArrayList<>();
        owners.addAll(region.getOwners());
        owners.remove(region.getBuilder());

        if (builder.isOnline()) isOnline = "§a온라인";
        else isOnline = "§8오프라인";

        if (region.getBuilder().equals(player.getUniqueId()) || isPermission) {
            inventory.setItem(4, new ItemBuilder(Material.PLAYER_HEAD).setOwner(region.getBuilder()).setName("§f" + builder.getName())
                    .setLore(isOnline, "", "§7[ 좌클릭: 건차 이동 GUI 오픈 ]").build());
            for (int i = 0; i < owners.size(); i++) {
                OfflinePlayer owner = Bukkit.getOfflinePlayer(owners.get(i));
                if (owner.isOnline()) isOnline = "§a온라인";
                else isOnline = "§8오프라인";
                ItemStack itemStack = new ItemBuilder(Material.PLAYER_HEAD).setName("§f" + owner.getName()).setOwner(owner.getUniqueId())
                        .setLore(isOnline, "", "§7[ 쉬프트 좌클릭 시 해당 플레이어의 권한을 제거합니다. ]", "§7[ 쉬프트 우클릭 시 해당 플레이어에게 건차를 양도합니다. ]").build();
                inventory.setItem(i + 9, itemStack);
            }
            inventory.setItem(31, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setName("§a권한 부여").build());
            inventory.setItem(35, new ItemBuilder(Material.BARRIER).setName("§c건차 회수").build());

            playerInventoryMap.put(player.getUniqueId(), new Tuple<>(region, InventoryType.MENU));
        } else {
            inventory.setItem(4, new ItemBuilder(Material.PLAYER_HEAD).setOwner(region.getBuilder()).setName("§f" + builder.getName())
                    .setLore(isOnline).build());
            for (int i = 0; i < owners.size(); i++) {
                OfflinePlayer owner = Bukkit.getOfflinePlayer(owners.get(i));
                if (owner.isOnline()) isOnline = "§a온라인";
                else isOnline = "§8오프라인";

                if (owner.equals(player)) {
                    ItemStack itemStack = new ItemBuilder(Material.PLAYER_HEAD).setName("§f" + owner.getName()).setOwner(owner.getUniqueId())
                            .setLore(isOnline, "", "§7[ 쉬프트 좌클릭 시 자신의 권한을 제거합니다. ]").build();
                    inventory.setItem(i + 9, itemStack);
                }
                else {
                    ItemStack itemStack = new ItemBuilder(Material.PLAYER_HEAD).setName("§f" + owner.getName()).setOwner(owner.getUniqueId())
                            .setLore(isOnline).build();
                    inventory.setItem(i + 9, itemStack);
                }

            }
            playerInventoryMap.put(player.getUniqueId(), new Tuple<>(region, InventoryType.MENU_WITHOUT_PERMISSION));
        }
        player.openInventory(inventory);
    }

    public static void openAddPermission(Player player, ProtectionRegion region) {
        player.closeInventory();
        Inventory inventory = Bukkit.createInventory(null, 54, "§r권한을 추가할 플레이어를 선택해주세요");
        List<Player> playerList = new ArrayList<>();
        playerList.addAll(Bukkit.getOnlinePlayers());

        region.getOwners().forEach(uuid -> {
            playerList.remove(Bukkit.getPlayer(uuid));
        });

        int slot = 0;
        for (Player onlinePlayer : playerList) {
            ItemStack playerHead = new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§f" + onlinePlayer.getDisplayName())
                    .setLore("§7클릭 시 건차에 권한을 추가합니다.")
                    .setOwner(onlinePlayer.getUniqueId())
                    .build();
            inventory.setItem(slot, playerHead);
            slot++;
        }

        for (int i = 46; i < 53; i++) {
            inventory.setItem(i, new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setName(" ").build());
        }

        inventory.setItem(45, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setName("§c이전 페이지로 이동").build());
        inventory.setItem(53, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setName("§a다음 페이지로 이동").build());

        player.openInventory(inventory);
        playerInventoryMap.put(player.getUniqueId(), new Tuple<>(region, InventoryType.ADD_PERMISSION_MENU));
    }

    public static void openWithDrawMenu(Player player, ProtectionRegion region) {
        player.closeInventory();
        Inventory inventory = Bukkit.createInventory(null, 9, "§r건차를 회수하시겠습니까?");
        for (int i = 0; i < 4; i++) {
            inventory.setItem(i, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setName("§a회수하기").build());
        }
        for (int i = 5; i < 9; i++) {
            inventory.setItem(i, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setName("§c취소하기").build());
        }
        ItemStack item = new ItemStack(Material.DIRT);
        switch (region.getSize()) {
            case BIG -> {
                item = BuildProtectionItemStorage.getProtectionItem().getProtectionBigItem();
            }
            case NORMAL -> {
                item = BuildProtectionItemStorage.getProtectionItem().getProtectionNormalItem();
            }
            case SMALL -> {
                item = BuildProtectionItemStorage.getProtectionItem().getProtectionSmallItem();
            }
        }
        inventory.setItem(4, item);

        player.openInventory(inventory);
        playerInventoryMap.put(player.getUniqueId(), new Tuple<>(region, InventoryType.WITHDRAW_MENU));
    }

    public static void changeInventoryAddPermission(Player player, ProtectionRegion region) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        List<Player> playerList = new ArrayList<>();
        playerList.addAll(Bukkit.getOnlinePlayers());
        region.getOwners().forEach(uuid -> {
            playerList.remove(Bukkit.getPlayer(uuid));
        });

        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, null);
        }

        int slot = 0;
        for (Player onlinePlayer : playerList) {
            ItemStack playerHead = new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§f" + onlinePlayer.getDisplayName())
                    .setLore("§7클릭 시 건차에 권한을 추가합니다.")
                    .setOwner(onlinePlayer.getUniqueId())
                    .build();
            inventory.setItem(slot, playerHead);
            slot++;
        }
    }

    public static void changeInventoryMenu(Player player, ProtectionRegion region, boolean isPermission) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        for (int i = 9; i < 27; i++) {
            inventory.clear(i);
        }

        List<UUID> owners = new ArrayList<>();
        owners.addAll(region.getOwners());
        owners.remove(region.getBuilder());

        OfflinePlayer builder = Bukkit.getOfflinePlayer(region.getBuilder());

        String isOnline;
        if (builder.isOnline()) isOnline = "§a온라인";
        else isOnline = "§8오프라인";

        inventory.setItem(4, new ItemBuilder(Material.PLAYER_HEAD).setOwner(region.getBuilder()).setName("§f" + builder.getName())
                .setLore(isOnline).build());

        if (region.getBuilder().equals(player.getUniqueId()) || isPermission) {
            for (int i = 0; i < owners.size(); i++) {
                OfflinePlayer owner = Bukkit.getOfflinePlayer(owners.get(i));
                if (owner.isOnline()) isOnline = "§a온라인";
                else isOnline = "§8오프라인";
                ItemStack itemStack = new ItemBuilder(Material.PLAYER_HEAD).setName("§f" + owner.getName()).setOwner(owner.getUniqueId())
                        .setLore(isOnline, "", "§7[ 쉬프트 좌클릭 시 해당 플레이어의 권한을 제거합니다.", "§7[ 쉬프트 우클릭 시 해당 플레이어에게 건차를 양도합니다.").build();
                inventory.setItem(i + 9, itemStack);
            }
        } else {
            for (int i = 0; i < owners.size(); i++) {
                OfflinePlayer owner = Bukkit.getOfflinePlayer(owners.get(i));
                if (owner.isOnline()) isOnline = "§a온라인";
                else isOnline = "§8오프라인";
                ItemStack itemStack = new ItemBuilder(Material.PLAYER_HEAD).setName("§f" + owner.getName()).setOwner(owner.getUniqueId())
                        .setLore(isOnline).build();
                inventory.setItem(i + 9, itemStack);
            }
        }
    }

    public static void openProtectionList(Player player) {
        if (PaginationHolder.getProtectionPageList().isEmpty()) {
            MessageManager.getInstance().getMessageAfterPrefix(MessageType.ERROR, "noProtection").ifPresent(player::sendMessage);
            return;
        }
        ProtectionPage protectionPage = PaginationHolder.getProtectionPageList().get(0);
        Inventory inventory = Bukkit.createInventory(null, 54, "§r건차 : 1페이지");
        int slot = 0;

        for (ItemStack itemStack : protectionPage.getItemStacks()) {
            inventory.setItem(slot, itemStack);
            slot++;
        }
        for (int i = 46; i < 53; i++) {
            inventory.setItem(i, new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setName(" ").build());
        }

        inventory.setItem(45, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setName("§c이전 페이지로 이동").build());
        inventory.setItem(53, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setName("§a다음 페이지로 이동").build());

        player.openInventory(inventory);
        playerInventoryMap.put(player.getUniqueId(), new Tuple<>(null, InventoryType.LIST_MENU));
        playerPageMap.put(player.getUniqueId(), 0);
    }

    public static void changeProtectionListMenu(Player player) {
        PaginationHolder.initProtectionList();
        if (PaginationHolder.getProtectionPageList().isEmpty()) {
            player.closeInventory();
            return;
        }
        ProtectionPage protectionPage = PaginationHolder.getProtectionPageList().get(playerPageMap.get(player.getUniqueId()));
        Inventory inventory = player.getOpenInventory().getTopInventory();
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, null);
        }
        int slot = 0;
        for (ItemStack itemStack : protectionPage.getItemStacks()) {
            inventory.setItem(slot, itemStack);
            slot++;
        }
    }

    public static void openTelePortInventory(Player player, ProtectionRegion region) {
        Inventory inventory = Bukkit.createInventory(null, org.bukkit.event.inventory.InventoryType.HOPPER, "§r" + region.getSize().getKey() + " 건차 신호기");
        int slot = 0;
        for (ProtectionRegion protectionRegion : BuildProtectionRegionStorage.getRegionBySize(player.getUniqueId(), region.getSize())) {
            if (protectionRegion.equals(region)) {
                inventory.setItem(slot, new ItemBuilder(Material.BEACON).setName("§e" + Bukkit.getOfflinePlayer(protectionRegion.getBuilder()).getName() + "님의 건차" + "§7(현재위치)")
                        .setLore(" §7└ 크기: " + protectionRegion.getSize().getKey(),
                                " §7└ 좌표: " + protectionRegion.getHead().getBlockX() + " " + protectionRegion.getHead().getBlockY() + " " + protectionRegion.getHead().getBlockZ(),
                                "",
                                "§7[ 좌클릭: 건차 이동 ]")
                        .setPDC(protectionRegion.getUuid().toString())
                        .build());
                slot++;
            } else {
                inventory.setItem(slot, new ItemBuilder(Material.BEACON).setName("§e" + Bukkit.getOfflinePlayer(protectionRegion.getBuilder()).getName() + "님의 건차")
                        .setLore(" §7└ 크기: " + protectionRegion.getSize().getKey(),
                                " §7└ 좌표: " + protectionRegion.getHead().getBlockX() + " " + protectionRegion.getHead().getBlockY() + " " + protectionRegion.getHead().getBlockZ(),
                                "",
                                "§7[ 좌클릭: 건차 이동 ]")
                        .setPDC(protectionRegion.getUuid().toString())
                        .build());
                slot++;
            }
        }
        player.openInventory(inventory);
        playerInventoryMap.put(player.getUniqueId(), new Tuple<>(region, InventoryType.TELEPORT_MENU));
    }

    public static void openOwnerBlockSettingInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "§r권한 금지 블럭 설정");
        if (BuildProtectionItemStorage.getAllowedBlock().getOwnerMaterial() != null) {
            int slot = 0;
            for (Material ownerMaterial : BuildProtectionItemStorage.getAllowedBlock().getOwnerMaterial()) {
                inventory.setItem(slot, new ItemStack(ownerMaterial));
                slot++;
            }
        }
        player.openInventory(inventory);
        playerInventoryMap.put(player.getUniqueId(), new Tuple<>(null, InventoryType.OWNER_BLOCK_SETTING_MENU));
    }

    public static void openGeneralBlockSettingInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "§r일반 금지 블럭 설정");
        if (BuildProtectionItemStorage.getAllowedBlock().getGeneralMaterial() != null) {
            int slot = 0;
            for (Material generalMaterial : BuildProtectionItemStorage.getAllowedBlock().getGeneralMaterial()) {
                inventory.setItem(slot, new ItemStack(generalMaterial));
                slot++;
            }
        }
        player.openInventory(inventory);
        playerInventoryMap.put(player.getUniqueId(), new Tuple<>(null, InventoryType.GENERAL_BLOCK_SETTING_MENU));
    }
}
