package com.github.mertunctuncer.playervaults;

import com.github.mertunctuncer.playervaults.command.VaultCommand;
import com.github.mertunctuncer.playervaults.repository.VaultRepository;
import com.github.mertunctuncer.playervaults.repository.database.SQLConnectionProvider;
import com.github.mertunctuncer.playervaults.repository.database.HikariConnectionProvider;
import com.github.mertunctuncer.playervaults.repository.database.sqlite.SQLiteVaultDAO;
import com.github.mertunctuncer.playervaults.repository.database.VaultDAO;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.Executors;

public class PlayerVaults extends JavaPlugin {

    private VaultDAO dao;
    private VaultRepository vaultRepository;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        SQLConnectionProvider SQLConnectionProvider = buildConnectionProvider();
        this.dao = new SQLiteVaultDAO(
                this.getServer(),
                this.getLogger(),
                SQLConnectionProvider,
                Executors.newVirtualThreadPerTaskExecutor()
        );

        this.dao.createTableAsync();
        this.vaultRepository = new VaultRepository(this.getServer(), this, this.dao);
        registerCommands();
    }

    @Override
    public void onDisable() {
        try {
            vaultRepository.close();
            dao.close();
        } catch (Exception e) {
            this.getLogger().severe(e.getMessage());
        }
    }

    private void registerCommands() {
        this.getCommand("vault").setExecutor(new VaultCommand(vaultRepository));
    }

    private SQLConnectionProvider buildConnectionProvider() {
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
