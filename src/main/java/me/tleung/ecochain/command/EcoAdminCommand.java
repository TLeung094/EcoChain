package me.tleung.ecochain.command;

import me.tleung.ecochain.EcoChain;
import me.tleung.ecochain.data.EcoRegionData;
import me.tleung.ecochain.manager.ChunkManager;
import me.tleung.ecochain.manager.EcoCompassManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EcoAdminCommand implements CommandExecutor, TabCompleter {

    private final EcoChain plugin;
    private final ChunkManager chunkManager;
    private final EcoCompassManager compassManager;

    public EcoAdminCommand(EcoChain plugin, ChunkManager chunkManager, EcoCompassManager compassManager) {
        this.plugin = plugin;
        this.chunkManager = chunkManager;
        this.compassManager = compassManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用此指令！");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // 處理 reload 指令
        if (subCommand.equals("reload")) {
            plugin.reloadConfig();
            player.sendMessage("§a[EcoAdmin] config.yml 設定檔已重新載入！");
            return true;
        }

        // 處理 compass 指令 (領取生態羅盤)
        if (subCommand.equals("compass")) {
            player.getInventory().addItem(compassManager.createEcoCompass());
            player.sendMessage("§a[EcoAdmin] 你獲得了一支生態探測羅盤！");
            return true;
        }

        // 獲取玩家當前所在的 Chunk 數據
        EcoRegionData data = chunkManager.getRegionData(player.getLocation().getChunk());
        if (data == null) {
            player.sendMessage("§c無法獲取當前區域的生態數據，請稍後再試。");
            return true;
        }

        switch (subCommand) {
            case "reset":
                data.getFlora().set(0);
                data.getFauna().set(0);
                data.getAqua().set(0);
                player.sendMessage("§a[EcoAdmin] 已將當前區域的生態數值全部重置為 0。");
                break;

            case "set":
                if (args.length < 3) {
                    player.sendMessage("§c用法: /ecoadmin set <flora|fauna|aqua> <數值>");
                    return true;
                }
                String type = args[1].toLowerCase();
                int value;
                try {
                    value = Integer.parseInt(args[2]);
                    value = Math.max(-100, Math.min(100, value));
                } catch (NumberFormatException e) {
                    player.sendMessage("§c請輸入有效的數字！");
                    return true;
                }

                switch (type) {
                    case "flora":
                        data.getFlora().set(value);
                        player.sendMessage("§a[EcoAdmin] 當前區域的 §2綠化度 §a已設定為 §f" + value);
                        break;
                    case "fauna":
                        data.getFauna().set(value);
                        player.sendMessage("§a[EcoAdmin] 當前區域的 §d動物親和度 §a已設定為 §f" + value);
                        break;
                    case "aqua":
                        data.getAqua().set(value);
                        player.sendMessage("§a[EcoAdmin] 當前區域的 §b水源純淨度 §a已設定為 §f" + value);
                        break;
                    default:
                        player.sendMessage("§c未知的屬性，請使用 flora, fauna 或 aqua。");
                }
                break;

            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(" ");
        player.sendMessage("§e=== EcoChain 管理員指令 ===");
        player.sendMessage("§6/ecoadmin set <flora|fauna|aqua> <數值> §f- 設定當前區域的生態值");
        player.sendMessage("§6/ecoadmin reset §f- 重置當前區域的生態值為 0");
        player.sendMessage("§6/ecoadmin compass §f- 獲得一支生態探測羅盤");
        player.sendMessage("§6/ecoadmin reload §f- 重新載入 config.yml 設定檔");
        player.sendMessage(" ");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "reset", "compass", "reload"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(Arrays.asList("flora", "fauna", "aqua"));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(Arrays.asList("100", "60", "0", "-50", "-100"));
        }
        return completions;
    }
}