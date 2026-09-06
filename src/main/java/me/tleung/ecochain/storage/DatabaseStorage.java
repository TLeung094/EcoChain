package me.tleung.ecochain.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.tleung.ecochain.EcoChain;
import me.tleung.ecochain.data.EcoRegionData;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseStorage {
    private final HikariDataSource dataSource;
    private final EcoChain plugin;

    public DatabaseStorage(EcoChain plugin) {
        this.plugin = plugin;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File dbFile = new File(plugin.getDataFolder(), "ecochain.db");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1);

        this.dataSource = new HikariDataSource(config);
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS eco_regions (" +
                "chunk_key VARCHAR(64) PRIMARY KEY, " +
                "flora INTEGER NOT NULL, fauna INTEGER NOT NULL, aqua INTEGER NOT NULL)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("無法建立 eco_regions 資料表: " + e.getMessage());
        }
    }

    public EcoRegionData loadDataSync(String chunkKey) {
        String sql = "SELECT flora, fauna, aqua FROM eco_regions WHERE chunk_key = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chunkKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new EcoRegionData(chunkKey, rs.getInt("flora"), rs.getInt("fauna"), rs.getInt("aqua"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("DB 讀取錯誤: " + e.getMessage());
        }
        return new EcoRegionData(chunkKey, 0, 0, 0);
    }

    public void saveDataSync(EcoRegionData data) {
        String sql = "INSERT INTO eco_regions (chunk_key, flora, fauna, aqua) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(chunk_key) DO UPDATE SET flora=?, fauna=?, aqua=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, data.getChunkKey());
            stmt.setInt(2, data.getFlora().get());
            stmt.setInt(3, data.getFauna().get());
            stmt.setInt(4, data.getAqua().get());

            stmt.setInt(5, data.getFlora().get());
            stmt.setInt(6, data.getFauna().get());
            stmt.setInt(7, data.getAqua().get());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("DB 寫入錯誤: " + e.getMessage());
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }
}