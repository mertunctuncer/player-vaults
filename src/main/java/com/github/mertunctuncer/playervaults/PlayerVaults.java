package com.github.mertunctuncer.playervaults;

import com.github.mertunctuncer.playervaults.util.ConfigReader;
import com.github.mertunctuncer.playervaults.util.command.CommandHandler;
import com.github.mertunctuncer.playervaults.repository.VaultRepository;
import com.github.mertunctuncer.playervaults.repository.database.SQLConnectionProvider;
import com.github.mertunctuncer.playervaults.repository.database.HikariConnectionProvider;
import com.github.mertunctuncer.playervaults.repository.database.sqlite.SQLiteVaultDAO;
import com.github.mertunctuncer.playervaults.repository.database.VaultDAO;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;

public class PlayerVaults extends JavaPlugin {

    private VaultDAO dao;
    private VaultRepository vaultRepository;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        ConfigReader configReader = new ConfigReader(this, this.getConfig());

        SQLConnectionProvider SQLConnectionProvider = new HikariConnectionProvider(configReader.getConnectionData());

        this.dao = new SQLiteVaultDAO(
                this.getServer(),
                this.getLogger(),
                SQLConnectionProvider,
                Executors.newVirtualThreadPerTaskExecutor()
        );

        this.dao.createTableAsync();
        this.vaultRepository = new VaultRepository(this.getServer(), this, this.getLogger(), this.dao);
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
        CommandHandler commandHandler = new CommandHandler(this, vaultRepository);
        commandHandler.registerVaultCommand();
    }
}
