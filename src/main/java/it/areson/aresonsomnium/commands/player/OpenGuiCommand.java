package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.shops.ShopManager;
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
                notEnoughArguments(commandSender);
                break;
            case 1:
                handleOpenGui(commandSender, args[0]);
                break;
            default:
                tooManyArguments(commandSender, "");
        }
        return true;
    }

    private void handleOpenGui(CommandSender commandSender, String shopName) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            ShopManager shopManager = aresonSomnium.getShopManager();
            if (shopManager.isShop(shopName)) {
                shopManager.openShop(player, shopName);
            } else {
                player.sendMessage("La GUI richiesta non esiste");
            }
        } else {
            commandSender.sendMessage(MessageUtils.errorMessage("Comando disponibile solo da Player"));
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            StringUtil.copyPartialMatches(strings[0], aresonSomnium.getShopManager().getShops().keySet(), suggestions);
        }
        return suggestions;
    }

    private void notEnoughArguments(CommandSender commandSender) {
        commandSender.sendMessage(MessageUtils.errorMessage("Parametri non sufficienti"));
        commandSender.sendMessage(command.getUsage());
    }

    private void tooManyArguments(CommandSender commandSender, String function) {
        commandSender.sendMessage(MessageUtils.errorMessage("Troppi parametri forniti a " + function));
        commandSender.sendMessage(command.getUsage());
    }
}
