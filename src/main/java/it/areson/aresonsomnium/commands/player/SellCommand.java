package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Constants;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import it.areson.aresonsomnium.shops.items.BlockPrice;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class SellCommand implements CommandExecutor, TabCompleter {

    private final AresonSomnium aresonSomnium;
    private final PluginCommand pluginCommand;
    private final HashMap<Material, String> blocksPermission;

    public SellCommand(AresonSomnium aresonSomnium, String command) {
        this.aresonSomnium = aresonSomnium;

        pluginCommand = this.aresonSomnium.getCommand(command);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'sell' non dichiarato");
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
                    player.sendMessage(ChatColor.GOLD + "Hai venduto quest'oggetto per " + sold);
                } else {
                    player.sendMessage(ChatColor.RED + "Quest'oggetto non è vendibile");
                }
            } else if (commandName.equalsIgnoreCase(Constants.sellAllCommand)) {
                BigDecimal sold = sellItems(player, player.getInventory().getContents());

                if (sold.compareTo(BigDecimal.ZERO) > 0) {
                    player.sendMessage(ChatColor.GOLD + "Hai venduto tutti gli oggetti vendibili nel tuo inventario per " + sold);
                } else {
                    player.sendMessage(ChatColor.RED + "Non hai nessun oggetto vendibile nell'inventario");
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

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        //TODO
//        List<String> suggestions = new ArrayList<>();
//        if (strings.length == 1) {
//            StringUtil.copyPartialMatches(strings[0], pluginCommand.ge, suggestions);
//        }
//        return suggestions;
        return null;
    }

}
