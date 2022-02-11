package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonlib.commands.shapes.CompleteCommand;
import it.areson.aresonlib.files.MessageManager;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class SetItemPriceCommand implements CompleteCommand {

    private final MessageManager messageManager;
    private final ShopItemsManager shopItemsManager;
    private final String commandUsage;
    private final ArrayList<String> coinTypes;

    public SetItemPriceCommand(AresonSomnium aresonSomnium) {
        this.messageManager = aresonSomnium.getMessageManager();
        this.shopItemsManager = aresonSomnium.getShopItemsManager();

        this.commandUsage = "/shopadmin setitemprice <buy|sell> <itemId> <coinType> <price>";
        this.coinTypes = new ArrayList<>();
        this.coinTypes.addAll(Arrays.stream(CoinType.values())
                .map(coinType -> coinType.name().toLowerCase()).toList());
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        // shopadmin setitemprice <buy|sell> <itemId> <coinType> <price>
        if (arguments.length >= 4) {
            try {
                int itemId = Integer.parseInt(arguments[1]);
                CoinType coinType = CoinType.valueOf(arguments[2].toUpperCase());
                BigDecimal price = new BigDecimal(arguments[3]);

                Optional<ShopItem> itemById = shopItemsManager.getItemsGateway().getItemById(itemId);
                if (itemById.isPresent()) {
                    ShopItem shopItem = itemById.get();
                    if (arguments[0].equalsIgnoreCase("buy")) {
                        shopItem.getShoppingPrice().setPrice(coinType, price);
                        shopItemsManager.getItemsGateway().upsertShopItem(shopItem);
                        shopItemsManager.reloadItems();
                        messageManager.sendFreeMessage(commandSender, "Prezzo impostato per l'oggetto ID " + shopItem.getId());
                    } else if (arguments[0].equalsIgnoreCase("sell")) {
                        shopItem.getSellingPrice().setPrice(coinType, price);
                        shopItemsManager.getItemsGateway().upsertShopItem(shopItem);
                        shopItemsManager.reloadItems();
                        messageManager.sendFreeMessage(commandSender, "Prezzo impostato per l'oggetto ID " + shopItem.getId());
                    } else {
                        messageManager.sendFreeMessage(commandSender, "Comando: " + commandUsage);
                    }
                } else {
                    messageManager.sendFreeMessage(commandSender, "Questo item non esiste. Comando: " + commandUsage);
                }
            } catch (NumberFormatException exception) {
                messageManager.sendFreeMessage(commandSender, "Quantità non valida");
            } catch (IllegalArgumentException exception) {
                messageManager.sendFreeMessage(commandSender, "Tipo di valuta invalida");
            }
        } else {
            messageManager.sendMessage(commandSender, "not-enough-arguments");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] arguments) {
        List<String> suggestions = new ArrayList<>();
        switch (arguments.length) {
            case 0 -> {
                suggestions.add("buy");
                suggestions.add("sell");
            }
            case 1 -> suggestions = shopItemsManager.getItemsGateway()
                    .getAllItems(false).parallelStream()
                    .map(shopItem -> shopItem.getId() + "").collect(Collectors.toList());
            case 2 -> {
                return coinTypes.parallelStream().filter(coinType -> coinType.startsWith(arguments[0])).toList();
            }
        }
        return suggestions;
    }
}
