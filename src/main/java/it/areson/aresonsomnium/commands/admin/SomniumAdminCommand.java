package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.shops.guis.CustomShop;
import it.areson.aresonsomnium.shops.guis.ShopEditor;
import it.areson.aresonsomnium.shops.guis.ShopManager;
import it.areson.aresonsomnium.utils.Debugger;
import it.areson.aresonsomnium.utils.MessageManager;
import it.areson.aresonsomnium.utils.MessageUtils;
import it.areson.aresonsomnium.utils.Pair;
import org.bukkit.command.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@SuppressWarnings("NullableProblems")
public class SomniumAdminCommand implements CommandExecutor, TabCompleter {


    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;
    private final String[] subCommands = new String[]{"stats", "setCoins", "listPlayers", "createShop", "editShop", "reloadShops", "setDebugLevel", "deleteLastLoreLine"};

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
                    case "stats":
                    case "setcoins":
                    case "createshop":
                    case "editshop":
                    case "setdebuglevel":
                        MessageUtils.notEnoughArguments(commandSender, command);
                        break;
                    case "listplayers":
                        handleListPlayers(commandSender);
                        break;
                    case "reloadshops":
                        handleReloadShops(commandSender);
                        break;
                    case "deletelastloreline":
                        handleDeleteLastLoreLine(commandSender);
                        break;
                }
                break;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "stats":
                        handleStatsCommand(commandSender, args[1]);
                        break;
                    case "editshop":
                        handleEditShop(commandSender, args[1]);
                        break;
                    case "listplayers":
                        MessageUtils.tooManyArguments(commandSender, command);
                        break;
                    case "setdebuglevel":
                        handleSetDebugLevel(commandSender, args[1]);
                        break;
                    case "setcoins":
                    case "createshop":
                        MessageUtils.notEnoughArguments(commandSender, command);
                        break;
                }
                break;
            case 3:
                switch (args[0].toLowerCase()) {
                    case "setcoins":
                        MessageUtils.notEnoughArguments(commandSender, command);
                        break;
                    case "createshop":
                        handleCreateShop(commandSender, args[1], args[2].replaceAll("_", " "));
                        break;
                    case "setdebuglevel":
                    case "stats":
                    case "listplayers":
                    case "editshop":
                        MessageUtils.tooManyArguments(commandSender, command);
                        break;
                }
                break;
            case 4:
                switch (args[0].toLowerCase()) {
                    case "setcoins":
                        handleSetCoins(commandSender, args[1], args[2], args[3]);
                        break;
                    case "stats":
                    case "listplayers":
                    case "createshop":
                    case "editshop":
                        MessageUtils.tooManyArguments(commandSender, command);
                        break;
                }
                break;
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
                case "setcoins":
                    StringUtil.copyPartialMatches(
                            strings[1],
                            aresonSomnium.getServer().getOnlinePlayers().stream()
                                    .map(HumanEntity::getName)
                                    .collect(Collectors.toList()),
                            suggestions
                    );
                    break;
                case "editshop":
                    StringUtil.copyPartialMatches(
                            strings[1],
                            aresonSomnium.getShopManager().getGuis().keySet(),
                            suggestions
                    );
                    break;
                case "setdebuglevel":
                    StringUtil.copyPartialMatches(
                            strings[1],
                            Arrays.stream(Debugger.DebugLevel.values()).map(Enum::name).collect(Collectors.toList()),
                            suggestions
                    );
                    break;
            }
        }
        if (strings.length == 3) {
            if ("setcoins".equalsIgnoreCase(strings[0])) {
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
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
            ItemMeta itemMeta = itemInMainHand.getItemMeta();
            if (Objects.nonNull(itemMeta)) {
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

    private void handleSetDebugLevel(CommandSender commandSender, String level) {
        Debugger.DebugLevel debugLevel = Debugger.DebugLevel.valueOf(level);
        switch (debugLevel) {
            case LOW:
            case HIGH:
                aresonSomnium.getDebugger().setDebugLevel(debugLevel);
                break;
            default:
                messageManager.sendPlainMessage(commandSender, "invalid-debug-level");
                break;
        }
    }

    private void handleReloadShops(CommandSender commandSender) {
        aresonSomnium.getShopManager().fetchAllFromDB();
        messageManager.sendPlainMessage(commandSender, "guis-reloaded");
    }

    private void handleEditShop(CommandSender commandSender, String guiName) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            ShopManager shopManager = aresonSomnium.getShopManager();
            ShopEditor shopEditor = aresonSomnium.getShopEditor();
            if (shopManager.isPermanent(guiName)) {
                CustomShop permanentGui = shopManager.getPermanentGui(guiName);
                player.openInventory(permanentGui.createInventory());
                if (shopEditor.isEditingCustomGui(player) || shopEditor.isEditingPrice(player)) {
                    shopEditor.endEditGui(player);
                    shopEditor.endEditPrice(player);
                }
                shopEditor.beginEditGui(player, guiName);
            } else {
                messageManager.sendPlainMessage(player, "guis-reloaded");
            }
        } else {
            messageManager.sendPlainMessage(commandSender, "player-only-command");
        }
    }

    private void handleCreateShop(CommandSender commandSender, String guiName, String guiTitle) {
        ShopManager shopManager = aresonSomnium.getShopManager();
        ShopEditor shopEditor = aresonSomnium.getShopEditor();
        CustomShop newGui = shopManager.createNewGui(guiName, guiTitle);
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.openInventory(newGui.createInventory());
            if (shopEditor.isEditingCustomGui(player) || shopEditor.isEditingPrice(player)) {
                shopEditor.endEditGui(player);
                shopEditor.endEditPrice(player);
            }
            shopEditor.beginEditGui(player, guiName);
        }
    }

    private void handleStatsCommand(CommandSender commandSender, String playerName) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (Objects.nonNull(player)) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (Objects.nonNull(somniumPlayer)) {
                messageManager.sendPlainMessage(
                        player,
                        "stats-format",
                        Pair.of("%player%", playerName),
                        Pair.of("%secondsPlayed%", somniumPlayer.getSecondsPlayedTotal() + ""),
                        Pair.of("%coins%", Wallet.getCoins(player).toPlainString()),
                        Pair.of("%obols%", somniumPlayer.getWallet().getObols().toString()),
                        Pair.of("%gems%", somniumPlayer.getWallet().getGems().toString())
                );
            } else {
                messageManager.sendPlainMessage(player, "somniumplayer-not-found", Pair.of("%player%", playerName));
            }
        } else {
            messageManager.sendPlainMessage(player, "player-not-found", Pair.of("%player%", playerName));
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
                        case OBOLI:
                            somniumPlayer.getWallet().setObols(amount.toBigInteger());
                            messageManager.sendPlainMessage(player, "coins-set");
                            break;
                        case GEMME:
                            somniumPlayer.getWallet().setGems(amount.toBigInteger());
                            messageManager.sendPlainMessage(player, "coins-set");
                            break;
                        case MONETE:
                            Wallet.setCoins(player, amount);
                            messageManager.sendPlainMessage(player, "coins-set");
                            break;
                        default:
                            messageManager.sendPlainMessage(player, "coins-set");
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
