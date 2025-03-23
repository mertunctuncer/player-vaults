package com.github.mertunctuncer.playervaults.repository;

import com.github.mertunctuncer.playervaults.model.PlayerVault;
import com.github.mertunctuncer.playervaults.repository.database.VaultDAO;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VaultRepository {

    private final Server server;
    private final Plugin plugin;
    private final VaultDAO dao;
    private final Map<UUID, Map<Integer, PlayerVault>> vaults = new ConcurrentHashMap<>();

    public VaultRepository(Server server, Plugin plugin, VaultDAO dao) {
        this.server = server;
        this.plugin = plugin;
        this.dao = dao;
    }

    // Lazy fetch
    public void openVault(Player owner, int vaultId) {

        System.out.println("Opening vault " + vaultId);

        if(vaults.containsKey(owner.getUniqueId())) {
            System.out.println("Vault map " + vaultId + " exists");
            Map<Integer, PlayerVault> playerVaults = vaults.get(owner.getUniqueId());
            if(playerVaults.containsKey(vaultId)) {
                System.out.println("Vault " + vaultId + " exists");
                playerVaults.get(vaultId).open();
                return;
            }
        } else {
            System.out.println("Creating map " + vaultId + " exists");
            vaults.put(owner.getUniqueId(), new HashMap<>());
        }

        this.dao.fetchVault(owner, vaultId).thenAccept(it -> {
            final PlayerVault immutableVault = it;
            Bukkit.getScheduler().runTask(plugin, () -> {
                PlayerVault vault = immutableVault;
                if (vault == null) {
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

        System.out.println("Quiting vault " + player.getUniqueId());
        Map<Integer, PlayerVault> playerVaults = vaults.get(player.getUniqueId());
        CompletableFuture<?>[] futures = playerVaults.entrySet().stream()
                .map(entry -> dao.upsertVault(entry.getValue(), entry.getKey()))
                .collect(Collectors.toUnmodifiableSet()).toArray(new CompletableFuture[0]);

        CompletableFuture.allOf(futures).thenRun(() -> {
            vaults.remove(player.getUniqueId());
        });
    }
}
