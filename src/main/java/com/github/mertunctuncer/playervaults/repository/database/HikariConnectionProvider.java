package com.github.mertunctuncer.playervaults.repository.database;

import com.github.mertunctuncer.playervaults.util.ConfigReader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariConnectionProvider implements SQLConnectionProvider, AutoCloseable {
    private final HikariDataSource hikariDataSource;

    public HikariConnectionProvider(ConfigReader.ConnectionData connectionData) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(connectionData.jdbcUrl());
        config.setUsername(connectionData.username());
        config.setPassword(connectionData.password());
        config.setMaximumPoolSize(connectionData.maxConnections());
        config.setConnectionTimeout(connectionData.timeoutMillis());

        hikariDataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    @Override
    public void close() {
        hikariDataSource.close();
    }
}
