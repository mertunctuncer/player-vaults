package com.github.mertunctuncer.playervaults.model;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerVault {
    private final Server server;
    private final Inventory inventory;

    private final UUID owner;

    public PlayerVault(Server server, UUID owner, String title, ItemStack[] contents) {
        this(server, owner, title);
        this.inventory.setContents(contents);
    }

    public PlayerVault(Server server, UUID owner, String title) {
        this.server = server;
        // paper deprecation, keep for bukkit support
        this.inventory = Bukkit.createInventory(null, 9 * 9, title);
        this.owner = owner;
    }

    public ItemStack[] getContents() {
        return inventory.getContents();
    }

    public void open() {
        Player player = server.getPlayer(owner);
        if(player == null) {
            throw new RuntimeException("Attempted to open player vault on a player that is not online.");
        }
        player.openInventory(inventory);
    }

    public void close() {
        inventory.close();
    }

    public Server getServer() {
        return server;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public UUID getOwner() {
        return owner;
    }
}
