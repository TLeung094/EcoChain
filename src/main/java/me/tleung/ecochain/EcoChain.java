package me.tleung.ecochain;

import me.tleung.ecochain.command.EcoAdminCommand;
import me.tleung.ecochain.command.EcoCommand;
import me.tleung.ecochain.command.EcoQuestCommand;
import me.tleung.ecochain.command.EcoShopCommand;
import me.tleung.ecochain.hook.EcoPlaceholder;
import me.tleung.ecochain.listener.EcoEventListener;
import me.tleung.ecochain.listener.EcoFeatureListener;
import me.tleung.ecochain.manager.ChunkManager;
import me.tleung.ecochain.manager.EcoCompassManager;
import me.tleung.ecochain.manager.EcoShopManager;
import me.tleung.ecochain.manager.PlayerQuestManager;
import me.tleung.ecochain.storage.DatabaseStorage;
import me.tleung.ecochain.task.EcoDecayTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class EcoChain extends JavaPlugin {

    private DatabaseStorage databaseStorage;
    private ChunkManager chunkManager;
    private PlayerQuestManager questManager;
    private EcoCompassManager compassManager;
    private EcoShopManager shopManager;
    private EcoDecayTask decayTask;

    @Override
    public void onEnable() {
        getLogger().info("EcoChain 正在啟動...");

        // 1. 產生並載入預設的 config.yml
        saveDefaultConfig();

        // 2. 初始化資料庫與管理器
        this.databaseStorage = new DatabaseStorage(this);
        this.chunkManager = new ChunkManager(this, databaseStorage);
        this.questManager = new PlayerQuestManager(this);
        this.compassManager = new EcoCompassManager(this, chunkManager);
        this.shopManager = new EcoShopManager(this);

        // 3. 註冊事件監聽器
        getServer().getPluginManager().registerEvents(chunkManager, this);
        getServer().getPluginManager().registerEvents(questManager, this);
        getServer().getPluginManager().registerEvents(compassManager, this);
        getServer().getPluginManager().registerEvents(shopManager, this);
        getServer().getPluginManager().registerEvents(new EcoEventListener(this, chunkManager, questManager), this);
        getServer().getPluginManager().registerEvents(new EcoFeatureListener(this, chunkManager), this);

        // 4. 註冊指令
        Objects.requireNonNull(getCommand("ecochain")).setExecutor(new EcoCommand(chunkManager));
        Objects.requireNonNull(getCommand("ecoquest")).setExecutor(new EcoQuestCommand(questManager));
        Objects.requireNonNull(getCommand("ecoshop")).setExecutor(new EcoShopCommand(shopManager));

        // 傳入 compassManager 以支援 /ecoadmin compass 指令
        EcoAdminCommand adminCommand = new EcoAdminCommand(this, chunkManager, compassManager);
        Objects.requireNonNull(getCommand("ecoadmin")).setExecutor(adminCommand);
        Objects.requireNonNull(getCommand("ecoadmin")).setTabCompleter(adminCommand);

        // 5. 啟動排程任務
        this.decayTask = new EcoDecayTask(this, chunkManager);
        decayTask.start();

        // 6. 註冊 PlaceholderAPI 支援
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EcoPlaceholder(this, chunkManager).register();
            getLogger().info("已成功掛載 PlaceholderAPI 變數支援！");
        }

        getLogger().info("EcoChain 啟動成功！已支援 Folia 架構。");
    }

    @Override
    public void onDisable() {
        getLogger().info("EcoChain 正在關閉...");

        if (decayTask != null) decayTask.stop();
        if (chunkManager != null) chunkManager.saveAllOnDisable();
        if (questManager != null) questManager.saveAllOnDisable();
        if (databaseStorage != null) databaseStorage.close();

        getLogger().info("EcoChain 安全關閉完成。");
    }
    public ChunkManager getChunkManager() {
        return this.chunkManager;
    }
}