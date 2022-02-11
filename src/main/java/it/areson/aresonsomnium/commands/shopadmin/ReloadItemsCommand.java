package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonlib.commands.shapes.SubCommand;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import org.bukkit.command.CommandSender;

public class ReloadItemsCommand implements SubCommand {

    private final ShopItemsManager shopItemsManager;

    public ReloadItemsCommand(AresonSomnium aresonSomnium) {
        this.shopItemsManager = aresonSomnium.getShopItemsManager();
    }

    @Override
    public void onCommand(CommandSender commandSender, String[] arguments) {
        if (commandSender.isOp()) {
            shopItemsManager.reloadItems();
        }
    }

}
