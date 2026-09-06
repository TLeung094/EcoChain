package me.tleung.ecochain.listener;

import me.tleung.ecochain.EcoChain;
import me.tleung.ecochain.data.EcoRegionData;
import me.tleung.ecochain.manager.ChunkManager;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class EcoFeatureListener implements Listener {
    private final EcoChain plugin;
    private final ChunkManager chunkManager;

    public EcoFeatureListener(EcoChain plugin, ChunkManager chunkManager) {
        this.plugin = plugin;
        this.chunkManager = chunkManager;
    }

    @EventHandler
    public void onCropOrSaplingGrow(BlockGrowEvent event) {
        EcoRegionData data = chunkManager.getRegionData(event.getBlock().getChunk());
        if (data == null || data.getFlora().get() < 60) return;

        BlockState newState = event.getNewState();

        if (newState.getBlockData() instanceof Ageable ageable) {
            if (ThreadLocalRandom.current().nextDouble() <= 0.30) {
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    ageable.setAge(Math.min(ageable.getMaximumAge(), ageable.getAge() + 1));
                    newState.setBlockData(ageable);
                    spawnParticleFeedback(event.getBlock().getLocation().add(0.5, 0.5, 0.5), Particle.HAPPY_VILLAGER);
                }
            }
        }
        else if (newState.getBlockData() instanceof Sapling sapling) {
            if (ThreadLocalRandom.current().nextDouble() <= 0.25) {
                if (sapling.getStage() < sapling.getMaximumStage()) {
                    sapling.setStage(sapling.getMaximumStage());
                    newState.setBlockData(sapling);
                    spawnParticleFeedback(event.getBlock().getLocation().add(0.5, 0.5, 0.5), Particle.HAPPY_VILLAGER);
                }
            }
        }
    }

    @EventHandler
    public void onCropHarvest(BlockDropItemEvent event) {
        EcoRegionData data = chunkManager.getRegionData(event.getBlock().getChunk());
        if (data == null || data.getFlora().get() < 60) return;

        BlockState state = event.getBlockState();
        if (state.getBlockData() instanceof Ageable crop) {
            if (crop.getAge() == crop.getMaximumAge()) {
                if (ThreadLocalRandom.current().nextDouble() <= 0.15) {
                    for (Item drop : event.getItems()) {
                        event.getBlock().getWorld().dropItemNaturally(
                                event.getBlock().getLocation(),
                                drop.getItemStack().clone()
                        );
                    }
                    spawnParticleFeedback(event.getBlock().getLocation().add(0.5, 0.5, 0.5), Particle.HAPPY_VILLAGER);
                }
            }
        }
    }

    @EventHandler
    public void onAnimalBreed(EntityBreedEvent event) {
        EcoRegionData data = chunkManager.getRegionData(event.getEntity().getChunk());
        if (data == null || data.getFauna().get() < 60) return;

        event.getEntity().getServer().getRegionScheduler().runDelayed(
                plugin,
                event.getEntity().getLocation(),
                task -> {
                    if (event.getMother() instanceof org.bukkit.entity.Ageable mother && mother.getAge() > 0) {
                        mother.setAge(3600);
                    }
                    if (event.getFather() instanceof org.bukkit.entity.Ageable father && father.getAge() > 0) {
                        father.setAge(3600);
                    }
                },
                1L
        );

        spawnParticleFeedback(event.getEntity().getLocation().add(0, 1, 0), Particle.HEART);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (event.getCaught() == null || !(event.getCaught() instanceof Item caughtItem)) return;

        EcoRegionData data = chunkManager.getRegionData(event.getPlayer().getLocation().getChunk());
        if (data == null) return;

        int aqua = data.getAqua().get();

        if (aqua >= 60) {
            if (ThreadLocalRandom.current().nextDouble() <= 0.15) {
                Material[] treasures = {
                        Material.NAME_TAG,
                        Material.NAUTILUS_SHELL,
                        Material.SADDLE
                };
                Material selectedTreasure = treasures[ThreadLocalRandom.current().nextInt(treasures.length)];

                caughtItem.setItemStack(new ItemStack(selectedTreasure));
                spawnParticleFeedback(caughtItem.getLocation(), Particle.HAPPY_VILLAGER);
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                event.getPlayer().sendMessage("§b[生態回饋] 純淨的水源為你帶來了意想不到的寶物！");
            }
        }
        else if (aqua <= -30) {
            if (ThreadLocalRandom.current().nextDouble() <= 0.25) {
                Material[] trash = {
                        Material.ROTTEN_FLESH,
                        Material.BONE,
                        Material.KELP
                };
                caughtItem.setItemStack(new ItemStack(trash[ThreadLocalRandom.current().nextInt(trash.length)]));
                spawnParticleFeedback(caughtItem.getLocation(), Particle.SQUID_INK);
                event.getPlayer().sendMessage("§8[生態惡化] 水質過於混濁，你只釣上了一些垃圾...");
            }
        }
    }

    private void spawnParticleFeedback(org.bukkit.Location loc, Particle particle) {
        loc.getWorld().spawnParticle(particle, loc, 5, 0.3, 0.3, 0.3, 0);
    }
}