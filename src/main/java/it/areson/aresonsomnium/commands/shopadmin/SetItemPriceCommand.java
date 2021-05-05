package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.AresonCommand;
import it.areson.aresonsomnium.commands.CommandParserCommand;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AresonCommand("setitemprice")
public class SetItemPriceCommand extends CommandParserCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // / /shopadmin setitemprice buy <id> <valuta> <qta>
        ShopItemsManager shopItemsManager = AresonSomniumAPI.instance.shopItemsManager;
        try {
            int id = Integer.parseInt(strings[2]);
            try {
                CoinType coinType = CoinType.valueOf(strings[3].toUpperCase());
                BigDecimal amount = new BigDecimal(strings[4]);
                Optional<ShopItem> itemById = shopItemsManager.getItemsGateway().getItemById(id);
                if (itemById.isPresent()) {
                    ShopItem shopItem = itemById.get();
                    if (strings[1].equalsIgnoreCase("buy")) {
                        shopItem.getShoppingPrice().setPrice(coinType, amount);
                        shopItemsManager.getItemsGateway().upsertShopItem(shopItem);
                        shopItemsManager.reloadItems();
                        commandSender.sendMessage("Prezzo impostato per l'oggetto ID " + shopItem.getId());
                    } else if (strings[1].equalsIgnoreCase("sell")) {
                        shopItem.getSellingPrice().setPrice(coinType, amount);
                        shopItemsManager.getItemsGateway().upsertShopItem(shopItem);
                        shopItemsManager.reloadItems();
                        commandSender.sendMessage("Prezzo impostato per l'oggetto ID " + shopItem.getId());
                    } else {
                        commandSender.sendMessage("Comando: /shopadmin setitemprice buy|sell <id> <valuta> <qta>");
                    }
                } else {
                    commandSender.sendMessage("L'ID non esiste. Comando: /shopadmin setitemprice buy|sell <id> <valuta> <qta>");
                }
            } catch (EnumConstantNotPresentException | IllegalArgumentException enumConstantNotPresentException) {
                commandSender.sendMessage("Quella valuta (" + strings[2] + ") non esiste.");
            }

        } catch (NumberFormatException numberFormatException) {
            commandSender.sendMessage("L'ID o la quantità non è un numero");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 2) {
            suggestions.add("buy");
            suggestions.add("sell");
        }
        if (strings.length == 2) {
            suggestions = AresonSomniumAPI.instance.shopItemsManager.getItemsGateway().getAllItems(false).stream().map(shopItem -> shopItem.getId() + "").collect(Collectors.toList());
        }
        if (strings.length == 4) {
            boolean b = suggestions.addAll(Arrays.stream(CoinType.values()).map(coinType -> coinType.name().toLowerCase()).collect(Collectors.toList()));
        }
        if (strings.length == 5) {
            suggestions.add("<prezzo>");
        }
        return suggestions;
    }
}
