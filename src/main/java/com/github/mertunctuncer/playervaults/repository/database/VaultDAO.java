package com.github.mertunctuncer.playervaults.repository.database;

import com.github.mertunctuncer.playervaults.model.PlayerVault;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface VaultDAO extends AutoCloseable {

    CompletableFuture<Void> createTableAsync();
    CompletableFuture<PlayerVault> fetchVault(Player owner, int vaultId);
    CompletableFuture<Void> upsertVault(PlayerVault vault, int vaultId);
}
