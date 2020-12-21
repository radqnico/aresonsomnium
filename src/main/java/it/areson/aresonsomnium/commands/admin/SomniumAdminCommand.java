package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.entities.SomniumPlayer;
import org.bukkit.ChatColor;
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

    private final PluginCommand command;
    private final String[] subCommands = new String[]{"stats", "setCoins"};
    private final String[] coinTypes = new String[]{"basic", "charon", "forced"};
    private AresonSomnium aresonSomnium;

    public SomniumAdminCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        command = this.aresonSomnium.getCommand("SomniumAdmin");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
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
                switch (args[0].toLowerCase()) {
                    case "stats":
                        notEnoughArguments(commandSender);
                        break;
                    case "chengecoins":
                        notEnoughArguments(commandSender);
                        break;
                }
                break;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "stats":
                        handleStatsCommand(commandSender, args[1]);
                        break;
                    case "setCoins":
                        notEnoughArguments(commandSender);
                        break;
                }
            case 3:
                switch (args[0].toLowerCase()) {
                    case "stats":
                        tooManyArguments(commandSender);
                        break;
                    case "setcoins":
                        notEnoughArguments(commandSender);
                        break;
                }
            case 4:
                switch (args[0].toLowerCase()) {
                    case "stats":
                        tooManyArguments(commandSender);
                        break;
                    case "setcoins":
                        handleChengeCoins(commandSender, args[1], args[2], args[3]);
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
            switch (strings[0].toLowerCase()) {
                case "stats":
                    StringUtil.copyPartialMatches(
                            strings[1],
                            aresonSomnium.getServer().getOnlinePlayers().stream()
                                    .map(HumanEntity::getName)
                                    .collect(Collectors.toList()),
                            suggestions
                    );
                    break;
                case "setcoins":
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
        if (strings.length==3){
            switch (strings[0].toLowerCase()) {
                case "setcoins":
                    StringUtil.copyPartialMatches(
                            strings[2],
                            Arrays.asList(coinTypes),
                            suggestions
                    );
                    break;
            }
        }
        return suggestions;
    }

    private void notEnoughArguments(CommandSender commandSender) {
        commandSender.sendMessage("Parametri non sufficienti");
        commandSender.sendMessage(command.getUsage());
    }

    private void tooManyArguments(CommandSender commandSender) {
        commandSender.sendMessage("Troppi parametri forniti");
        commandSender.sendMessage(command.getUsage());
    }

    private void handleStatsCommand(CommandSender commandSender, String playerName) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (Objects.nonNull(player)) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (Objects.nonNull(somniumPlayer)) {
                String toSend = ChatColor.GOLD + somniumPlayer.getPlayerName() + ChatColor.RESET + "'s stats:\n" +
                        "   Secondi giocati: " + somniumPlayer.getSecondsPlayedTotal() + "\n" +
                        "   Wallet:\n" +
                        "      Basic coins: " + somniumPlayer.getWallet().getBasicCoins() + "\n" +
                        "      Charon coins: " + somniumPlayer.getWallet().getCharonCoins() + "\n" +
                        "      Forced coins: " + somniumPlayer.getWallet().getForcedCoins() + "\n";
                commandSender.sendMessage(toSend);
            } else {
                commandSender.sendMessage(ChatColor.RED + "Impossibile reperire il SomniumPlayer per " + playerName);
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "Il giocatore " + playerName + " non esiste");
        }
    }

    private void handleChengeCoins(CommandSender commandSender, String playerName, String coinType, String amountString) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (Objects.nonNull(player)) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (Objects.nonNull(somniumPlayer)) {
                try {
                    int amount = Integer.parseInt(amountString);
                    switch (coinType) {
                        case "basic":
                            somniumPlayer.getWallet().setBasicCoins(amount);
                            commandSender.sendMessage(ChatColor.GREEN + "Valore dei Basic Coins impostato");
                            break;
                        case "charon":
                            somniumPlayer.getWallet().setCharonCoins(amount);
                            commandSender.sendMessage(ChatColor.GREEN + "Valore dei Charon Coins impostato");
                            break;
                        case "force":
                            somniumPlayer.getWallet().setForcedCoins(amount);
                            commandSender.sendMessage(ChatColor.GREEN + "Valore dei Forced Coins impostato");
                            break;
                        default:
                            commandSender.sendMessage(ChatColor.RED + "Tipo di moneta non esistente");
                    }
                } catch (NumberFormatException exception) {
                    commandSender.sendMessage(ChatColor.RED + "Numero non valido");
                }
            } else {
                commandSender.sendMessage(ChatColor.RED + "Impossibile reperire il SomniumPlayer per " + playerName);
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "Il giocatore " + playerName + " non esiste");
        }
    }
}
