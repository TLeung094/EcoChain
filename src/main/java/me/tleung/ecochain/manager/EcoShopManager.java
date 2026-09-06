package me.tleung.ecochain.manager;

import me.tleung.ecochain.EcoChain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class EcoShopManager implements Listener {
    private final EcoChain plugin;
    private static final String SHOP_TITLE = "§2🛍️ 生態點數商店";

    public EcoShopManager(EcoChain plugin) {
        this.plugin = plugin;
    }

    /**
     * 開啟生態商店 GUI
     */
    public void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, SHOP_TITLE);

        // 1. 櫻花樹苗 (售價: 30 經驗值)
        ItemStack cherrySapling = new ItemStack(Material.CHERRY_SAPLING);
        ItemMeta cherryMeta = cherrySapling.getItemMeta();
        if (cherryMeta != null) {
            cherryMeta.setDisplayName("§d🌸 櫻花樹苗");
            cherryMeta.setLore(List.of("§7稀有景觀樹木", "§e售價: 30 點經驗值", "§a點擊以兌換"));
            cherrySapling.setItemMeta(cherryMeta);
        }
        inv.setItem(11, cherrySapling);

        // 2. 紅樹林 propagule / 樹苗 (對應 1.21 材質，此處用曼格rove propagule)
        ItemStack mangroveSapling = new ItemStack(Material.MANGROVE_PROPAGULE);
        ItemMeta mangroveMeta = mangroveSapling.getItemMeta();
        if (mangroveMeta != null) {
            mangroveMeta.setDisplayName("§c mangrove 樹苗 (紅樹林)");
            mangroveMeta.setLore(List.of("§7潮濕環境專屬樹苗", "§e售價: 30 點經驗值", "§a點擊以兌換"));
            mangroveSapling.setItemMeta(mangroveMeta);
        }
        inv.setItem(12, mangroveSapling);

        // 3. 熊貓生怪蛋 (售價: 100 經驗值)
        ItemStack pandaEgg = new ItemStack(Material.PANDA_SPAWN_EGG);
        ItemMeta pandaMeta = pandaEgg.getItemMeta();
        if (pandaMeta != null) {
            pandaMeta.setDisplayName("§f🐼 熊貓生怪蛋");
            pandaMeta.setLore(List.of("§7珍貴動物生命", "§e售價: 100 點經驗值", "§a點擊以兌換"));
            pandaEgg.setItemMeta(pandaMeta);
        }
        inv.setItem(14, pandaEgg);

        // 4. 北極熊生怪蛋 (售價: 100 經驗值)
        ItemStack polarBearEgg = new ItemStack(Material.POLAR_BEAR_SPAWN_EGG);
        ItemMeta polarBearMeta = polarBearEgg.getItemMeta();
        if (polarBearMeta != null) {
            polarBearMeta.setDisplayName("§b🐻‍❄️ 北極熊生怪蛋");
            polarBearMeta.setLore(List.of("§7極地生態動物", "§e售價: 100 經驗值", "§a點擊以兌換"));
            polarBearEgg.setItemMeta(polarBearMeta);
        }
        inv.setItem(15, polarBearEgg);

        player.openInventory(inv);
    }

    /**
     * 處理商店點擊與交易邏輯
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(SHOP_TITLE)) return;
        event.setCancelled(true); // 防止玩家拿走介面上的道具

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        int cost = 0;
        ItemStack rewardItem = null;

        switch (clickedItem.getType()) {
            case CHERRY_SAPLING:
                cost = 30;
                rewardItem = new ItemStack(Material.CHERRY_SAPLING);
                break;
            case MANGROVE_PROPAGULE:
                cost = 30;
                rewardItem = new ItemStack(Material.MANGROVE_PROPAGULE);
                break;
            case PANDA_SPAWN_EGG:
                cost = 100;
                rewardItem = new ItemStack(Material.PANDA_SPAWN_EGG);
                break;
            case POLAR_BEAR_SPAWN_EGG:
                cost = 100;
                rewardItem = new ItemStack(Material.POLAR_BEAR_SPAWN_EGG);
                break;
            default:
                return;
        }

        // 檢查玩家經驗值是否足夠（此處以玩家總經驗點數或等級作為簡易貨幣，示範使用玩家總經驗）
        if (player.getTotalExperience() < cost) {
            player.sendMessage("§c❌ 你的經驗/生態點數不足！需要 " + cost + " 點。");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // 扣除經驗值並給予物品
        player.setTotalExperience(player.getTotalExperience() - cost);
        player.setLevel(0); // 簡化處理，扣除後重新計算或直接給予道具
        // 給予玩家獎勵物品
        player.getInventory().addItem(rewardItem);
        player.sendMessage("§a🎉 兌換成功！已扣除 " + cost + " 點並將物品放入你的背包。");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }
}