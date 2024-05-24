package xyz.chide1.buildprotection.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import xyz.chide1.buildprotection.BuildProtection;
import xyz.chide1.buildprotection.builder.ItemBuilder;
import xyz.chide1.buildprotection.inventory.InventoryHandler;
import xyz.chide1.buildprotection.inventory.InventoryType;
import xyz.chide1.buildprotection.inventory.page.PaginationHolder;
import xyz.chide1.buildprotection.message.MessageManager;
import xyz.chide1.buildprotection.message.MessageType;
import xyz.chide1.buildprotection.object.ProtectionRegion;
import xyz.chide1.buildprotection.object.RegionSize;
import xyz.chide1.buildprotection.storage.BuildProtectionRegionStorage;
import xyz.chide1.buildprotection.util.ProtectionRegionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!InventoryHandler.getPlayerInventoryMap().containsKey(player.getUniqueId())) return;

        ProtectionRegion region = InventoryHandler.getPlayerInventoryMap().get(player.getUniqueId()).getA();
        InventoryType inventoryType = InventoryHandler.getPlayerInventoryMap().get(player.getUniqueId()).getB();
        MessageManager content = MessageManager.getInstance();

        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().equals(player.getInventory())) return;
        if (event.getCurrentItem() == null) return;

        ProtectionRegionUtil regionUtil = ProtectionRegionUtil.getInstance();
        FileConfiguration config = BuildProtection.getInstance().getConfig();

        final int slot = event.getSlot();
        switch (inventoryType) {
            case MENU -> {
                event.setCancelled(true);
                if (slot == 4) {
                    if (region.getBuilder().equals(player.getUniqueId()) && config.getBoolean("region.beaconTeleport")) {
                        if (!event.getClick().equals(ClickType.LEFT)) return;
                        switch (region.getSize()) {
                            case BIG -> {
                                if (config.getInt("regionLimit.big") > 5
                                    || BuildProtectionRegionStorage.getRegionBySize(player.getUniqueId(), RegionSize.BIG).size() > 5)
                                {
                                    content.getMessageAfterPrefix(MessageType.ERROR, "overLapProtectionAmount").ifPresent(message -> {
                                        String replacedMessage = message.replace("%size%", RegionSize.BIG.getKey());
                                        player.sendMessage(replacedMessage);
                                    });
                                    return;
                                }
                            }
                            case NORMAL -> {
                                if (config.getInt("regionLimit.normal") > 5
                                        || BuildProtectionRegionStorage.getRegionBySize(player.getUniqueId(), RegionSize.NORMAL).size() > 5)
                                {
                                    content.getMessageAfterPrefix(MessageType.ERROR, "overLapProtectionAmount").ifPresent(message -> {
                                        String replacedMessage = message.replace("%size%", RegionSize.NORMAL.getKey());
                                        player.sendMessage(replacedMessage);
                                    });
                                    return;
                                }
                            }
                            case SMALL -> {
                                if (config.getInt("regionLimit.small") > 5
                                        || BuildProtectionRegionStorage.getRegionBySize(player.getUniqueId(), RegionSize.SMALL).size() > 5)
                                {
                                    content.getMessageAfterPrefix(MessageType.ERROR, "overLapProtectionAmount").ifPresent(message -> {
                                        String replacedMessage = message.replace("%size%", RegionSize.SMALL.getKey());
                                        player.sendMessage(replacedMessage);
                                    });
                                    return;
                                }
                            }
                        }
                        InventoryHandler.openTelePortInventory(player, region);
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                    }
                }
                else if (slot > 8 && slot < 27) {
                    if (event.getCurrentItem() == null) return;
                    List<UUID> owners = new ArrayList<>();
                    owners.addAll(region.getOwners());
                    owners.remove(player.getUniqueId());
                    UUID uuid = owners.get(slot - 9);
                    OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);

                    if (event.getClick().equals(ClickType.SHIFT_LEFT)) {
                        region.getOwners().remove(uuid);

                        content.getMessageAfterPrefix(MessageType.NORMAL, "removeTargetPermission").ifPresent(s -> {
                            String replacedMessage = s.replace("%player%", target.getName());
                            player.sendMessage(replacedMessage);
                        });
                        try {
                            content.getMessageAfterPrefix(MessageType.NORMAL, "removedPermission").ifPresent(s -> {
                                String replacedMessage = s.replace("%player%", player.getDisplayName());
                                target.getPlayer().sendMessage(replacedMessage);
                            });
                        } catch (Exception ignored) {}

                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                        InventoryHandler.changeInventoryMenu(player, region, true);
                    } else if (event.getClick().equals(ClickType.SHIFT_RIGHT)) {
                        region.setBuilder(uuid);

                        content.getMessageAfterPrefix(MessageType.NORMAL, "handOverRegion").ifPresent(s -> {
                            String replacedMessage = s.replace("%player%", target.getName());
                            player.sendMessage(replacedMessage);
                        });
                        try {
                            content.getMessageAfterPrefix(MessageType.NORMAL, "receivedRegion").ifPresent(s -> {
                                String replacedMessage = s.replace("%player%", player.getDisplayName());
                                target.getPlayer().sendMessage(replacedMessage);
                            });
                        } catch (Exception ignored) {}

                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                        InventoryHandler.changeInventoryMenu(player, region, true);
                    }
                }

                else if (slot == 31) {
                    InventoryHandler.openAddPermission(player, region);
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                } else if (slot == 35) {
                    InventoryHandler.openWithDrawMenu(player, region);
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                }
            }

            case ADD_PERMISSION_MENU -> {
                event.setCancelled(true);

                if (event.getSlot() == 45) {
                    content.getMessageAfterPrefix(MessageType.ERROR, "nonExistPrevPage").ifPresent(player::sendMessage);
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                    return;
                } else if (event.getSlot() == 53) {
                    content.getMessageAfterPrefix(MessageType.ERROR, "nonExistNextPage").ifPresent(player::sendMessage);
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                    return;
                }

                if (!event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) return;
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                if (region.getOwners().size() >= config.getInt("region.limitOwner")) {
                    content.getMessageAfterPrefix(MessageType.ERROR, "limitOwner").ifPresent(player::sendMessage);
                    return;
                }

                UUID owner = new ItemBuilder(event.getCurrentItem()).getOwner();
                List<ProtectionRegion> protectionRegions = BuildProtectionRegionStorage.getProtectionRegions();
                protectionRegions.remove(region);
                List<UUID> owners = new ArrayList<>();
                owners.addAll(region.getOwners());
                owners.add(owner);
                region.setOwners(owners);
                protectionRegions.add(region);

                Player target = Bukkit.getPlayer(owner);
                content.getMessageAfterPrefix(MessageType.NORMAL, "giveTargetPermission").ifPresent(s -> {
                    String replacedMessage = s.replace("%player%", target.getDisplayName());
                    player.sendMessage(replacedMessage);
                });
                content.getMessageAfterPrefix(MessageType.NORMAL, "receivePermission").ifPresent(s -> {
                    String replacedMessage = s.replace("%player%", player.getDisplayName());
                    target.sendMessage(replacedMessage);
                });
                InventoryHandler.changeInventoryAddPermission(player, region);
            }

            case WITHDRAW_MENU -> {
                event.setCancelled(true);
                if (slot < 4) {
                    Player builder = Bukkit.getPlayer(region.getBuilder());
                    if (!builder.isOnline()) {
                        content.getMessageAfterPrefix(MessageType.ERROR, "noOneLinePlayer").ifPresent(message -> {
                            String replacedMessage = message.replace("%player%", builder.getName());
                            player.sendMessage(replacedMessage);
                        });
                        return;
                    } else if (!hasSpace(builder.getInventory())) {
                        content.getMessageAfterPrefix(MessageType.ERROR, "noSpaceinPlayer").ifPresent(message -> {
                            String replacedMessage = message.replace("%player%", builder.getName());
                            player.sendMessage(replacedMessage);
                        });
                        return;
                    }

                    regionUtil.withDrawRegion(player, region);
                    player.closeInventory();
                    content.getMessageAfterPrefix(MessageType.NORMAL, "withDrawBuildProjection").ifPresent(player::sendMessage);
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                } else if (slot > 4) {
                    content.getMessageAfterPrefix(MessageType.NORMAL, "rejectWithDraw").ifPresent(player::sendMessage);
                    player.closeInventory();
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                }
            }

            case LIST_MENU -> {
                event.setCancelled(true);
                int currentPage = InventoryHandler.getPlayerPageMap().get(player.getUniqueId());
                if (event.getSlot() == 45) {
                    if (!(0 < currentPage)) {
                        content.getMessageAfterPrefix(MessageType.ERROR, "nonExistPrevPage").ifPresent(player::sendMessage);
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                        return;
                    }

                    currentPage--;
                    InventoryHandler.getPlayerPageMap().put(player.getUniqueId(), currentPage);
                    InventoryHandler.changeProtectionListMenu(player);
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                }
                else if (event.getSlot() == 53) {
                    if (PaginationHolder.getProtectionPageList().size() <= currentPage + 1) {
                        content.getMessageAfterPrefix(MessageType.ERROR, "nonExistNextPage").ifPresent(player::sendMessage);
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                        return;
                    }

                    currentPage++;
                    InventoryHandler.getPlayerPageMap().put(player.getUniqueId(), currentPage);
                    InventoryHandler.changeProtectionListMenu(player);
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                }

                if (!event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) return;

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                ItemBuilder itemBuilder = new ItemBuilder(event.getCurrentItem());
                ProtectionRegion currentRegion = BuildProtectionRegionStorage.getRegion(
                        UUID.fromString(itemBuilder.getPDC()));

                // 건차 정보
                if (event.getClick().equals(ClickType.LEFT)) {
                    InventoryHandler.openMenu(player, currentRegion, true);
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                }
                // 건차 이동
                else if (event.getClick().equals(ClickType.RIGHT)) {
                    player.teleport(currentRegion.getHead());
                    content.getMessageAfterPrefix(MessageType.NORMAL, "telePortRegion").ifPresent(message -> {
                        String replacedMessage = message.replace("%player%", Bukkit.getOfflinePlayer(currentRegion.getBuilder()).getName());
                        player.sendMessage(replacedMessage);
                    });
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                }
                // 건차 주인 변경
                else if (event.getClick().equals(ClickType.SHIFT_LEFT)) {
                    event.setCancelled(true);
                    PlayerChatListener.getChatMap().put(player.getUniqueId(), currentRegion);
                    content.getMessageAfterPrefix(MessageType.NORMAL, "inputPlayer").ifPresent(player::sendMessage);
                    player.closeInventory();

                    Bukkit.getScheduler().runTaskLater(BuildProtection.getInstance(), () -> {
                        if (PlayerChatListener.getChatMap().containsKey(player.getUniqueId())) {
                            PlayerChatListener.getChatMap().remove(player.getUniqueId());
                            content.getMessageAfterPrefix(MessageType.ERROR, "expireBuilderChange").ifPresent(player::sendMessage);
                        }
                    }, 10 * 20);
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                }
                // 건차 삭제
                else if (event.getClick().equals(ClickType.SHIFT_RIGHT)) {
                    if (Bukkit.getEntity(currentRegion.getEntityUUID()) == null) {
                        content.getMessageAfterPrefix(MessageType.ERROR, "notAroundProtection").ifPresent(message -> {
                            String replacedMessage = message.replace("%player%", Bukkit.getOfflinePlayer(currentRegion.getBuilder()).getName());
                            player.sendMessage(replacedMessage);
                        });
                        return;
                    }

                    event.setCancelled(true);
                    regionUtil.deleteRegion(currentRegion);
                    InventoryHandler.changeProtectionListMenu(player);
                    content.getMessageAfterPrefix(MessageType.NORMAL, "deleteRegion").ifPresent(message -> {
                        String replacedMessage = message.replace("%player%", Bukkit.getOfflinePlayer(currentRegion.getBuilder()).getName());
                        player.sendMessage(replacedMessage);
                    });
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
                } else return;

            }

            case TELEPORT_MENU -> {
                event.setCancelled(true);
                String pdc = new ItemBuilder(event.getCurrentItem()).getPDC();
                ProtectionRegion current = BuildProtectionRegionStorage.getRegion(UUID.fromString(pdc));
                if (!event.getClick().equals(ClickType.LEFT)) return;
                if (current.equals(region)) {
                    content.getMessageAfterPrefix(MessageType.ERROR, "alreadyRegion").ifPresent(player::sendMessage);
                    return;
                }

                player.teleport(current.getHead());
                content.getMessageAfterPrefix(MessageType.NORMAL, "telePort").ifPresent(message -> {
                    String replacedMessage = message.replace("%size%", current.getSize().getKey());
                    player.sendMessage(replacedMessage);
                });
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 2);
            }

            case MENU_WITHOUT_PERMISSION -> {
                event.setCancelled(true);
                if (!(slot > 8 && slot < 27)) return;
                if (Bukkit.getOfflinePlayer(new ItemBuilder(event.getCurrentItem()).getOwner()).getUniqueId().equals(player.getUniqueId())) {
                    if (!event.getClick().equals(ClickType.SHIFT_LEFT)) return;
                    region.getOwners().remove(player.getUniqueId());
                    InventoryHandler.changeInventoryMenu(player, region, false);

                    content.getMessageAfterPrefix(MessageType.NORMAL, "withdrawSelf").ifPresent(message -> {
                        String replacedMessage = message.replace("%player%", Bukkit.getOfflinePlayer(region.getBuilder()).getName());
                        player.sendMessage(replacedMessage);
                    });
                    content.getMessageAfterPrefix(MessageType.NORMAL, "withdrawPlayer").ifPresent(message -> {
                        String replacedMessage = message.replace("%player%", player.getName());
                        if (Bukkit.getOfflinePlayer(region.getBuilder()).isOnline()) {
                            Bukkit.getOfflinePlayer(region.getBuilder()).getPlayer().sendMessage(replacedMessage);
                        }
                    });
                }
            }
        }
    }

    private boolean hasSpace(Inventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getItem(i) == null) return true;
        }
        return false;
    }
}
