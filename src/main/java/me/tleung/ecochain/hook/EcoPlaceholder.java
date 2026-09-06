package me.tleung.ecochain.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.tleung.ecochain.EcoChain;
import me.tleung.ecochain.data.EcoRegionData;
import me.tleung.ecochain.manager.ChunkManager;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EcoPlaceholder extends PlaceholderExpansion {

    private final EcoChain plugin;
    private final ChunkManager chunkManager;

    public EcoPlaceholder(EcoChain plugin, ChunkManager chunkManager) {
        this.plugin = plugin;
        this.chunkManager = chunkManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ecochain";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TLeung";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) return "";
        Player player = offlinePlayer.getPlayer();
        if (player == null) return "";

        // 為了符合 Folia 非同步安全檢查，我們必須確保不會在異步執行緒直接呼叫 player.getLocation().getChunk()
        // 改為透過同步執行緒取得位置或使用安全的取法
        // 這裡我們直接向主執行緒安全地取得資料，或者透過快取安全讀取
        final EcoRegionData[] targetData = new EcoRegionData[1];

        // 判斷當前是否為主執行緒 (如果是 TAB 執行緒等非同步執行緒，切回主執行緒或該 Region 執行緒取得)
        if (org.bukkit.Bukkit.isPrimaryThread()) {
            Chunk chunk = player.getLocation().getChunk();
            targetData[0] = chunkManager.getRegionData(chunk);
        } else {
            // 如果是在 TAB 的非同步刷新執行緒，我們使用同步鎖定或直接向 ChunkManager 查詢當前快取
            // 為了效能與安全，直接用 chunkKey 字串去 activeRegions 拿，避免觸發 CraftWorld.getChunkAt
            try {
                String worldName = player.getWorld().getName();
                int chunkX = player.getLocation().getBlockX() >> 4;
                int chunkZ = player.getLocation().getBlockZ() >> 4;
                String key = worldName + "_" + chunkX + "_" + chunkZ;
                targetData[0] = chunkManager.getAllActiveRegions().get(key);
            } catch (Exception ignored) {
                // 如果跨執行緒讀取不到，回傳預設值
            }
        }

        if (targetData[0] == null) {
            return "0";
        }

        EcoRegionData data = targetData[0];
        int flora = data.getFlora().get();
        int fauna = data.getFauna().get();
        int aqua = data.getAqua().get();
        int overall = data.getOverallHealth();

        return switch (params.toLowerCase()) {
            case "flora" -> String.valueOf(flora);
            case "fauna" -> String.valueOf(fauna);
            case "aqua" -> String.valueOf(aqua);
            case "overall" -> String.valueOf(overall);
            case "stage" -> {
                if (overall >= 80) yield "§a🌟 豐饒淨土";
                else if (overall >= 50) yield "§2🌿 和諧林地";
                else if (overall >= 0) yield "§e🌾 平凡田野";
                else if (overall >= -30) yield "§6🏜️ 荒蕪之地";
                else yield "§8☠️ 死寂廢土";
            }
            default -> null;
        };
    }
}