package me.tleung.ecochain.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.tleung.ecochain.EcoChain;
import me.tleung.ecochain.data.EcoRegionData;
import me.tleung.ecochain.manager.ChunkManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EcoDecayTask {
    private final EcoChain plugin;
    private final ChunkManager chunkManager;
    private ScheduledTask task;

    public EcoDecayTask(EcoChain plugin, ChunkManager chunkManager) {
        this.plugin = plugin;
        this.chunkManager = chunkManager;
    }

    public void start() {
        long intervalTicks = plugin.getConfig().getLong("decay.interval-ticks", 12000L);
        long intervalMillis = (intervalTicks / 20) * 1000;

        task = plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, t -> {
            int decayAmount = plugin.getConfig().getInt("decay.amount", 2);

            for (Map.Entry<String, EcoRegionData> entry : chunkManager.getAllActiveRegions().entrySet()) {
                String key = entry.getKey();
                EcoRegionData data = entry.getValue();

                String[] parts = key.split("_");
                World world = Bukkit.getWorld(parts[0]);
                if (world == null) continue;

                int chunkX = Integer.parseInt(parts[1]);
                int chunkZ = Integer.parseInt(parts[2]);
                int blockX = chunkX << 4;
                int blockZ = chunkZ << 4;

                plugin.getServer().getRegionScheduler().run(plugin, world, blockX, blockZ, st -> {
                    applyDecay(data.getFlora(), decayAmount);
                    applyDecay(data.getFauna(), decayAmount);
                    applyDecay(data.getAqua(), decayAmount);

                    if (data.getOverallHealth() <= -50) {
                        applyWitheringMiasma(world, chunkX, chunkZ);
                    }
                });
            }
        }, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    private void applyDecay(java.util.concurrent.atomic.AtomicInteger value, int amount) {
        int current = value.get();
        if (current > 0) {
            value.set(Math.max(0, current - amount));
        } else if (current < 0) {
            value.set(Math.min(0, current + amount));
        }
    }

    private void applyWitheringMiasma(World world, int chunkX, int chunkZ) {
        for (Player player : world.getPlayers()) {
            if (player.getLocation().getChunk().getX() == chunkX &&
                    player.getLocation().getChunk().getZ() == chunkZ) {

                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 600, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 600, 1));
                player.sendMessage("§8[生態警報] 你所在的區域生態已經枯竭，周遭充滿了枯萎瘴氣...");
            }
        }
    }

    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}