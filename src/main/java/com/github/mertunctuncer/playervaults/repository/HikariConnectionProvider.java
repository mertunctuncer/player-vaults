package com.github.mertunctuncer.playervaults.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class HikariConnectionProvider implements ConnectionProvider, AutoCloseable {
    private final HikariConfig config = new HikariConfig();
    private final HikariDataSource hikariDataSource;


    public HikariConnectionProvider(
            String url,
            String username,
            String password,
            int maxPoolSize,
            long connectionTimeout
    ) {
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
