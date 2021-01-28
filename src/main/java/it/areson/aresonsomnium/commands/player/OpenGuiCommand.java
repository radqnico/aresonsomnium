package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.shops.guis.ShopManager;
import it.areson.aresonsomnium.utils.MessageManager;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("NullableProblems")
public class OpenGuiCommand implements CommandExecutor, TabCompleter {

    private final PluginCommand command;
    private final AresonSomnium aresonSomnium;

    public OpenGuiCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        command = this.aresonSomnium.getCommand("OpenGui");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'OpenGui' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 0:
                MessageUtils.notEnoughArguments(commandSender, command);
                break;
            case 1:
                handleOpenGui(commandSender, args[0]);
                break;
            default:
                MessageUtils.tooManyArguments(commandSender, command);
        }
        return true;
    }

    private void handleOpenGui(CommandSender commandSender, String guiName) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            ShopManager shopManager = aresonSomnium.getShopManager();
            if (shopManager.isPermanent(guiName)) {
                shopManager.openGuiToPlayer(player, guiName);
            } else {
                player.sendMessage(aresonSomnium.getMessages().getPlainMessage("gui-not-found"));
            }
        } else {
            commandSender.sendMessage(aresonSomnium.getMessages().getPlainMessage("player-only-command"));
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            StringUtil.copyPartialMatches(strings[0], aresonSomnium.getShopManager().getGuis().keySet(), suggestions);
        }
        return suggestions;
    }
}
