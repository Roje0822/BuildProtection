package xyz.chide1.buildprotection.command.tabcomplete;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildProtectionTabComplete implements TabCompleter {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        final Player player = (Player) sender;
        final List<String> tabList = new ArrayList<>();

        if (!player.isOp()) return Collections.emptyList();

        if (args.length == 1) {
            tabList.addAll(List.of("목록", "리로드", "관리자", "설정"));
            return StringUtil.copyPartialMatches(args[0], tabList, new ArrayList<>());
        } else if (args.length == 2 && args[0].equals("설정")) {
            tabList.addAll(List.of("권한", "일반"));
            return StringUtil.copyPartialMatches(args[1], tabList, new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
