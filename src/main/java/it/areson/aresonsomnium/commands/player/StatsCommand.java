package it.areson.aresonsomnium.commands.player;

import it.areson.aresonlib.files.MessageManager;
import it.areson.aresonlib.utils.Substitution;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@SuppressWarnings("NullableProblems")
public class StatsCommand implements CommandExecutor, TabCompleter {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;

    public StatsCommand(AresonSomnium aresonSomnium, MessageManager messageManager) {
        this.aresonSomnium = aresonSomnium;
        this.messageManager = messageManager;
        PluginCommand command = this.aresonSomnium.getCommand("stats");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'stats' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            handleStats(commandSender);
        } else {
            MessageUtils.tooManyArguments(commandSender, command);
        }
        return true;
    }

    private void handleStats(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (Objects.nonNull(somniumPlayer)) {
                messageManager.sendMessage(player,
                        "stats-format",
                        new Substitution("%player%", player.getName()),
                        new Substitution("%secondsPlayed%", somniumPlayer.getSecondsPlayedTotal() + ""),
                        new Substitution("%coins%", Wallet.getCoins(player).toString()),
                        new Substitution("%obols%", somniumPlayer.getWallet().getObols().toString()),
                        new Substitution("%gems%", somniumPlayer.getWallet().getGems().toString())
                );
            } else {
                commandSender.sendMessage(MessageUtils.errorMessage("Riscontrato un problema con i tuoi dati. Segnala il problema  allo staff."));
            }
        } else {
            commandSender.sendMessage(MessageUtils.errorMessage("Comando disponibile solo da Player"));
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>();
    }
}
