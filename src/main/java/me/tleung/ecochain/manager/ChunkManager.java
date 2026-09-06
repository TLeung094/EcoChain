package me.tleung.ecochain.manager;

import me.tleung.ecochain.EcoChain;
import me.tleung.ecochain.data.EcoRegionData;
import me.tleung.ecochain.storage.DatabaseStorage;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkManager implements Listener {
    private final EcoChain plugin;
    private final DatabaseStorage dbStorage;
    private final ConcurrentHashMap<String, EcoRegionData> activeRegions = new ConcurrentHashMap<>();

    public ChunkManager(EcoChain plugin, DatabaseStorage dbStorage) {
        this.plugin = plugin;
        this.dbStorage = dbStorage;
    }

    public EcoRegionData getRegionData(Chunk chunk) {
        return activeRegions.get(getChunkKey(chunk));
    }

    public Map<String, EcoRegionData> getAllActiveRegions() {
        return activeRegions;
    }

    public static String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + "_" + chunk.getX() + "_" + chunk.getZ();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        String key = getChunkKey(chunk);
        World world = chunk.getWorld();
        int bX = chunk.getX() << 4;
        int bZ = chunk.getZ() << 4;

        if (activeRegions.containsKey(key)) return;

        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            EcoRegionData loaded = dbStorage.loadDataSync(key);
            plugin.getServer().getRegionScheduler().execute(plugin, world, bX, bZ, () -> {
                activeRegions.putIfAbsent(key, loaded);
            });
        });
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        String key = getChunkKey(event.getChunk());
        EcoRegionData data = activeRegions.remove(key);
        if (data != null) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, task -> dbStorage.saveDataSync(data));
        }
    }

    public void saveAllOnDisable() {
        for (EcoRegionData data : activeRegions.values()) {
            dbStorage.saveDataSync(data);
        }
        activeRegions.clear();
    }
}