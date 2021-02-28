package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Constants;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import it.areson.aresonsomnium.shops.items.BlockPrice;
import it.areson.aresonsomnium.utils.file.MessageManager;
import it.areson.aresonsomnium.utils.Pair;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;

import static it.areson.aresonsomnium.Constants.PERMISSION_MULTIPLIER;

@SuppressWarnings("NullableProblems")
public class SellCommand implements CommandExecutor {

    private final AresonSomnium aresonSomnium;
    private final HashMap<Material, String> blocksPermission = new HashMap<Material, String>() {{
        put(Material.COBBLESTONE, Constants.PERMISSION_SELVA);
        put(Material.NETHERRACK, Constants.PERMISSION_ANTINFERNO);
        put(Material.COAL_BLOCK, Constants.PERMISSION_SECONDO_GIRONE);
        put(Material.RED_NETHER_BRICKS, Constants.PERMISSION_QUARTO_GIRONE);
        put(Material.MAGMA_BLOCK, Constants.PERMISSION_SESTO_GIRONE);
        put(Material.RED_CONCRETE, Constants.PERMISSION_OTTAVO_GIRONE);
    }};

    private final MessageManager messageManager;

    public SellCommand(AresonSomnium plugin, String command) {
        aresonSomnium = plugin;
        messageManager = aresonSomnium.getMessageManager();

        PluginCommand pluginCommand = aresonSomnium.getCommand(command);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
        } else {
            aresonSomnium.getLogger().warning("Comando " + command + " non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            String commandName = command.getName();

            if (commandName.equalsIgnoreCase(Constants.SELL_HAND_COMMAND)) {
                ItemStack[] itemArray = {player.getInventory().getItemInMainHand()};
                BigDecimal sold = sellItems(player, itemArray);

                if (sold.compareTo(BigDecimal.ZERO) > 0) {
                    messageManager.sendPlainMessage(player, "item-sold", Pair.of("%money%", "" + sold));
                } else {
                    messageManager.sendPlainMessage(player, "item-not-sellable");
                }
            } else if (commandName.equalsIgnoreCase(Constants.SELL_ALL_COMMAND)) {
                BigDecimal sold = sellItems(player, player.getInventory().getContents());

                if (sold.compareTo(BigDecimal.ZERO) > 0) {
                    messageManager.sendPlainMessage(player, "items-sold", Pair.of("%money%", "" + sold));
                } else {
                    messageManager.sendPlainMessage(player, "items-not-sellable");
                }
            } else {
                aresonSomnium.getLogger().severe("Command not registered in SellCommand");
            }
        } else {
            commandSender.sendMessage("Player only command");
        }

        return true;
    }



    private BigDecimal sellItems(Player player, ItemStack[] itemStacks) {
        double multiplier = aresonSomnium.getPlayerMultiplier(player);

        //Getting amount
        BigDecimal coinsToGive = Arrays.stream(itemStacks).parallel().reduce(BigDecimal.ZERO, (total, itemStack) -> {
            try {
                if (itemStack != null) {
                    String permissionRequired = blocksPermission.get(itemStack.getType());
                    if (permissionRequired != null && player.hasPermission(permissionRequired)) {
                        BigDecimal itemValue = BlockPrice.getPrice(itemStack.getType());
                        itemValue = itemValue.multiply(BigDecimal.valueOf(itemStack.getAmount()));

                        total = total.add(itemValue);
                        player.getInventory().remove(itemStack);
                    }
                }
            } catch (MaterialNotSellableException ignored) {
            }
            return total;
        }, BigDecimal::add);

        coinsToGive = coinsToGive.multiply(BigDecimal.valueOf(multiplier));
        Wallet.addCoins(player, coinsToGive);

        return coinsToGive;
    }

}
