package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.entities.SomniumPlayer;
import org.bukkit.command.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class SomniumAdminCommand implements CommandExecutor, TabCompleter {

    private final String[] subCommands = new String[]{"stats"};
    private AresonSomnium aresonSomnium;

    public SomniumAdminCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        PluginCommand somnium = this.aresonSomnium.getCommand("SomniumAdmin");
        if (somnium != null) {
            somnium.setExecutor(this);
            somnium.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'somnium' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 0:
                notEnoughArguments(commandSender);
                break;
            case 1:
                switch (args[0]) {
                    case "stats":
                        notEnoughArguments(commandSender);
                        break;
                }
                break;
            case 2:
                switch (args[0]) {
                    case "stats":
                        handleStatsCommand(commandSender, args[1]);
                        break;
                }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            StringUtil.copyPartialMatches(strings[0], Arrays.asList(subCommands), suggestions);
        }
        if (strings.length == 2) {
            switch (strings[0]) {
                case "stats":
                    StringUtil.copyPartialMatches(
                            strings[1],
                            aresonSomnium.getServer().getOnlinePlayers().stream()
                                    .map(HumanEntity::getName)
                                    .collect(Collectors.toList()),
                            suggestions
                    );
                    break;
            }
        }
        return suggestions;
    }

    private void notEnoughArguments(CommandSender commandSender) {
        commandSender.sendMessage("Parametri non sufficienti");
    }

    private void handleStatsCommand(CommandSender commandSender, String playerName) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (Objects.nonNull(player)) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (Objects.nonNull(somniumPlayer)) {
                String toSend = somniumPlayer.getPlayerName() + "'s stats:\n" +
                        "   Seconds played: " + somniumPlayer.getSecondsPlayedTotal() + "\n" +
                        "   Wallet:\n" +
                        "      Basic coins: " + somniumPlayer.getWallet().getBasicCoins() + "\n" +
                        "      Charon coins: " + somniumPlayer.getWallet().getBasicCoins() + "\n" +
                        "      Forced coins: " + somniumPlayer.getWallet().getBasicCoins() + "\n";
                commandSender.sendMessage(toSend);
            } else {
                commandSender.sendMessage("Impossibile reperire il SomniumPlayer per " + playerName);
            }
        } else {
            commandSender.sendMessage("Il giocatore " + playerName + " non esiste");
        }
    }
}
