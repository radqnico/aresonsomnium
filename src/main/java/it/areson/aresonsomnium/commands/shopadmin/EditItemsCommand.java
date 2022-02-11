package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonlib.commands.shapes.SubCommand;
import it.areson.aresonlib.files.MessageManager;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EditItemsCommand implements SubCommand {

    private final ShopItemsManager shopItemsManager;
    private final MessageManager messageManager;

    public EditItemsCommand(AresonSomnium aresonSomnium) {
        this.shopItemsManager = aresonSomnium.getShopItemsManager();
        this.messageManager = aresonSomnium.getMessageManager();
    }

    @Override
    public void onCommand(@NotNull CommandSender commandSender, String[] arguments) {
        // /shopadmin editshopitems <page>
        if (commandSender instanceof Player player) {
            if (arguments.length == 1) {
                shopItemsManager.openEditGuiToPlayer(player, 0);
            } else if (arguments.length == 2) {
                try {
                    int page = Integer.parseInt(arguments[1]) - 1;
                    shopItemsManager.openEditGuiToPlayer(player, page);
                } catch (Exception exception) {
                    messageManager.sendFreeMessage(commandSender, "La pagina non è un numero");
                }
            }
        }
    }

    //TODO
//    @Override
//    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender,Command command,String s,String[] strings) {
//        List<String> suggestions = new ArrayList<>();
//        if (strings.length == 2) {
//            suggestions.add(shopItemsManager.getItemListView().getNumberOfPages() + "");
//        }
//        return suggestions;
//    }
}
