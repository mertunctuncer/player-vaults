package com.github.mertunctuncer.playervaults;

import com.github.mertunctuncer.playervaults.repository.ConnectionProvider;
import com.github.mertunctuncer.playervaults.repository.HikariConnectionProvider;
import com.github.mertunctuncer.playervaults.repository.SQLiteVaultRepository;
import com.github.mertunctuncer.playervaults.repository.VaultRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.Executors;

public class PlayerVaults extends JavaPlugin {

    private VaultRepository repository;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        ConnectionProvider connectionProvider = getConnectionProvider();
        repository = new SQLiteVaultRepository(
                this.getServer(),
                this.getLogger(),
                connectionProvider,
                Executors.newVirtualThreadPerTaskExecutor()
        );
    }

    @Override
    public void onDisable() {

    }

    private ConnectionProvider getConnectionProvider() {
        File databaseFile = new File(this.getDataFolder(), this.getConfig().getString("sqlite-database-path"));
        String dbUsername = this.getConfig().getString("database-username");
        String dbPassword = this.getConfig().getString("database-password");
        int maxConnections = this.getConfig().getInt("database-max-connections");
        long connectionTimeout = this.getConfig().getLong("database-connection-timeout");

        return new HikariConnectionProvider(
                "jdbc:sqlite:" + databaseFile,
                dbUsername,
                dbPassword,
                maxConnections,
                connectionTimeout
                );
    }
}
