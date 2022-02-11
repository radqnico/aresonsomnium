package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonlib.commands.shapes.CompleteCommand;
import it.areson.aresonlib.files.MessageManager;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.economy.items.ShopItemsManager;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.SoundManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class SellLootableCommand implements CompleteCommand {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;
    private final ShopItemsManager shopItemsManager;

    public SellLootableCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        this.messageManager = aresonSomnium.getMessageManager();
        this.shopItemsManager = aresonSomnium.getShopItemsManager();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        // shopadmin selllootable <playerName> <material> <quantity>
        if (arguments.length >= 3) {
            Player player = aresonSomnium.getServer().getPlayer(arguments[0]);
            if (player == null) {
                messageManager.sendMessage(commandSender, "player-invalid");
                return true;
            }
            try {
                int quantity = Integer.parseInt(arguments[2]);
                Material material = Material.getMaterial(arguments[1]);
                //TODO
                System.out.println(material);

                if (material == null) {
                    commandSender.sendMessage("Nessun materiale trovato con l'id.");
                    return true;
                }
                sellItem(commandSender, player, material, quantity);
            } catch (Exception exception) {
                messageManager.sendFreeMessage(commandSender, "Quantità non valida");
            }
        } else {
            messageManager.sendMessage(commandSender, "not-enough-arguments");
        }
        return true;
    }

    private void sellItem(CommandSender commandSender, Player player, Material material, int quantity) {
        SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
        if (somniumPlayer == null) {
            commandSender.sendMessage("Il giocatore non esiste nel somnium");
            return;
        }
        Optional<ShopItem> item = shopItemsManager.getItemsGateway().getShopItemByMaterialAmount(material, quantity);
        if (item.isEmpty()) {
            commandSender.sendMessage("Combinazione materiale-quantità non esistente");
            return;
        }
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> stacks = getStackFromInventory(inventory, material);
        if (stacks.size() <= 0) {
            commandSender.sendMessage("Non è presente il materiale nell'inventario");
            SoundManager.playDeniedSound(somniumPlayer.getPlayer());
            return;
        }
        int totalAmount = getTotalAmount(stacks);
        if (totalAmount < quantity) {
            commandSender.sendMessage("Quantità insufficiente");
            SoundManager.playDeniedSound(somniumPlayer.getPlayer());
            return;
        }
        removeFromInventory(stacks, quantity);
        somniumPlayer.givePriceAmount(item.get().getSellingPrice());
        SoundManager.playCoinsSound(player);
        commandSender.sendMessage("Vendita completata");
    }

    private int getTotalAmount(List<ItemStack> stacks) {
        int totalAmount = 0;
        for (ItemStack stack : stacks) {
            totalAmount += stack.getAmount();
        }
        return totalAmount;
    }

    private List<ItemStack> getStackFromInventory(PlayerInventory inventory, Material material) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : inventory) {
            if (stack == null) {
                continue;
            }
            if (stack.getType().equals(material)) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private void removeFromInventory(List<ItemStack> stacks, int amountToRemove) {
        for (ItemStack stack : stacks) {
            int amount = stack.getAmount();
            if (amount > amountToRemove) {
                stack.setAmount(amount - amountToRemove);
                break;
            }
            amountToRemove -= amount;
            stack.setAmount(0);
            if (amountToRemove <= 0) {
                break;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] arguments) {
        List<String> suggestions = new ArrayList<>();
        //TODO
        switch (arguments.length) {
            case 2:
                return null;
            case 3:
                Arrays.stream(Material.values()).forEach(e -> suggestions.add(e.name()));
                break;
            case 4:
                suggestions.add("Quantity");
            default:
                break;
        }
        return suggestions;
    }
}
