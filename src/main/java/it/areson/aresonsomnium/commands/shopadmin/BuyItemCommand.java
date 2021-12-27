package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonlib.file.MessageManager;
import it.areson.aresonlib.utils.Substitution;
import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.AresonCommand;
import it.areson.aresonsomnium.commands.CommandParserCommand;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.SoundManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AresonCommand("buyitem")
public class BuyItemCommand extends CommandParserCommand {

    private final MessageManager messageManager;

    public BuyItemCommand(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    public void buyItem(int id, Player player, CommandSender commandSender, boolean putTags) {
        if (player != null) {
            SomniumPlayer somniumPlayer = AresonSomniumAPI.instance.getSomniumPlayerManager().getSomniumPlayer(player);
            if (somniumPlayer != null) {
                Optional<ShopItem> itemById = AresonSomniumAPI.instance.shopItemsManager.getItemsGateway().getItemById(id);
                if (itemById.isPresent()) {
                    ShopItem shopItem = itemById.get();
                    if (shopItem.getShoppingPrice().isPriceReady()) {
                        if (somniumPlayer.canAfford(shopItem.getShoppingPrice())) {
                            if (player.getInventory().addItem(shopItem.getItemStack(false, false, putTags)).isEmpty()) {
                                Price price = shopItem.getShoppingPrice();
                                somniumPlayer.takePriceAmount(price);
                                messageManager.sendMessage(
                                        player,
                                        "item-buy-success",
                                        new Substitution("%coins%", price.getCoins().toString()),
                                        new Substitution("%gems%", price.getGems().toString()),
                                        new Substitution("%obols%", price.getObols().toString())
                                );
                                SoundManager.playCoinsSound(player);
                                return;
                            } else {
                                messageManager.sendMessage(commandSender, "item-buy-not-enough-space");
                            }
                        } else {
                            messageManager.sendMessage(commandSender, "item-buy-not-enough-money");
                        }
                    } else {
                        messageManager.sendMessage(commandSender, "item-buy-not-buyable");
                    }
                } else {
                    messageManager.sendFreeMessage(commandSender, "ID '" + id + "' non trovato");
                }
            } else {
                messageManager.sendMessage(commandSender, "item-buy-error");
            }
            SoundManager.playDeniedSound(player);
        } else {
            messageManager.sendMessage(commandSender, "Giocatore non trovato");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // / /shopadmin buyitem <player> <id> <true/false>
        try {
            boolean putTags = true;
            if (strings.length == 4) {
                putTags = Boolean.parseBoolean(strings[3]);
            }
            int id = Integer.parseInt(strings[2]);
            String playerName = strings[1];
            Player player = AresonSomniumAPI.instance.getServer().getPlayer(playerName);
            buyItem(id, player, commandSender, putTags);
        } catch (NumberFormatException numberFormatException) {
            commandSender.sendMessage("L'ID o la quantità non è un numero");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
