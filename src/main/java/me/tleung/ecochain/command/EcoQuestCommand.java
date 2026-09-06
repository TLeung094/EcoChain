package me.tleung.ecochain.command;

import me.tleung.ecochain.manager.PlayerQuestManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EcoQuestCommand implements CommandExecutor {

    private final PlayerQuestManager questManager;

    public EcoQuestCommand(PlayerQuestManager questManager) {
        this.questManager = questManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用此指令！");
            return true;
        }

        YamlConfiguration config = questManager.getPlayerData(player.getUniqueId());
        if (config == null) {
            player.sendMessage("§c正在讀取任務資料，請稍後再試...");
            return true;
        }

        String type = config.getString("daily-quest.type", "NONE");
        if (type.equals("NONE")) {
            player.sendMessage("§7你今天沒有任何生態任務。");
            return true;
        }

        int target = config.getInt("daily-quest.target-amount", 0);
        int progress = config.getInt("daily-quest.current-progress", 0);
        boolean completed = config.getBoolean("daily-quest.completed", false);

        String desc = switch (type) {
            case "PLANT_TREE" -> "在世界上種植樹苗";
            case "BREED_ANIMAL" -> "繁衍動物生命";
            case "CLEAN_LAVA" -> "清理危險的岩漿源";
            default -> "未知任務";
        };

        String progressBar = generateProgressBar(progress, target);

        player.sendMessage(" ");
        player.sendMessage("§a📜 §l今日生態任務");
        player.sendMessage("§8-----------------------------------");
        player.sendMessage("§f目標：§7" + desc);
        player.sendMessage("§f進度：§e" + progress + " §f/ §e" + target);
        player.sendMessage(progressBar);
        player.sendMessage(" ");
        if (completed) {
            player.sendMessage("§a✅ 任務已完成！獎勵已發放。");
        } else {
            player.sendMessage("§c❌ 尚未完成 §7(獎勵: 50 經驗值)");
        }
        player.sendMessage("§8-----------------------------------");
        player.sendMessage(" ");

        return true;
    }

    private String generateProgressBar(int current, int target) {
        int totalBars = 20;
        int filledBars = (int) Math.floor((double) current / target * totalBars);
        filledBars = Math.min(filledBars, totalBars);

        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                bar.append("█");
            } else if (i == filledBars) {
                bar.append("§7").append("░");
            } else {
                bar.append("░");
            }
        }
        return bar.toString();
    }
}