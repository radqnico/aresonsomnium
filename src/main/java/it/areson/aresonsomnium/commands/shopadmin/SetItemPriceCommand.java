package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonlib.commands.shapes.SubCommand;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Optional;

public class SetItemPriceCommand implements SubCommand {

    private final AresonSomnium aresonSomnium;

    public SetItemPriceCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
    }

    @Override
    public void onCommand(CommandSender commandSender, String[] arguments) {
        // / /shopadmin setitemprice buy <id> <valuta> <qta>
        ShopItemsManager shopItemsManager = aresonSomnium.getShopItemsManager();
        try {
            int id = Integer.parseInt(arguments[2]);
            try {
                CoinType coinType = CoinType.valueOf(arguments[3].toUpperCase());
                BigDecimal amount = new BigDecimal(arguments[4]);
                Optional<ShopItem> itemById = shopItemsManager.getItemsGateway().getItemById(id);
                if (itemById.isPresent()) {
                    ShopItem shopItem = itemById.get();
                    if (arguments[1].equalsIgnoreCase("buy")) {
                        shopItem.getShoppingPrice().setPrice(coinType, amount);
                        shopItemsManager.getItemsGateway().upsertShopItem(shopItem);
                        shopItemsManager.reloadItems();
                        commandSender.sendMessage("Prezzo impostato per l'oggetto ID " + shopItem.getId());
                    } else if (arguments[1].equalsIgnoreCase("sell")) {
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
                commandSender.sendMessage("Quella valuta (" + arguments[2] + ") non esiste.");
            }

        } catch (NumberFormatException numberFormatException) {
            commandSender.sendMessage("L'ID o la quantità non è un numero");
        }
    }

//    @Override
//    public @Nullable List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
//        List<String> suggestions = new ArrayList<>();
//        if (strings.length == 2) {
//            suggestions.add("buy");
//            suggestions.add("sell");
//        }
//        if (strings.length == 2) {
//            suggestions = aresonSomnium.getShopItemsManager().getItemsGateway()
//                    .getAllItems(false).stream()
//                    .map(shopItem -> shopItem.getId() + "").collect(Collectors.toList());
//        }
//        if (strings.length == 4) {
//            suggestions.addAll(Arrays.stream(CoinType.values())
//                    .map(coinType -> coinType.name().toLowerCase()).toList());
//        }
//        if (strings.length == 5) {
//            suggestions.add("<prezzo>");
//        }
//        return suggestions;
//    }
}
