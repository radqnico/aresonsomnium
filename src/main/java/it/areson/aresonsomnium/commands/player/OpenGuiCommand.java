package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.shops.CustomShop;
import it.areson.aresonsomnium.shops.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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

    private void handleOpenGui(CommandSender commandSender, String guiName) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            ShopManager shopManager = aresonSomnium.getShopManager();
            if (shopManager.isShop(guiName)) {
                shopManager.openGuiToPlayer(player, guiName);
            } else {
                player.sendMessage("La GUI richiesta non esiste");
            }
        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
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

    private void notEnoughArguments(CommandSender commandSender) {
        commandSender.sendMessage(errorMessage("Parametri non sufficienti"));
        commandSender.sendMessage(command.getUsage());
    }

    private void tooManyArguments(CommandSender commandSender, String function) {
        commandSender.sendMessage(errorMessage("Troppi parametri forniti a " + function));
        commandSender.sendMessage(command.getUsage());
    }

    private void handleReloadGuis(CommandSender commandSender) {
        aresonSomnium.getShopManager().fetchAllFromDB();
        commandSender.sendMessage(successMessage("Tutte le GUI ricaricate dal DB"));
    }

    private void handleEditGui(CommandSender commandSender, String guiName) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            ShopManager shopManager = aresonSomnium.getShopManager();
            if (shopManager.isShop(guiName)) {
                CustomShop permanentGui = shopManager.getShop(guiName);
                player.openInventory(permanentGui.createInventory());
                shopManager.beginEditGui(player, guiName);
            } else {
                player.sendMessage("La GUI richiesta non è una GUI salvata");
            }
        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
    }

    private void handleCreateGui(CommandSender commandSender, String guiName, String guiTitle) {
        ShopManager shopManager = aresonSomnium.getShopManager();
        CustomShop newGui = shopManager.createNewGui(guiName, guiTitle);
        String message;
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.openInventory(newGui.createInventory());
            shopManager.beginEditGui(player, guiName);
            message = "GUI '" + guiName + "' creata e aperta al giocatore '" + player.getName() + "'";
        } else {
            message = "GUI '" + guiName + "' creata";
        }
        commandSender.sendMessage(successMessage(message));
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
                        "      Forced coins: " + somniumPlayer.getWallet().getForcedCoins();
                commandSender.sendMessage(toSend);
            } else {
                commandSender.sendMessage(errorMessage("Impossibile reperire il SomniumPlayer per " + playerName));
            }
        } else {
            commandSender.sendMessage(errorMessage("Il giocatore " + playerName + " non esiste"));
        }
    }

    private void handleSetCoins(CommandSender commandSender, String playerName, String coinType, String amountString) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (Objects.nonNull(player)) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (Objects.nonNull(somniumPlayer)) {
                try {
                    int amount = Integer.parseInt(amountString);
                    CoinType type = CoinType.valueOf(coinType.toUpperCase());
                    switch (type) {
                        case BASIC:
                            somniumPlayer.getWallet().setBasicCoins(amount);
                            commandSender.sendMessage(successMessage("Valore dei Basic Coins impostato"));
                            break;
                        case CHARON:
                            somniumPlayer.getWallet().setCharonCoins(amount);
                            commandSender.sendMessage(successMessage("Valore dei Charon Coins impostato"));
                            break;
                        case FORCED:
                            somniumPlayer.getWallet().setForcedCoins(amount);
                            commandSender.sendMessage(successMessage("Valore dei Forced Coins impostato"));
                            break;
                        default:
                            commandSender.sendMessage(errorMessage("Tipo di moneta non esistente"));
                    }
                } catch (NumberFormatException exception) {
                    commandSender.sendMessage(errorMessage("Numero non valido"));
                }
            } else {
                commandSender.sendMessage(errorMessage("Impossibile reperire il SomniumPlayer per " + playerName));
            }
        } else {
            commandSender.sendMessage(errorMessage("Il giocatore " + playerName + " non esiste"));
        }
    }

    private String successMessage(String message) {
        return ChatColor.GREEN + message;
    }

    private String warningMessage(String message) {
        return ChatColor.YELLOW + message;
    }

    private String errorMessage(String message) {
        return ChatColor.RED + message;
    }

    private void handleListPlayers(CommandSender commandSender) {
        StringBuilder message = new StringBuilder("Giocatori online: ");
        List<String> onlinePlayersNames = aresonSomnium.getSomniumPlayerManager().getOnlinePlayersNames();
        for (String playerName : onlinePlayersNames) {
            message.append(playerName).append(" ");
        }
        commandSender.sendMessage(message.toString());
    }
}
