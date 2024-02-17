package xyz.chide1.buildprotection.inventory.page;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.chide1.buildprotection.builder.ItemBuilder;
import xyz.chide1.buildprotection.object.ProtectionRegion;
import xyz.chide1.buildprotection.storage.BuildProtectionRegionStorage;

import java.util.ArrayList;
import java.util.List;

public class PaginationHolder {

    @Getter
    private static final List<ProtectionPage> protectionPageList = new ArrayList<>();

    public static void initProtectionList() {
        protectionPageList.clear();
        List<ItemStack> temp = new ArrayList<>();
        for (ProtectionRegion region : BuildProtectionRegionStorage.getProtectionRegions()) {
            temp.add(new ItemBuilder(Material.PLAYER_HEAD).setName("§e" + Bukkit.getOfflinePlayer(region.getBuilder()).getName() + "님의 건차")
                    .setLore(" §7└ 크기: " + region.getSize().getKey(),
                            " §7└ 좌표: " + region.getHead().getBlockX() + " " + region.getHead().getBlockY() + " " + region.getHead().getBlockZ(),
                            "",
                            "§7[ 좌클릭: 건차 정보 GUI 오픈 ]",
                            "§7[ 우클릭: 건차 이동 ]",
                            "§7[ 쉬프트+좌클릭: 건차 주인 설정 ]",
                            "§7[ 쉬프트+우클릭: 건차 삭제 ]")
                    .setOwner(region.getBuilder())
                    .setPDC(region.getUuid().toString())
                    .build());
        }

        int itemCount = temp.size();
        int pageCount = (int) Math.ceil((double) itemCount / 45);

        for (int i = 0; i < pageCount; i++) {
            int start = i * 45;
            int end = Math.min(start + 45, itemCount);
            List<ItemStack> result = temp.subList(start, end);
            protectionPageList.add(i, new ProtectionPage(result));
        }
    }
}
