package it.areson.aresonsomnium.commands.player;

import it.areson.aresonlib.minecraft.files.MessageManager;
import it.areson.aresonlib.minecraft.utils.Substitution;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Constants;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.HashSet;

@SuppressWarnings("NullableProblems")
public class SellCommand implements CommandExecutor {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;
    private final HashSet<String> playersWithAutoSellActive;

    public SellCommand(AresonSomnium aresonSomnium, String command) {
        this.aresonSomnium = aresonSomnium;
        this.messageManager = aresonSomnium.getMessageManager();
        this.playersWithAutoSellActive = aresonSomnium.getPlayersWithAutoSellActive();

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
            BigDecimal sold;

            switch (command.getName().toLowerCase()) {
                case Constants.SELL_HAND_COMMAND -> {
                    ItemStack[] itemArray = {player.getInventory().getItemInMainHand()};
                    sold = aresonSomnium.sellItems(player, itemArray);
                    if (sold.compareTo(BigDecimal.ZERO) > 0) {
                        messageManager.sendMessage(player, "item-sold", new Substitution("%money%", "" + sold));
                    } else {
                        messageManager.sendMessage(player, "item-not-sellable");
                    }
                }
                case Constants.SELL_ALL_COMMAND -> {
                    sold = aresonSomnium.sellItems(player, player.getInventory().getContents());
                    if (sold.compareTo(BigDecimal.ZERO) > 0) {
                        messageManager.sendMessage(player, "items-sold", new Substitution("%money%", "" + sold));
                    } else {
                        messageManager.sendMessage(player, "items-not-sellable");
                    }
                }
                case Constants.AUTO_SELL_COMMAND -> {
                    String playerName = player.getName();
                    if (!playersWithAutoSellActive.contains(playerName)) {
                        playersWithAutoSellActive.add(playerName);
                        messageManager.sendMessage(player, "autosell-activated");
                    } else {
                        playersWithAutoSellActive.remove(playerName);
                        messageManager.sendMessage(player, "autosell-deactivated");
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
