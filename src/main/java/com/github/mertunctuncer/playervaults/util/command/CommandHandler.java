package com.github.mertunctuncer.playervaults.util.command;

import com.github.mertunctuncer.playervaults.util.command.commands.VaultCommand;
import com.github.mertunctuncer.playervaults.repository.VaultRepository;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandHandler {

    private final JavaPlugin plugin;
    private final VaultRepository vaultRepository;

    public CommandHandler(JavaPlugin plugin, VaultRepository vaultRepository) {
        this.plugin = plugin;
        this.vaultRepository = vaultRepository;
    }

    public void registerVaultCommand() {
        this.plugin.getCommand("vault").setExecutor(new VaultCommand(vaultRepository));
    }
}
