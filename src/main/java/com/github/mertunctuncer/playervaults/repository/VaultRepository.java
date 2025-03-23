package com.github.mertunctuncer.playervaults.repository;

import com.github.mertunctuncer.playervaults.model.PlayerVault;
import com.github.mertunctuncer.playervaults.repository.database.VaultDAO;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VaultRepository implements AutoCloseable {

    private final Server server;
    private final Plugin plugin;
    private final Logger logger;
    private final VaultDAO dao;
    private final Map<UUID, Map<Integer, PlayerVault>> vaults = new ConcurrentHashMap<>();

    public VaultRepository(Server server, Plugin plugin, Logger logger,VaultDAO dao) {
        this.server = server;
        this.plugin = plugin;
        this.logger = logger;
        this.dao = dao;

        this.server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                handleQuit(event.getPlayer());
            }
        }, plugin);
    }

    // Lazy fetch
    public void openVault(Player owner, int vaultId) {
        this.logger.info("Opening vault for " + owner.getName() + " with id " + vaultId + ".");
        if(vaults.containsKey(owner.getUniqueId())) {
            Map<Integer, PlayerVault> playerVaults = vaults.get(owner.getUniqueId());
            if(playerVaults.containsKey(vaultId)) {
                this.logger.info("Vault for " + owner.getName() + " with id " + vaultId + " already cached.");
                playerVaults.get(vaultId).open();
                return;
            }
        } else {
            vaults.put(owner.getUniqueId(), new HashMap<>());
        }

        this.dao.fetchVault(owner, vaultId).thenAccept(it -> {
            final PlayerVault immutableVault = it;
            Bukkit.getScheduler().runTask(plugin, () -> {
                PlayerVault vault = immutableVault;
                if (vault == null) {
                    this.logger.info("Vault not found. Creating vault for " + owner.getName() + " with id " + vaultId + ".");
                    vault = new PlayerVault(
                            server,
                            UUID.randomUUID(),
                            owner.getUniqueId(),
                            String.format("%s's Vault #%d", owner.getName(), vaultId)
                    );
                }

                vaults.get(owner.getUniqueId()).put(vaultId, vault);
                PlayerVault finalVault = vault;
                finalVault.open();
            });
        });
    }

    public void handleQuit(Player player) {
        if(!vaults.containsKey(player.getUniqueId())) return;

        this.logger.info("Saving vaults for " + player.getName() + ".");

        Map<Integer, PlayerVault> playerVaults = vaults.get(player.getUniqueId());
        CompletableFuture<?>[] futures = playerVaults.entrySet().stream()
                .map(entry -> dao.upsertVault(entry.getValue(), entry.getKey()))
                .collect(Collectors.toUnmodifiableSet()).toArray(new CompletableFuture[0]);

        CompletableFuture.allOf(futures).thenRun(() -> {
            vaults.remove(player.getUniqueId());
        });
    }

    @Override
    public void close() {
        PlayerQuitEvent.getHandlerList().unregister(plugin);
        vaults.forEach((uuid, playerVaults) -> {
            CompletableFuture<?>[] futures = playerVaults.entrySet().stream()
                    .map(entry -> dao.upsertVault(entry.getValue(), entry.getKey()))
                    .collect(Collectors.toUnmodifiableSet()).toArray(new CompletableFuture[0]);

            CompletableFuture.allOf(futures).thenRun(() -> {
                vaults.remove(uuid);
            });
        });
    }
}
