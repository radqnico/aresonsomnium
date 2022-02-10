package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.commands.CommandParserCommand;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReloadItemsCommand extends CommandParserCommand {

    private final ShopItemsManager shopItemsManager;

    public ReloadItemsCommand(AresonSomnium aresonSomnium) {
        this.shopItemsManager = aresonSomnium.getShopItemsManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.isOp()) {
            shopItemsManager.reloadItems();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
