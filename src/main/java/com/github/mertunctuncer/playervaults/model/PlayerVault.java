package com.github.mertunctuncer.playervaults.model;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerVault {
    private final UUID uuid;
    private final Server server;
    private final Inventory inventory;
    private final UUID ownerUuid;

    public PlayerVault(Server server, UUID uuid, UUID ownerUuid, String title, ItemStack[] items) {
        this(server, uuid, ownerUuid, title);
        this.inventory.setContents(items);
    }

    public PlayerVault(Server server, UUID uuid, UUID ownerUuid, String title) {
        this.uuid = uuid;
        this.server = server;
        // paper deprecation, keep for bukkit support
        this.inventory = Bukkit.createInventory(null, 9 * 6, title);
        this.ownerUuid = ownerUuid;
    }

    public ItemStack[] getContents() {
        return inventory.getContents();
    }

    public void open() {
        Player player = server.getPlayer(ownerUuid);
        if(player == null) {
            throw new RuntimeException("Attempted to open player vault on a player that is not online.");
        }
        player.openInventory(inventory);
    }

    public void close() {
        inventory.close();
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
