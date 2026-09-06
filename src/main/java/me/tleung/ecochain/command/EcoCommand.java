package me.tleung.ecochain.command;

import me.tleung.ecochain.data.EcoRegionData;
import me.tleung.ecochain.manager.ChunkManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EcoCommand implements CommandExecutor {

    private final ChunkManager chunkManager;

    public EcoCommand(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用此指令！");
            return true;
        }

        EcoRegionData data = chunkManager.getRegionData(player.getLocation().getChunk());
        if (data == null) {
            player.sendMessage("§c正在加載當前區域的生態數據，請稍後再試...");
            return true;
        }

        int flora = data.getFlora().get();
        int fauna = data.getFauna().get();
        int aqua = data.getAqua().get();
        int overall = data.getOverallHealth();

        String stageName;
        String stageColor;
        if (overall >= 80) { stageName = "🌟 豐饒淨土"; stageColor = "§a"; }
        else if (overall >= 50) { stageName = "🌿 和諧林地"; stageColor = "§2"; }
        else if (overall >= 0) { stageName = "🌾 平凡田野"; stageColor = "§e"; }
        else if (overall >= -30) { stageName = "🏜️ 荒蕪之地"; stageColor = "§6"; }
        else { stageName = "☠️ 死寂廢土"; stageColor = "§8"; }

        String biomeName = player.getLocation().getBlock().getBiome().name();

        player.sendMessage("§2╔═══════════════════════════════════════╗");
        player.sendMessage("§2║        §a🌿 §f當前區域生態報告            §2║");
        player.sendMessage("§2╠═══════════════════════════════════════╣");
        player.sendMessage("§2║  §f位置：§7X: " + (player.getLocation().getChunk().getX() * 16) + "  Z: " + (player.getLocation().getChunk().getZ() * 16) + "  §f群系：§7" + biomeName);
        player.sendMessage("§2╠═══════════════════════════════════════╣");
        player.sendMessage("§2║  §a🌱 綠化度     §f" + generateBar(flora) + "  §a" + flora);
        player.sendMessage("§2║  §d🐾 動物親和度 §f" + generateBar(fauna) + "  §d" + fauna);
        player.sendMessage("§2║  §b💧 水源純淨度 §f" + generateBar(aqua) + "  §b" + aqua);
        player.sendMessage("§2╠═══════════════════════════════════════╣");
        player.sendMessage("§2║  §f🌟 生態階段：" + stageColor + stageName);
        player.sendMessage("§2╚═══════════════════════════════════════╝");

        return true;
    }

    private String generateBar(int value) {
        int normalized = (value + 100) / 20;
        normalized = Math.max(0, Math.min(10, normalized));

        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < 10; i++) {
            if (i < normalized) {
                bar.append("█");
            } else if (i == normalized) {
                bar.append("§7").append("░");
            } else {
                bar.append("░");
            }
        }
        return bar.toString();
    }
}