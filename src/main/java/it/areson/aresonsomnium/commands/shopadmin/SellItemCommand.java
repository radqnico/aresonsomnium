package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.players.SomniumPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AresonCommand("sellitem")
public class SellItemCommand extends CommandParserCommand {

    public static void buyItem(int id, Player player, CommandSender commandSender) {
        if (player != null) {
            SomniumPlayer somniumPlayer = AresonSomniumAPI.instance.getSomniumPlayerManager().getSomniumPlayer(player);
            if (somniumPlayer != null) {
                Optional<ShopItem> itemById = AresonSomniumAPI.instance.shopItemsManager.getItemsGateway().getItemById(id);
                if (itemById.isPresent()) {
                    ShopItem shopItem = itemById.get();
                    if(shopItem.getSellingPrice().isPriceReady()) {
                        ItemStack itemStack = shopItem.getItemStack(false);
                        PlayerInventory inventory = player.getInventory();
                        if (inventory.contains(itemStack)) {
                            while (inventory.contains(itemStack)) {
                                inventory.remove(itemStack);
                                Wallet.addCoins(player, shopItem.getSellingPrice().getCoins());
                                somniumPlayer.getWallet().changeObols(shopItem.getSellingPrice().getObols());
                                somniumPlayer.getWallet().changeGems(shopItem.getSellingPrice().getGems());
                                player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-sell-success"));
                            }
                        } else {
                            player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-sell-not-present"));
                        }
                    }else{
                        player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-sell-not-sellable"));
                    }
                } else {
                    commandSender.sendMessage("ID '" + id + "' non trovato");
                }
            } else {
                player.sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-sell-error"));
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
