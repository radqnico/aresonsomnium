package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.entities.CoinType;
import static it.areson.aresonsomnium.utils.MessageUtils.*;
import org.bukkit.command.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@SuppressWarnings("NullableProblems")
public class SomniumTestCommand implements CommandExecutor, TabCompleter {

    private final PluginCommand command;
    private final String[] subCommands = new String[]{"isSerialization"};
    private AresonSomnium aresonSomnium;

    public SomniumTestCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        command = this.aresonSomnium.getCommand("somniumtest");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'somniumtest' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 0:
                notEnoughArguments(commandSender, command);
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "isSerialization":
                        itemStackSerializationHandler(commandSender);
                        break;
                }
                break;
            default:
                commandSender.sendMessage(errorMessage("Funzione non trovata"));
        }
        return true;
    }

    private void itemStackSerializationHandler(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
            byte[] bytes = itemInMainHand.serializeAsBytes();
            player.sendMessage(successMessage("Serializzazione completata"));
            player.sendMessage(Arrays.toString(bytes));
        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
        commandSender.sendMessage();
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            StringUtil.copyPartialMatches(strings[0], Arrays.asList(subCommands), suggestions);
        }
        return suggestions;
    }
}
