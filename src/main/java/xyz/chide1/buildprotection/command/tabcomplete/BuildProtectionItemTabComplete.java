package xyz.chide1.buildprotection.command.tabcomplete;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildProtectionItemTabComplete implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        final Player player = (Player) sender;
        final List<String> tabList = new ArrayList<>();

        if (!player.isOp()) return Collections.emptyList();

        if (args.length == 1) {
            tabList.addAll(List.of("생성", "머리", "테두리바닥", "테두리나머지", "지급", "머리지급"));
            return StringUtil.copyPartialMatches(args[0], tabList, new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equals("생성") || args[0].equals("지급")) {
                tabList.addAll(List.of("Big", "Normal", "Small"));
                return StringUtil.copyPartialMatches(args[1], tabList, new ArrayList<>());
            }
        }

        if (args.length == 3) {
            if (args[0].equals("지급")) {
                tabList.add("<개수>");
                return StringUtil.copyPartialMatches(args[2], tabList, new ArrayList<>());
            }
        }

        return Collections.emptyList();
    }
}
