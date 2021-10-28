package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Recaps;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.elements.Pair;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.MessageUtils;
import it.areson.aresonsomnium.utils.file.MessageManager;
import org.bukkit.command.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@SuppressWarnings("NullableProblems")
public class SomniumAdminCommand implements CommandExecutor, TabCompleter {


    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;
    private final String[] subCommands = new String[]{"stats", "setCoins", "listPlayers", "deleteLastLoreLine", "addCoins", "removeCoins", "openRecap"};

    public SomniumAdminCommand(AresonSomnium plugin) {
        aresonSomnium = plugin;
        messageManager = plugin.getMessageManager();
        PluginCommand command = aresonSomnium.getCommand("SomniumAdmin");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            aresonSomnium.getLogger().warning("Comando 'somniumadmin' non dichiarato");
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
                    case "stats", "setcoins", "addcoins", "setdebuglevel" -> MessageUtils.notEnoughArguments(commandSender, command);
                    case "listplayers" -> handleListPlayers(commandSender);
                    case "deletelastloreline" -> handleDeleteLastLoreLine(commandSender);
                }
                break;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "stats" -> handleStatsCommand(commandSender, args[1]);
                    case "listplayers" -> MessageUtils.tooManyArguments(commandSender, command);
                    case "setcoins", "addcoins" -> MessageUtils.notEnoughArguments(commandSender, command);
                }
                break;
            case 3:
                switch (args[0].toLowerCase()) {
                    case "addcoins", "setcoins" -> MessageUtils.notEnoughArguments(commandSender, command);
                    case "stats", "listplayers" -> MessageUtils.tooManyArguments(commandSender, command);
                    case "openrecap" -> handleOpenRecap(args[1], args[2]);
                }
                break;
            case 4:
                switch (args[0].toLowerCase()) {
                    case "setcoins" -> handleSetCoins(commandSender, args[1], args[2], args[3]);
                    case "addcoins" -> handleAddRemoveCoins(commandSender, args[1], args[2], args[3], false);
                    case "removecoins" -> handleAddRemoveCoins(commandSender, args[1], args[2], args[3], true);
                    case "stats", "listplayers" -> MessageUtils.tooManyArguments(commandSender, command);
                }
                break;
        }
        return true;
    }

    private void handleOpenRecap(String playerName, String recap) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (Objects.nonNull(player)) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (Objects.nonNull(somniumPlayer)) {
                Recaps.openRecapToPlayer(player, Integer.parseInt(recap));
            } else {
                messageManager.sendPlainMessage(player, "somniumplayer-not-found", Pair.of("%player%", playerName));
            }
        } else {
            aresonSomnium.getLogger().severe("Player not found in handleOpenRecap: " + playerName);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            StringUtil.copyPartialMatches(strings[0], Arrays.asList(subCommands), suggestions);
        }
        if (strings.length == 2) {
            switch (strings[0].toLowerCase()) {
                case "stats", "setcoins", "addcoins", "removecoins" -> StringUtil.copyPartialMatches(
                        strings[1],
                        aresonSomnium.getServer().getOnlinePlayers().stream()
                                .map(HumanEntity::getName)
                                .collect(Collectors.toList()),
                        suggestions
                );
            }
        }
        if (strings.length == 3) {
            if ("setcoins".equalsIgnoreCase(strings[0]) || "addcoins".equalsIgnoreCase(strings[0]) || "removecoins".equalsIgnoreCase(strings[0])) {
                StringUtil.copyPartialMatches(
                        strings[2],
                        Arrays.stream(CoinType.values()).map(value -> value.name().toLowerCase()).collect(Collectors.toList()),
                        suggestions
                );
            }
        }
        return suggestions;
    }

    private void handleDeleteLastLoreLine(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
            ItemMeta itemMeta = itemInMainHand.getItemMeta();
            if (Objects.nonNull(itemMeta)) {
                //TODO Deprecated
                List<String> lore = itemMeta.getLore();
                if (Objects.nonNull(lore) && lore.size() > 0) {
                    lore.remove(lore.size() - 1);
                }
                itemMeta.setLore(lore);
            }
            itemInMainHand.setItemMeta(itemMeta);
        } else {
            messageManager.sendPlainMessage(commandSender, "player-only-command");
        }
    }

    private void handleStatsCommand(CommandSender commandSender, String playerName) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (Objects.nonNull(player)) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (Objects.nonNull(somniumPlayer)) {
                messageManager.sendPlainMessage(
                        commandSender,
                        "stats-format",
                        Pair.of("%player%", playerName),
                        Pair.of("%secondsPlayed%", somniumPlayer.getSecondsPlayedTotal() + ""),
                        Pair.of("%coins%", Wallet.getCoins(player).toString()),
                        Pair.of("%obols%", somniumPlayer.getWallet().getObols().toString()),
                        Pair.of("%gems%", somniumPlayer.getWallet().getGems().toString())
                );
            } else {
                messageManager.sendPlainMessage(player, "somniumplayer-not-found", Pair.of("%player%", playerName));
            }
        } else {
            aresonSomnium.getLogger().severe("Player not found in handleStatsCommand: " + playerName);
        }
    }

    private void handleAddRemoveCoins(CommandSender commandSender, String playerName, String coinType, String amountString, boolean removing) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (Objects.nonNull(player)) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (Objects.nonNull(somniumPlayer)) {
                try {
                    BigDecimal amount;
                    if (amountString.contains("-")) {
                        String[] split = amountString.split("-");
                        int min = Integer.parseInt(split[0]);
                        int max = Integer.parseInt(split[1]);
                        amount = BigDecimal.valueOf(new Random().nextInt(max - min + 1) + min);
                    } else {
                        amount = new BigDecimal(amountString);
                    }
                    amount = removing ? amount.negate() : amount;
                    CoinType type = CoinType.valueOf(coinType.toUpperCase());
                    switch (type) {
                        case OBOLI -> somniumPlayer.getWallet().changeObols(amount.toBigInteger());
                        case GEMME -> somniumPlayer.getWallet().changeGems(amount.toBigInteger());
                        case MONETE -> Wallet.addCoins(player, amount);
                        default -> messageManager.sendPlainMessage(player, "coins-type-error");
                    }
                    switch (type) {
                        case MONETE:
                        case GEMME:
                        case OBOLI:
                            if (removing) {
                                messageManager.sendPlainMessage(player, "coins-remove", Pair.of("%type%", type.getCoinName()), Pair.of("%amount%", amount.negate().toString()));
                            } else {
                                messageManager.sendPlainMessage(player, "coins-add", Pair.of("%type%", type.getCoinName()), Pair.of("%amount%", amount.toString()));
                            }
                            break;
                    }
                } catch (NumberFormatException exception) {
                    messageManager.sendPlainMessage(player, "not-a-number");
                }
            } else {
                messageManager.sendPlainMessage(player, "somniumplayer-not-found", Pair.of("%player%", playerName));
            }
        } else {
            messageManager.sendPlainMessage(commandSender, "player-not-found", Pair.of("%player%", playerName));
        }
    }

    private void handleSetCoins(CommandSender commandSender, String playerName, String coinType, String amountString) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (Objects.nonNull(player)) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (Objects.nonNull(somniumPlayer)) {
                try {
                    BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(amountString));
                    CoinType type = CoinType.valueOf(coinType.toUpperCase());
                    switch (type) {
                        case OBOLI -> {
                            somniumPlayer.getWallet().setObols(amount.toBigInteger());
                            messageManager.sendPlainMessage(player, "coins-set");
                        }
                        case GEMME -> {
                            somniumPlayer.getWallet().setGems(amount.toBigInteger());
                            messageManager.sendPlainMessage(player, "coins-set");
                        }
                        case MONETE -> {
                            Wallet.setCoins(player, amount);
                            messageManager.sendPlainMessage(player, "coins-set");
                        }
                        default -> messageManager.sendPlainMessage(player, "coins-type-error");
                    }
                } catch (NumberFormatException exception) {
                    messageManager.sendPlainMessage(player, "not-a-number");
                }
            } else {
                messageManager.sendPlainMessage(player, "somniumplayer-not-found", Pair.of("%player%", playerName));
            }
        } else {
            messageManager.sendPlainMessage(commandSender, "player-not-found", Pair.of("%player%", playerName));
        }
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
