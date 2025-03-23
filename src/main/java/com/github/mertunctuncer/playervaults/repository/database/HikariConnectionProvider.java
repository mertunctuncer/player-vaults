package com.github.mertunctuncer.playervaults.repository.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariConnectionProvider implements SQLConnectionProvider, AutoCloseable {
    private final HikariDataSource hikariDataSource;

    public HikariConnectionProvider(
            String url,
            String username,
            String password,
            int maxPoolSize,
            long connectionTimeout
    ) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        config.setConnectionTimeout(connectionTimeout);

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
