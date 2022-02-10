package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonlib.files.MessageManager;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.commands.CommandParserCommand;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EditItemsCommand extends CommandParserCommand {

    private final ShopItemsManager shopItemsManager;
    private final MessageManager messageManager;

    public EditItemsCommand(AresonSomnium aresonSomnium) {
        this.shopItemsManager = aresonSomnium.getShopItemsManager();
        this.messageManager = aresonSomnium.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // /shopadmin editshopitems <page>
        if (commandSender instanceof Player player) {
            if (strings.length == 1) {
                shopItemsManager.openEditGuiToPlayer(player, 0);
            } else if (strings.length == 2) {
                try {
                    int page = Integer.parseInt(strings[1]) - 1;
                    shopItemsManager.openEditGuiToPlayer(player, page);
                } catch (Exception exception) {
                    messageManager.sendFreeMessage(commandSender, "La pagina non è un numero");
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 2) {
            suggestions.add(shopItemsManager.getItemListView().getNumberOfPages() + "");
        }
        return suggestions;
    }
}
