package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.AresonCommand;
import it.areson.aresonsomnium.commands.CommandParserCommand;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.elements.Pair;
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

    public static void buyItem(int id, Player player, CommandSender commandSender, boolean putTags) {
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
                                player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage(
                                        "item-buy-success",
                                        Pair.of("%coins%", price.getCoins().toString()),
                                        Pair.of("%gems%", price.getGems().toString()),
                                        Pair.of("%obols%", price.getObols().toString()))
                                );
                                SoundManager.playCoinsSound(player);
                                return;
                            } else {
                                player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-buy-not-enough-space"));
                            }
                        } else {
                            player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-buy-not-enough-money"));
                        }
                    } else {
                        player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-buy-not-buyable"));
                    }
                } else {
                    commandSender.sendMessage("ID '" + id + "' non trovato");
                }
            } else {
                player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-buy-error"));
            }
            SoundManager.playDeniedSound(player);
        } else {
            commandSender.sendMessage("Giocatore non trovato");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // / /shopadmin buyitem <player> <id> <true/false>
        try {
            boolean putTags = true;
            if(strings.length == 4) {
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
