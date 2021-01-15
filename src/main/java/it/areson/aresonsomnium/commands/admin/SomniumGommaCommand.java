package it.areson.aresonsomnium.commands.admin;

import com.destroystokyo.paper.block.TargetBlockInfo;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static it.areson.aresonsomnium.utils.MessageUtils.errorMessage;
import static it.areson.aresonsomnium.utils.MessageUtils.successMessage;


@SuppressWarnings("NullableProblems")
public class SomniumGommaCommand implements CommandExecutor, TabCompleter {

    private final String[] subCommands = new String[]{"setBlock", "addItem"};
    private final AresonSomnium aresonSomnium;

    public SomniumGommaCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        PluginCommand command = this.aresonSomnium.getCommand("gomma");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'gomma' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 0:
                MessageUtils.notEnoughArguments(commandSender, command);
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "setblock":
                        handleSetBlock(commandSender);
                        break;
                    case "additem":
                        handleAddItem(commandSender);
                        break;
                    default:
                        commandSender.sendMessage(errorMessage("Funzione non trovata"));
                }
                break;
        }
        return true;
    }

    private void handleAddItem(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
            if(!Material.AIR.equals(itemInMainHand.getType())){
                ItemStack itemStack = itemInMainHand.asOne();
                aresonSomnium.getGommaObjectsFileReader().storeItem(itemStack);
                commandSender.sendMessage(successMessage("Oggetto aggiunto alla lista Gomma Gomma"));
            }else{
                commandSender.sendMessage(errorMessage("Non hai nulla in mano"));
            }

        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
    }

    private void handleSetBlock(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            Block targetBlock = player.getTargetBlock(100, TargetBlockInfo.FluidMode.NEVER);
            if(Objects.nonNull(targetBlock)){
                Location location = targetBlock.getLocation();
                aresonSomnium.getGommaObjectsFileReader().setGommaBlock(location);
                commandSender.sendMessage(successMessage("Blocco Gomma Gomma impostato"));
            }
        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
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
