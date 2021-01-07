package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@SuppressWarnings("NullableProblems")
public class StatsCommand implements CommandExecutor, TabCompleter {

    private final PluginCommand command;
    private final AresonSomnium aresonSomnium;

    public StatsCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        command = this.aresonSomnium.getCommand("stats");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'stats' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 0:
                handleStats(commandSender);
                break;
            default:
                tooManyArguments(commandSender, "");
        }
        return true;
    }

    private void handleStats(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (Objects.nonNull(somniumPlayer)) {
                String toSend = ChatColor.GOLD + somniumPlayer.getPlayerName() + ChatColor.RESET + "'s stats:\n" +
                        "   Secondi giocati: " + somniumPlayer.getSecondsPlayedTotal() + "\n" +
                        "   Portafoglio:\n" +
                        "      $: " + Wallet.getBasicCoins(player) + "\n" +
                        "      Oboli: " + somniumPlayer.getWallet().getCharonCoins() + "\n" +
                        "      Gemme: " + somniumPlayer.getWallet().getForcedCoins();
                commandSender.sendMessage(toSend);
            } else {
                commandSender.sendMessage(MessageUtils.errorMessage("Riscontrato un problema con i tuoi dati. Segnala il problema  allo staff."));
            }
        } else {
            commandSender.sendMessage(MessageUtils.errorMessage("Comando disponibile solo da Player"));
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

    private void tooManyArguments(CommandSender commandSender, String function) {
        commandSender.sendMessage(MessageUtils.errorMessage("Troppi parametri forniti a " + function));
        commandSender.sendMessage(command.getUsage());
    }
}
