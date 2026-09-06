package me.tleung.ecochain.command;

import me.tleung.ecochain.manager.EcoShopManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EcoShopCommand implements CommandExecutor {
    private final EcoShopManager shopManager;

    public EcoShopCommand(EcoShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用此指令！");
            return true;
        }

        shopManager.openShop(player);
        return true;
    }
}