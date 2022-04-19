package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonlib.minecraft.commands.shapes.CompleteCommand;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

@SuppressWarnings("NullableProblems")
public class ReloadItemsCommand implements CompleteCommand {

    private final ShopItemsManager shopItemsManager;

    public ReloadItemsCommand(AresonSomnium aresonSomnium) {
        this.shopItemsManager = aresonSomnium.getShopItemsManager();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        if (commandSender.isOp()) {
            shopItemsManager.reloadItems();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] arguments) {
        return null;
    }

}
