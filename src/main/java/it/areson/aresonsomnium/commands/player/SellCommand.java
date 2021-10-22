package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Constants;
import it.areson.aresonsomnium.elements.Pair;
import it.areson.aresonsomnium.utils.file.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

@SuppressWarnings("NullableProblems")
public class SellCommand implements CommandExecutor {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;

    public SellCommand(AresonSomnium aresonSomnium, String command) {
        this.aresonSomnium = aresonSomnium;
        messageManager = this.aresonSomnium.getMessageManager();

        PluginCommand pluginCommand = this.aresonSomnium.getCommand(command);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando " + command + " non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        if (commandSender instanceof Player player) {
            String commandName = command.getName();

            BigDecimal sold;
            switch (commandName.toLowerCase()) {
                case Constants.SELL_HAND_COMMAND -> {
                    ItemStack[] itemArray = {player.getInventory().getItemInMainHand()};
                    sold = aresonSomnium.sellItems(player, itemArray);
                    if (sold.compareTo(BigDecimal.ZERO) > 0) {
                        messageManager.sendPlainMessage(player, "item-sold", Pair.of("%money%", "" + sold));
                    } else {
                        messageManager.sendPlainMessage(player, "item-not-sellable");
                    }
                }
                case Constants.SELL_ALL_COMMAND -> {
                    sold = aresonSomnium.sellItems(player, player.getInventory().getContents());
                    if (sold.compareTo(BigDecimal.ZERO) > 0) {
                        messageManager.sendPlainMessage(player, "items-sold", Pair.of("%money%", "" + sold));
                    } else {
                        messageManager.sendPlainMessage(player, "items-not-sellable");
                    }
                }
                case Constants.AUTO_SELL_COMMAND -> {
                    String playerName = player.getName();
                    if (aresonSomnium.playersWithAutoSellActive.contains(playerName)) {
                        aresonSomnium.playersWithAutoSellActive.add(playerName);
                        messageManager.sendPlainMessage(player, "autosell-activated");
                    } else {
                        aresonSomnium.playersWithAutoSellActive.remove(playerName);
                        messageManager.sendPlainMessage(player, "autosell-deactivated");
                    }
                }
                default -> aresonSomnium.getLogger().severe("Command not registered in SellCommand");
            }
        } else {
            commandSender.sendMessage("Player only command");
        }

        return true;
    }

}
