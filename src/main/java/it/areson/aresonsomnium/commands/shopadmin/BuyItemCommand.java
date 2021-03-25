package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.players.SomniumPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AresonCommand("buyitem")
public class BuyItemCommand extends CommandParserCommand {

    public static void buyItem(int id, Player player, CommandSender commandSender) {
        if (player != null) {
            SomniumPlayer somniumPlayer = AresonSomniumAPI.instance.getSomniumPlayerManager().getSomniumPlayer(player);
            if (somniumPlayer != null) {
                Optional<ShopItem> itemById = AresonSomniumAPI.instance.shopItemsManager.getItemsGateway().getItemById(id);
                if (itemById.isPresent()) {
                    ShopItem shopItem = itemById.get();
                    if (somniumPlayer.canAfford(shopItem.getShoppingPrice())) {
                        if (!player.getInventory().addItem(shopItem.getItemStack(false)).isEmpty()) {
                            player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-buy-success"));
                            Wallet.addCoins(player, shopItem.getShoppingPrice().getCoins().negate());
                            somniumPlayer.getWallet().changeObols(shopItem.getShoppingPrice().getObols().negate());
                            somniumPlayer.getWallet().changeGems(shopItem.getShoppingPrice().getGems().negate());
                        } else {
                            player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-buy-not-enough-space"));
                        }
                    } else {
                        player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-buy-not-enough-money"));
                    }
                } else {
                    commandSender.sendMessage("ID '" + id + "' non trovato");
                }
            } else {
                player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-buy-error"));
            }
        } else {
            commandSender.sendMessage("Giocatore non trovato");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // / /shopadmin buyitem <player> <id>
        try {
            int id = Integer.parseInt(strings[2]);
            String playerName = strings[1];
            Player player = AresonSomniumAPI.instance.getServer().getPlayer(playerName);
            buyItem(id, player, commandSender);
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
