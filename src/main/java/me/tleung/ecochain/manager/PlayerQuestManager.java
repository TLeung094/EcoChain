package me.tleung.ecochain.manager;

import me.tleung.ecochain.EcoChain;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerQuestManager implements Listener {
    private final EcoChain plugin;
    private final File playerDataFolder;

    private final Map<UUID, YamlConfiguration> playerCache = new ConcurrentHashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public PlayerQuestManager(EcoChain plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "players");
        if (!this.playerDataFolder.exists()) {
            this.playerDataFolder.mkdirs();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            File file = new File(playerDataFolder, uuid + ".yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            String today = dateFormat.format(new Date());
            String lastLogin = config.getString("last-login-date", "");

            if (!today.equals(lastLogin)) {
                config.set("last-login-date", today);
                generateDailyQuest(config);
            }

            playerCache.put(uuid, config);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        YamlConfiguration config = playerCache.remove(uuid);

        if (config != null) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
                saveConfig(uuid, config);
            });
        }
    }

    private void generateDailyQuest(YamlConfiguration config) {
        config.set("daily-quest.type", "PLANT_TREE");
        config.set("daily-quest.target-amount", 10);
        config.set("daily-quest.current-progress", 0);
        config.set("daily-quest.completed", false);
    }

    public YamlConfiguration getPlayerData(UUID uuid) {
        return playerCache.get(uuid);
    }

    public void saveConfig(UUID uuid, YamlConfiguration config) {
        try {
            File file = new File(playerDataFolder, uuid + ".yml");
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("無法儲存玩家任務資料: " + uuid);
        }
    }

    public void saveAllOnDisable() {
        plugin.getLogger().info("正在儲存所有玩家的任務進度...");
        for (Map.Entry<UUID, YamlConfiguration> entry : playerCache.entrySet()) {
            saveConfig(entry.getKey(), entry.getValue());
        }
        playerCache.clear();
    }
}