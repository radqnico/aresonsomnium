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

import java.util.List;
import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class SellItemCommand implements CompleteCommand {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;
    private final ShopItemsManager shopItemsManager;

    public SellItemCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        this.messageManager = aresonSomnium.getMessageManager();
        this.shopItemsManager = aresonSomnium.getShopItemsManager();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        // shopadmin sellitem <playerName> <itemId>
        if (arguments.length >= 2) {
            Player player = aresonSomnium.getServer().getPlayer(arguments[0]);
            if (player == null) {
                messageManager.sendMessage(commandSender, "player-invalid");
                return true;
            }
            try {
                int itemId = Integer.parseInt(arguments[1]);
                sellItem(itemId, player, commandSender);
            } catch (NumberFormatException exception) {
                messageManager.sendFreeMessage(commandSender, "Quantit?? non valida");
            }
        } else {
            messageManager.sendMessage(commandSender, "not-enough-arguments");
        }
        return true;
    }

    public void sellItem(int id, Player player, CommandSender commandSender) {
        if (player != null) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (somniumPlayer != null) {
                Optional<ShopItem> itemById = shopItemsManager.getItemsGateway().getItemById(id);
                if (itemById.isPresent()) {
                    ShopItem shopItem = itemById.get();
                    if (shopItem.getSellingPrice().isPriceReady()) {
                        PlayerInventory inventory = player.getInventory();
                        sellIfContains(somniumPlayer, shopItem, id, inventory);
                    } else {
                        messageManager.sendMessage(commandSender, "item-sell-not-sellable");
                    }
                } else {
                    messageManager.sendFreeMessage(commandSender, "ID '" + id + "' non trovato");
                }
            } else {
                messageManager.sendMessage(commandSender, "item-sell-error");
            }
        } else {
            commandSender.sendMessage("Giocatore non trovato");
        }
    }

    public void sellIfContains(SomniumPlayer somniumPlayer, ShopItem shopItem, int id, Inventory inventory) {
        Material type = shopItem.getItemStack(false, false).getType();
        if (inventory.contains(type)) {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack currentItem = inventory.getItem(i);
                if (currentItem != null && currentItem.getType().equals(type)) {
                    ItemMeta itemMeta = currentItem.getItemMeta();
                    if (itemMeta != null) {
                        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
                        Integer currentId = persistentDataContainer.getOrDefault(new NamespacedKey(aresonSomnium, "id"), PersistentDataType.INTEGER, -1);
                        if (currentId == id) {
                            int amount = currentItem.getAmount();
                            inventory.clear(i);
                            Price sellingPrice = shopItem.getSellingPrice().clone();
                            sellingPrice.multiply(amount);
                            somniumPlayer.givePriceAmount(sellingPrice);
                            messageManager.sendMessage(
                                    somniumPlayer.getPlayer(),
                                    "item-sell-success",
                                    new Substitution("%coins%", sellingPrice.getCoins().toString()),
                                    new Substitution("%gems%", sellingPrice.getGems().toString()),
                                    new Substitution("%obols%", sellingPrice.getObols().toString())
                            );
                            SoundManager.playCoinsSound(somniumPlayer.getPlayer());
                            return;
                        }
                    }
                }
            }
        } else {
            messageManager.sendMessage(somniumPlayer.getPlayer(), "item-sell-not-present");
        }
        SoundManager.playDeniedSound(somniumPlayer.getPlayer());
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] arguments) {
        return null;
    }

}
