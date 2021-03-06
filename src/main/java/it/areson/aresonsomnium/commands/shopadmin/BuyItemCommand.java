package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonlib.minecraft.commands.shapes.CompleteCommand;
import it.areson.aresonlib.minecraft.files.MessageManager;
import it.areson.aresonlib.minecraft.utils.Substitution;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.SoundManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class BuyItemCommand implements CompleteCommand {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;
    private final ShopItemsManager shopItemsManager;

    public BuyItemCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        this.messageManager = aresonSomnium.getMessageManager();
        this.shopItemsManager = aresonSomnium.getShopItemsManager();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        // shopadmin buyitem <playerName> <itemId> <true/false>
        if (arguments.length >= 2) {
            Player player = aresonSomnium.getServer().getPlayer(arguments[0]);
            if (player == null) {
                messageManager.sendMessage(commandSender, "player-invalid");
                return true;
            }
            try {
                int itemId = Integer.parseInt(arguments[1]);

                boolean putTags = true;
                if (arguments.length > 2) {
                    putTags = Boolean.parseBoolean(arguments[2]);
                }

                buyItem(itemId, player, commandSender, putTags);
            } catch (NumberFormatException exception) {
                messageManager.sendFreeMessage(commandSender, "L'ID o la quantit?? non ?? un numero");
            }
        } else {
            messageManager.sendMessage(commandSender, "not-enough-arguments");
        }
        return true;
    }

    public void buyItem(int id, Player player, CommandSender commandSender, boolean putTags) {
        if (player != null) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (somniumPlayer != null) {
                Optional<ShopItem> itemById = shopItemsManager.getItemsGateway().getItemById(id);
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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }

}
