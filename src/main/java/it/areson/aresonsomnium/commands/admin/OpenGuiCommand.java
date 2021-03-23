package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("NullableProblems")
public class OpenGuiCommand implements CommandExecutor, TabCompleter {

    private final AresonSomnium aresonSomnium;

    public OpenGuiCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;

        PluginCommand command = this.aresonSomnium.getCommand("OpenGui");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'OpenGui' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        switch (arguments.length) {
            case 0:
            case 1:
                MessageUtils.notEnoughArguments(commandSender, command);
                break;
            case 2:
                handleOpenGui(commandSender, arguments[0], arguments[1]);
                break;
            default:
                MessageUtils.tooManyArguments(commandSender, command);
        }
        return true;
    }

    private void handleOpenGui(CommandSender commandSender, String playerName, String guiName) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);

        if (player != null) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);

            if (somniumPlayer != null) {
                if (aresonSomnium.shopManager.isASavedGUI(guiName)) {
                    aresonSomnium.shopManager.openShopToPlayer(player, guiName);
                } else {
                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("gui-not-found"));
                }
            } else {
                commandSender.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("somniumplayer-not-found"));
            }
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            StringUtil.copyPartialMatches(strings[0], aresonSomnium.getSomniumPlayerManager().getOnlinePlayersNames(), suggestions);
        } else if (strings.length == 2) {
            StringUtil.copyPartialMatches(strings[1], aresonSomnium.shopManager.getGuis().keySet(), suggestions);
        }
        return suggestions;
    }
}
