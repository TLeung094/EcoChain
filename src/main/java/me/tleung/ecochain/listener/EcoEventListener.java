package me.tleung.ecochain.listener;

import me.tleung.ecochain.EcoChain;
import me.tleung.ecochain.data.EcoRegionData;
import me.tleung.ecochain.manager.ChunkManager;
import me.tleung.ecochain.manager.PlayerQuestManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class EcoEventListener implements Listener {
    private final EcoChain plugin;
    private final ChunkManager chunkManager;
    private final PlayerQuestManager questManager;

    public EcoEventListener(EcoChain plugin, ChunkManager chunkManager, PlayerQuestManager questManager) {
        this.plugin = plugin;
        this.chunkManager = chunkManager;
        this.questManager = questManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        EcoRegionData data = chunkManager.getRegionData(event.getBlock().getChunk());
        if (data == null) return;

        Material type = event.getBlock().getType();
        String name = type.name();

        // 1. 擴充綠化度判定：涵蓋所有樹苗、花卉、蘑菇、各類農作物（小麥、胡蘿蔔、馬鈴薯、甜菜根等）
        boolean isFloraItem = name.contains("SAPLING")
                || name.contains("FLOWER")
                || name.contains("MUSHROOM")
                || name.contains("WHEAT")
                || name.contains("CARROT")
                || name.contains("POTATO")
                || name.contains("BEETROOT")
                || name.contains("MELON")
                || name.contains("PUMPKIN")
                || name.contains("BAMBOO")
                || name.contains("SUGAR_Cane")
                || name.contains("SWEET_BERRY")
                || name.contains("CACTUS");

        if (isFloraItem) {
            int bonus = plugin.getConfig().getInt("weights.flora.plant-sapling", 2);
            data.getFlora().updateAndGet(v -> Math.min(100, v + bonus));

            // 2. 擴充任務判定：只要是樹苗、種子或植物類放置，都可以算入「種樹/種植」任務
            if (name.contains("SAPLING") || name.contains("WHEAT") || name.contains("CARROT") || name.contains("POTATO") || name.contains("BEETROOT")) {
                checkAndAdvanceQuest(event.getPlayer(), "PLANT_TREE");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        EcoRegionData data = chunkManager.getRegionData(event.getBlock().getChunk());
        if (data == null) return;

        String name = event.getBlock().getType().name();
        if (name.contains("LOG") || name.contains("WOOD")) {
            int penalty = plugin.getConfig().getInt("weights.flora.break-log", -1);
            data.getFlora().updateAndGet(v -> Math.max(-100, v + penalty));
        }
    }

    @EventHandler
    public void onEntityBreed(EntityBreedEvent event) {
        EcoRegionData data = chunkManager.getRegionData(event.getEntity().getChunk());
        if (data != null) {
            int bonus = plugin.getConfig().getInt("weights.fauna.breed-animal", 3);
            data.getFauna().updateAndGet(v -> Math.min(100, v + bonus));

            if (event.getBreeder() instanceof Player player) {
                checkAndAdvanceQuest(player, "BREED_ANIMAL");
            }
        }
    }

    @EventHandler
    public void onAnimalKill(EntityDeathEvent event) {
        if (event.getEntity() instanceof Animals && event.getEntity().getKiller() != null) {
            EcoRegionData data = chunkManager.getRegionData(event.getEntity().getChunk());
            if (data != null) {
                int penalty = plugin.getConfig().getInt("weights.fauna.kill-animal", -2);
                data.getFauna().updateAndGet(v -> Math.max(-100, v + penalty));
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        EcoRegionData data = chunkManager.getRegionData(event.getBlock().getChunk());
        if (data == null) return;

        Material bucketType = event.getBucket();
        if (bucketType == Material.WATER_BUCKET || bucketType == Material.AXOLOTL_BUCKET || bucketType == Material.COD_BUCKET) {
            int bonus = plugin.getConfig().getInt("weights.aqua.place-water", 2);
            data.getAqua().updateAndGet(v -> Math.min(100, v + bonus));
        } else if (bucketType == Material.LAVA_BUCKET) {
            int penalty = plugin.getConfig().getInt("weights.aqua.place-lava", -5);
            data.getAqua().updateAndGet(v -> Math.max(-100, v + penalty));
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        EcoRegionData data = chunkManager.getRegionData(event.getBlock().getChunk());
        if (data == null) return;
        if (event.getBlock().getType() == Material.WATER) {
            int penalty = plugin.getConfig().getInt("weights.aqua.pickup-water", -1);
            data.getAqua().updateAndGet(v -> Math.max(-100, v + penalty));
        } else if (event.getBlock().getType() == Material.LAVA) {
            checkAndAdvanceQuest(event.getPlayer(), "CLEAN_LAVA");
        }
    }

    private void checkAndAdvanceQuest(Player player, String targetQuestType) {
        YamlConfiguration config = questManager.getPlayerData(player.getUniqueId());
        if (config == null) return;

        String currentQuestType = config.getString("daily-quest.type", "NONE");
        if (!currentQuestType.equals(targetQuestType)) return;
        if (config.getBoolean("daily-quest.completed", false)) return;

        int target = config.getInt("daily-quest.target-amount", 10);
        int progress = config.getInt("daily-quest.current-progress", 0);

        progress++;
        config.set("daily-quest.current-progress", progress);

        if (progress >= target) {
            config.set("daily-quest.completed", true);
            int expReward = plugin.getConfig().getInt("quest.exp-reward", 50);
            player.giveExp(expReward);

            player.sendMessage("§a🎉 恭喜！你完成了今日的生態任務，獲得 " + expReward + " 點經驗值！");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        } else {
            player.sendActionBar("§a任務進度: " + progress + " / " + target);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
        }

        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            questManager.saveConfig(player.getUniqueId(), config);
        });
    }
}