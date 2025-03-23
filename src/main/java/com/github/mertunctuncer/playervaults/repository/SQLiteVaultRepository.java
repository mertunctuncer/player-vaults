package com.github.mertunctuncer.playervaults.repository;

import com.github.mertunctuncer.playervaults.model.PlayerVault;
import com.github.mertunctuncer.playervaults.util.VaultSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


public class SQLiteVaultRepository implements VaultRepository {
    private final Server server;
    private final ConnectionProvider connectionProvider;
    private final ExecutorService executorService;

    public SQLiteVaultRepository(
            Server server,
            ConnectionProvider connectionProvider,
            ExecutorService executorService
    ) {
        this.server = server;
        this.connectionProvider = connectionProvider;
        this.executorService = executorService;
        createTableAsync().thenRun(() -> {
            this.server.getLogger().info("VaultRepository initialized.");
        });
    }

    @Override
    public CompletableFuture<Void> createTableAsync() {
        return CompletableFuture.runAsync(() -> {
            try (
                    Connection connection = connectionProvider.getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "CREATE TABLE IF NOT EXISTS vaults (" +
                                    "player_uuid TEXT PRIMARY KEY," +
                                    "vault_id INTEGER NOT NULL," +
                                    "vault_data TEXT NOT NULL" +
                                    ");");
            ) {
                statement.execute();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create vaults table", e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<PlayerVault> fetchVault(Player owner, int vaultId) {
        String playerName = owner.getName();
        UUID uuid = owner.getUniqueId();
        String title = String.format("%s's Vault", playerName);

        return CompletableFuture.supplyAsync(() -> {
            ResultSet resultSet = null;

            try (
                    Connection connection = connectionProvider.getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "SELECT * FROM vaults WHERE " +
                                    "player_uuid=?," +
                                    "vault_id=?" +
                                    ";"
                    );
            ) {
                statement.setString(1, uuid.toString());
                statement.setInt(2, vaultId);
                resultSet = statement.executeQuery();

                resultSet.next();
                String vaultData = resultSet.getString("vault_data");

                ItemStack[] items = VaultSerializer.deserialize(vaultData);

                return new PlayerVault(server, uuid, title, items);
            } catch (SQLException e) {
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
                    Connection connection = connectionProvider.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO vaults(player_uuid,vault_id,vault_data) " +
                            "VALUES(?,?,?) " +
                            "ON CONFLICT(player_uuid) DO UPDATE SET " +
                            "player_uuid=?," +
                            "vauld_id=?," +
                            "vault_data=?" +
                            ";")
                    ) {
                preparedStatement.setString(1, vault.getOwner().toString());
                preparedStatement.setInt(2, vaultId);
                preparedStatement.setString(3, vaultData);
                preparedStatement.setString(4, vault.getOwner().toString());
                preparedStatement.setInt(5, vaultId);
                preparedStatement.setString(6, vaultData);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void close() throws Exception {
        connectionProvider.close();
    }
}
