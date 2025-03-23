package com.github.mertunctuncer.playervaults.repository;

import com.github.mertunctuncer.playervaults.model.PlayerVault;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public interface VaultRepository extends AutoCloseable {

    CompletableFuture<Void> createTableAsync();
    CompletableFuture<PlayerVault> fetchVault(Player owner, int vaultId);
    CompletableFuture<Void> upsertVault(PlayerVault vault, int vaultId);
}
