package com.github.mertunctuncer.playervaults.command;

import com.github.mertunctuncer.playervaults.repository.VaultRepository;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VaultCommand implements CommandExecutor {

    private final VaultRepository vaultRepository;

    public VaultCommand(VaultRepository vaultRepository) {
        this.vaultRepository = vaultRepository;
    }

    @Override
    public boolean onCommand (
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String @NotNull [] args
    ) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command!");
            return true;
        }
        if (args.length == 1) {
            Player player = (Player) sender;
            try {
                int id = Integer.parseInt(args[0]);
                this.vaultRepository.openVault(player, id);
            } catch (Exception e) {
                sender.sendMessage("Invalid vault number!");
                return true;
            }
        } else {
            // deprecation ignored for bukkit support
            sender.sendMessage(ChatColor.RED + "Usage: /vault <id>");
        }
        return true;
    }
}
