package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Constants;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import it.areson.aresonsomnium.shops.items.BlockPrice;
import it.areson.aresonsomnium.utils.MessageManager;
import it.areson.aresonsomnium.utils.Pair;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class SellCommand implements CommandExecutor {

    private final AresonSomnium aresonSomnium;
    private final HashMap<Material, String> blocksPermission;
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

        blocksPermission = new HashMap<>();
        blocksPermission.put(Material.COBBLESTONE, Constants.permissionSelva);
        blocksPermission.put(Material.NETHERRACK, Constants.permissionAntinferno);
        blocksPermission.put(Material.COAL_BLOCK, Constants.permissionSecondoGirone);
        blocksPermission.put(Material.RED_NETHER_BRICKS, Constants.permissionQuartoGirone);
        blocksPermission.put(Material.MAGMA_BLOCK, Constants.permissionSestoGirone);
        blocksPermission.put(Material.RED_CONCRETE, Constants.permissionOttavoGirone);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            String commandName = command.getName();

            if (commandName.equalsIgnoreCase(Constants.sellHandCommand)) {
                ItemStack[] itemArray = {player.getInventory().getItemInMainHand()};
                BigDecimal sold = sellItems(player, itemArray);

                if (sold.compareTo(BigDecimal.ZERO) > 0) {
                    messageManager.sendPlainMessage(player, "item-sold", Pair.of("%money%", "" + sold));
                } else {
                    messageManager.sendPlainMessage(player, "item-not-sellable");
                }
            } else if (commandName.equalsIgnoreCase(Constants.sellAllCommand)) {
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

    private double getMultiplier(Player player) {
        double multiplier = 1.0;

        //Getting multiplier
        Optional<PermissionAttachmentInfo> optionalMultiplierPermission = player.getEffectivePermissions().stream().parallel()
                .filter(permission -> permission.getPermission().startsWith(Constants.sellMultiplierPermission))
                .findFirst();

        if (optionalMultiplierPermission.isPresent()) {
            String permission = optionalMultiplierPermission.get().getPermission();
            int lastDotPosition = permission.lastIndexOf(".");
            String stringMultiplier = permission.substring(lastDotPosition + 1);

            try {
                double value = Double.parseDouble(stringMultiplier);
                multiplier = value / 100;
            } catch (NumberFormatException event) {
                aresonSomnium.getLogger().severe("Error while parsing string multiplier to double: " + stringMultiplier);
            }
        }

        return multiplier;
    }

    private BigDecimal sellItems(Player player, ItemStack[] itemStacks) {
        double multiplier = getMultiplier(player);

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
        Wallet.addBasicCoins(player, coinsToGive);

        return coinsToGive;
    }

}