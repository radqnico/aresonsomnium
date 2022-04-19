package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonlib.minecraft.commands.shapes.CompleteCommand;
import it.areson.aresonlib.minecraft.files.MessageManager;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class EditItemsCommand implements CompleteCommand {

    private final ShopItemsManager shopItemsManager;
    private final MessageManager messageManager;

    public EditItemsCommand(AresonSomnium aresonSomnium) {
        this.shopItemsManager = aresonSomnium.getShopItemsManager();
        this.messageManager = aresonSomnium.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        // shopadmin editshopitems <page>
        if (commandSender instanceof Player player) {
            if (arguments.length == 0) {
                shopItemsManager.openEditGuiToPlayer(player, 0);
            } else if (arguments.length == 1) {
                try {
                    int page = Integer.parseInt(arguments[0]) - 1;
                    shopItemsManager.openEditGuiToPlayer(player, page);
                } catch (Exception exception) {
                    messageManager.sendFreeMessage(commandSender, "La pagina non è un numero");
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] arguments) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add(shopItemsManager.getItemListView().getNumberOfPages() + "");
        return suggestions;
    }
}
