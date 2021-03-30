package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.AresonCommand;
import it.areson.aresonsomnium.commands.CommandParserCommand;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.elements.Pair;
import it.areson.aresonsomnium.players.SomniumPlayer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AresonCommand("sellitem")
public class SellItemCommand extends CommandParserCommand {

    public static void buyItem(int id, Player player, CommandSender commandSender) {
        if (player != null) {
            SomniumPlayer somniumPlayer = AresonSomniumAPI.instance.getSomniumPlayerManager().getSomniumPlayer(player);
            if (somniumPlayer != null) {
                Optional<ShopItem> itemById = AresonSomniumAPI.instance.shopItemsManager.getItemsGateway().getItemById(id);
                if (itemById.isPresent()) {
                    ShopItem shopItem = itemById.get();
                    if (shopItem.getSellingPrice().isPriceReady()) {
                        PlayerInventory inventory = player.getInventory();
                        sellIfContains(somniumPlayer, shopItem, id, inventory);
                    } else {
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

    public static void sellIfContains(SomniumPlayer somniumPlayer, ShopItem shopItem, int id, Inventory inventory) {
        Material type = shopItem.getItemStack(false, false).getType();
        if (inventory.contains(type)) {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack currentItem = inventory.getItem(i);
                if (currentItem != null && currentItem.getType().equals(type)) {
                    ItemMeta itemMeta = currentItem.getItemMeta();
                    if (itemMeta != null) {
                        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
                        Integer currentId = persistentDataContainer.getOrDefault(new NamespacedKey(AresonSomniumAPI.instance, "id"), PersistentDataType.INTEGER, -1);
                        if (currentId == id) {
                            int amount = currentItem.getAmount();
                            inventory.clear(i);
                            Price sellingPrice = shopItem.getSellingPrice().clone();
                            sellingPrice.multiply(amount);
                            somniumPlayer.givePriceAmount(sellingPrice);
                            somniumPlayer.getPlayer().sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage(
                                    "item-sell-success",
                                    Pair.of("%coins%", sellingPrice.getCoins().toString()),
                                    Pair.of("%gems%", sellingPrice.getGems().toString()),
                                    Pair.of("%obols%", sellingPrice.getObols().toString())
                            ));
                        }
                    }
                }
            }

        } else {
            somniumPlayer.getPlayer().sendMessage(AresonSomniumAPI.instance.getMessageManager().getPlainMessage("item-sell-not-present"));
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // / /shopadmin sellitem <player> <id>
        try {
            int id = Integer.parseInt(strings[2]);
            String playerName = strings[1];
            Player player = AresonSomniumAPI.instance.getServer().getPlayer(playerName);
            buyItem(id, player, commandSender);
        } catch (NumberFormatException numberFormatException) {
            commandSender.sendMessage("L'ID o non è un numero");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 2) {
            return null;
        }
        if (strings.length == 3) {
            boolean b = suggestions.addAll(Arrays.stream(CoinType.values()).map(coinType -> coinType.name().toLowerCase()).collect(Collectors.toList()));
        }
        return suggestions;
    }
}
