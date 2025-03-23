package com.github.mertunctuncer.playervaults.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigReader {

    private final FileConfiguration config;
    private final Plugin plugin;


    public ConfigReader(Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public record ConnectionData(
            String jdbcUrl,
            String username,
            String password,
            int maxConnections,
            long timeoutMillis
    ) { }

    public ConnectionData getConnectionData() {
        File databaseFile = new File(this.plugin.getDataFolder(), this.config.getString("sqlite-database-path"));
        String dbUsername = this.config.getString("database-username");
        String dbPassword = this.config.getString("database-password");
        int maxConnections = this.config.getInt("database-max-connections");
        long connectionTimeout = this.config.getLong("database-connection-timeout");

        return new ConnectionData(
                "jdbc:sqlite:" + databaseFile,
                dbUsername,
                dbPassword,
                maxConnections,
                connectionTimeout
        );
    }
}
