package com.github.mertunctuncer.playervaults.repository.connection;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLConnectionProvider extends AutoCloseable {
    Connection getConnection() throws SQLException;
}
