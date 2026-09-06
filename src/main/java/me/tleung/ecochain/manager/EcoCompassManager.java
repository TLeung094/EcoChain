package me.tleung.ecochain.manager;

import me.tleung.ecochain.EcoChain;
import me.tleung.ecochain.data.EcoRegionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class EcoCompassManager implements Listener {
    private final EcoChain plugin;
    private final ChunkManager chunkManager;
    private final NamespacedKey compassKey;
    private static final String GUI_TITLE = "§2🌿 生態探測儀表板";

    public EcoCompassManager(EcoChain plugin, ChunkManager chunkManager) {
        this.plugin = plugin;
        this.chunkManager = chunkManager;
        this.compassKey = new NamespacedKey(plugin, "eco_compass");
    }

    /**
     * 建立一支給玩家的生態羅盤
     */
    public ItemStack createEcoCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "🌱 生態探測羅盤");
            meta.setLore(List.of(
                    ChatColor.GRAY + "右鍵點擊以開啟生態儀表板",
                    ChatColor.DARK_GREEN + "EcoChain 專用探測儀"
            ));
            meta.getPersistentDataContainer().set(compassKey, PersistentDataType.BYTE, (byte) 1);
            compass.setItemMeta(meta);
        }
        return compass;
    }

    /**
     * 偵測玩家右鍵使用生態羅盤，開啟 GUI
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Byte tag = meta.getPersistentDataContainer().get(compassKey, PersistentDataType.BYTE);
        if (tag == null || tag != 1) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        // 開啟生態 GUI
        openEcoGui(player);
    }

    /**
     * 開啟 27 格的生態儀表板 GUI
     */
    public void openEcoGui(Player player) {
        EcoRegionData data = chunkManager.getRegionData(player.getLocation().getChunk());
        if (data == null) {
            player.sendMessage("§c正在加載當前區域的生態數據，請稍後再試...");
            return;
        }

        int flora = data.getFlora().get();
        int fauna = data.getFauna().get();
        int aqua = data.getAqua().get();
        int overall = data.getOverallHealth();

        // 決定生態階段顯示
        Material stageIcon;
        String stageName;
        if (overall >= 80) { stageIcon = Material.EMERALD_BLOCK; stageName = "§a🌟 豐饒淨土"; }
        else if (overall >= 50) { stageIcon = Material.OAK_SAPLING; stageName = "§2🌿 和諧林地"; }
        else if (overall >= 0) { stageIcon = Material.WHEAT; stageName = "§e🌾 平凡田野"; }
        else if (overall >= -30) { stageIcon = Material.DEAD_BUSH; stageName = "§6🏜️ 荒蕪之地"; }
        else { stageIcon = Material.WITHER_ROSE; stageName = "§8☠️ 死寂廢土"; }

        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        // 1. 綠化度指示物 (第 11 格)
        ItemStack floraItem = new ItemStack(Material.OAK_LEAVES);
        ItemMeta floraMeta = floraItem.getItemMeta();
        if (floraMeta != null) {
            floraMeta.setDisplayName("§a🌱 綠化度: " + flora);
            floraMeta.setLore(List.of("§7數值範圍: -100 ~ 100", "§7透過種植樹苗與花卉提升"));
            floraItem.setItemMeta(floraMeta);
        }
        inv.setItem(11, floraItem);

        // 2. 動物親和度指示物 (第 13 格)
        ItemStack faunaItem = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta faunaMeta = faunaItem.getItemMeta();
        if (faunaMeta != null) {
            faunaMeta.setDisplayName("§d🐾 動物親和度: " + fauna);
            faunaMeta.setLore(List.of("§7數值範圍: -100 ~ 100", "§7透過繁育動物提升"));
            faunaItem.setItemMeta(faunaMeta);
        }
        inv.setItem(13, faunaItem);

        // 3. 水源純淨度指示物 (第 15 格)
        ItemStack aquaItem = new ItemStack(Material.WATER_BUCKET);
        ItemMeta aquaMeta = aquaItem.getItemMeta();
        if (aquaMeta != null) {
            aquaMeta.setDisplayName("§b💧 水源純淨度: " + aqua);
            aquaMeta.setLore(List.of("§7數值範圍: -100 ~ 100", "§7放置乾淨水源或避免倒岩漿維持"));
            aquaItem.setItemMeta(aquaMeta);
        }
        inv.setItem(15, aquaItem);

        // 4. 中央下方顯示總體生態階段 (第 22 格)
        ItemStack statusItem = new ItemStack(stageIcon);
        ItemMeta statusMeta = statusItem.getItemMeta();
        if (statusMeta != null) {
            statusMeta.setDisplayName("§f🌟 當前生態階段: " + stageName);
            statusMeta.setLore(List.of("§7總體健康評分: " + overall));
            statusItem.setItemMeta(statusMeta);
        }
        inv.setItem(22, statusItem);

        player.openInventory(inv);
    }

    /**
     * 防止玩家在 GUI 裡面把道具拿走
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true);
        }
    }
}