package com.github.mertunctuncer.playervaults.repository.database.sqlite;

import com.github.mertunctuncer.playervaults.model.PlayerVault;
import com.github.mertunctuncer.playervaults.repository.database.SQLConnectionProvider;
import com.github.mertunctuncer.playervaults.repository.database.VaultDAO;
import com.github.mertunctuncer.playervaults.util.VaultSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class SQLiteVaultDAO implements VaultDAO {
    private final Server server;
    private final Logger logger;
    private final SQLConnectionProvider SQLConnectionProvider;
    private final ExecutorService executorService;

    public SQLiteVaultDAO(
            Server server,
            Logger logger,
            SQLConnectionProvider SQLConnectionProvider,
            ExecutorService executorService
    ) {
        this.server = server;
        this.logger = logger;
        this.SQLConnectionProvider = SQLConnectionProvider;
        this.executorService = executorService;
    }

    @Override
    public CompletableFuture<Void> createTableAsync() {
        return CompletableFuture.runAsync(() -> {
            try (
                    Connection connection = SQLConnectionProvider.getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "CREATE TABLE IF NOT EXISTS vaults (" +
                                    "vault_uuid TEXT PRIMARY KEY," +
                                    "player_uuid TEXT NOT NULL," +
                                    "vault_id INTEGER NOT NULL," +
                                    "vault_data TEXT NOT NULL" +
                                    ");");
            ) {
                statement.execute();
                this.logger.info("Creating vaults table if not exists.");
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create vaults table", e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<PlayerVault> fetchVault(Player owner, int vaultId) {
        this.logger.info("Fetching vault for " + owner.getName() + " with id " + vaultId + ".");
        String playerName = owner.getName();
        UUID ownerUuid = owner.getUniqueId();
        String title = String.format("%s's Vault #%d", playerName, vaultId);

        return CompletableFuture.supplyAsync(() -> {
            ResultSet resultSet = null;
            try (
                    Connection connection = SQLConnectionProvider.getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "SELECT * FROM 'vaults' WHERE " +
                                    "player_uuid=? AND " +
                                    "vault_id=?" +
                                    ";"
                    );
            ) {

                statement.setString(1, ownerUuid.toString());
                statement.setInt(2, vaultId);
                resultSet = statement.executeQuery();

                if(resultSet.next()) {
                    UUID vaultUuid = UUID.fromString(resultSet.getString("vault_uuid"));
                    String vaultData = resultSet.getString("vault_data");

                    ItemStack[] items = VaultSerializer.deserialize(vaultData);

                    logger.info(String.format("Found vault for %s with id %d: %s", owner.getName(), vaultId, vaultUuid));
                    return new PlayerVault(server, vaultUuid, ownerUuid, title, items);
                }

                return null;
            } catch (SQLException e) {
                logger.warning(e.getMessage());
                return null;
            } finally {
                try {
                    resultSet.close();
                } catch (Exception ignored) {}
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> upsertVault(PlayerVault vault, int vaultId){
        String vaultData = VaultSerializer.serialize(vault);
        return CompletableFuture.runAsync(() -> {
            try (
                    Connection connection = SQLConnectionProvider.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(
                            "INSERT INTO vaults(vault_uuid,player_uuid,vault_id,vault_data) " +
                                    "VALUES(?,?,?,?) " +
                                    "ON CONFLICT(vault_uuid) DO UPDATE SET " +
                                    "vault_data=?" +
                                    ";"
                    )
            ) {
                preparedStatement.setString(1, vault.getUuid().toString());
                preparedStatement.setString(2, vault.getOwnerUuid().toString());
                preparedStatement.setInt(3, vaultId);
                preparedStatement.setString(4, vaultData);
                preparedStatement.setString(5, vaultData);

                preparedStatement.execute();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }, executorService);
    }

    @Override
    public void close() {
        try {
            SQLConnectionProvider.close();
        } catch (Exception ignored) {}
    }
}
