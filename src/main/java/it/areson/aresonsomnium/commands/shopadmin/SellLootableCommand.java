package it.areson.aresonsomnium.commands.shopadmin;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.AresonCommand;
import it.areson.aresonsomnium.commands.CommandParserCommand;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.players.SomniumPlayer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AresonCommand("selllootable")
public class SellLootableCommand extends CommandParserCommand {
    private void sellItem(CommandSender commandSender, Player player, Material material, int quantity) {
        SomniumPlayer somniumPlayer = AresonSomniumAPI.instance.getSomniumPlayerManager().getSomniumPlayer(player);
        if (somniumPlayer == null)
        {
            commandSender.sendMessage("Il giocatore non esiste nel somnium");
            return;
        }
        Optional<ShopItem> item = AresonSomniumAPI.instance.shopItemsManager.getItemsGateway().getShopItemByMaterialAmount(material, quantity);
        if (!item.isPresent())
        {
            commandSender.sendMessage("Combinazione materiale-quantità non esistente");
            return;
        }
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> stacks = getStackFromInventory(inventory, material);
        if (stacks.size() <= 0)
        {
            commandSender.sendMessage("Non è presente il materiale nell'inventario");
            return;
        }
        int totalAmount = getTotalAmount(stacks);
        if (totalAmount < quantity)
        {
            commandSender.sendMessage("Quantità insufficiente");
            return;
        }
        removeFromInventory(inventory, stacks, quantity);
        somniumPlayer.givePriceAmount(item.get().getSellingPrice());
        commandSender.sendMessage("Vendita completata");
    }

    private int getTotalAmount(List<ItemStack> stacks)
    {
        int totalAmount = 0;
        for (ItemStack stack : stacks)
        {
            totalAmount += stack.getAmount();
        }
        return  totalAmount;
    }

    private List<ItemStack> getStackFromInventory(PlayerInventory inventory, Material material)
    {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : inventory)
        {
            if (stack == null)
            {
                continue;
            }
            if (stack.getType().equals(material))
            {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private void removeFromInventory(PlayerInventory inventory, List<ItemStack> stacks, int amountToRemove)
    {
        for (ItemStack stack : stacks)
        {
            int amount = stack.getAmount();
            if (amount > amountToRemove)
            {
                stack.setAmount(amount - amountToRemove);
                break;
            }
            amountToRemove -= amount;
            stack.setAmount(0);
            if (amountToRemove <= 0)
            {
                break;
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // /shopadmin selllootable player material quantity
        try {
            int quantity = Integer.parseInt(strings[3]);
            String materialId = strings[2];
            String playerName = strings[1];
            Player player = AresonSomniumAPI.instance.getServer().getPlayer(playerName);
            Material material = Material.getMaterial(materialId);
            if (material == null)
            {
                commandSender.sendMessage("Nessun materiale trovato con l'id.");
                return true;
            }
            if (quantity <= 0 || quantity > material.getMaxStackSize())
            {
                commandSender.sendMessage("La quantità non è valida");
                return true;
            }
            this.sellItem(commandSender, player, material, quantity);
        } catch (NumberFormatException numberFormatException) {
            commandSender.sendMessage("La quantità selezionata non è un numero");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> suggestions = new ArrayList<>();
        switch (strings.length)
        {
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
