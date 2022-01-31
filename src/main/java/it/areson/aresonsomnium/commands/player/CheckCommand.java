package it.areson.aresonsomnium.commands.player;

import it.areson.aresonlib.files.MessageManager;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import org.bukkit.command.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class CheckCommand implements CommandExecutor, TabCompleter {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;

    public CheckCommand(AresonSomnium aresonSomnium, MessageManager messageManager) {
        this.aresonSomnium = aresonSomnium;
        this.messageManager = messageManager;
        PluginCommand command = this.aresonSomnium.getCommand("assegno");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'assegno' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        commandSender.sendMessage("Funzione non ancora abilitata.");
        return true;

        /*
        if (commandSender instanceof Player) {
            if (strings.length == 2) {
                Player player = (Player) commandSender;
                SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
                if (Objects.nonNull(somniumPlayer)) {
                    try {
                        BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(strings[1]));
                        CoinType type = CoinType.valueOf(strings[0].toUpperCase());
                        createNewCheck(somniumPlayer, amount, type);
                    } catch (NumberFormatException exception) {
                        commandSender.sendMessage(messageManager.getPlainMessage("not-a-number"));
                    }
                } else {
                    commandSender.sendMessage(MessageUtils.errorMessage("Riscontrato un problema con i tuoi dati. Segnala il problema  allo staff."));
                }
            } else {
                MessageUtils.notEnoughArguments(commandSender, command);
            }
        } else {
            commandSender.sendMessage(messageManager.getPlainMessage("player-only-command"));
        }
        return true;
        */
    }

    private void createNewCheck(SomniumPlayer somniumPlayer, BigDecimal amount, CoinType type) {
        switch (type) {
            case OBOLI:
                if (somniumPlayer.canAfford(new Price(BigDecimal.ZERO, amount.toBigInteger(), BigInteger.ZERO))) {
                    ItemStack itemStack = Wallet.generateCheck(amount, type);
                    if (!somniumPlayer.getPlayer().getInventory().addItem(itemStack).isEmpty()) {
                        messageManager.sendMessage(somniumPlayer.getPlayer(), "item-buy-not-enough-space");
                    } else {
                        messageManager.sendMessage(somniumPlayer.getPlayer(), "check-created");
                        somniumPlayer.getWallet().changeObols(amount.toBigInteger().negate());
                    }
                } else {
                    messageManager.sendMessage(somniumPlayer.getPlayer(), "item-buy-not-enough-money");
                }
                break;
            case GEMME:
                if (somniumPlayer.canAfford(new Price(BigDecimal.ZERO, BigInteger.ZERO, amount.toBigInteger()))) {
                    ItemStack itemStack = Wallet.generateCheck(amount, type);
                    if (!somniumPlayer.getPlayer().getInventory().addItem(itemStack).isEmpty()) {
                        messageManager.sendMessage(somniumPlayer.getPlayer(), "item-buy-not-enough-space");
                    } else {
                        messageManager.sendMessage(somniumPlayer.getPlayer(), "check-created");
                        somniumPlayer.getWallet().changeGems(amount.toBigInteger().negate());
                    }
                } else {
                    messageManager.sendMessage(somniumPlayer.getPlayer(), "item-buy-not-enough-money");
                }
                break;
            case MONETE:
                if (somniumPlayer.canAfford(new Price(amount, BigInteger.ZERO, BigInteger.ZERO))) {
                    ItemStack itemStack = Wallet.generateCheck(amount, type);
                    if (!somniumPlayer.getPlayer().getInventory().addItem(itemStack).isEmpty()) {
                        messageManager.sendMessage(somniumPlayer.getPlayer(), "item-buy-not-enough-space");
                    } else {
                        messageManager.sendMessage(somniumPlayer.getPlayer(), "check-created");
                        Wallet.addCoins(somniumPlayer.getPlayer(), amount.negate());
                    }
                } else {
                    messageManager.sendMessage(somniumPlayer.getPlayer(), "item-buy-not-enough-money");
                }
                break;
            default:
                messageManager.sendMessage(somniumPlayer.getPlayer(), "coins-type-error");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            StringUtil.copyPartialMatches(
                    strings[0],
                    Arrays.stream(CoinType.values()).map(value -> value.name().toLowerCase()).collect(Collectors.toList()),
                    suggestions
            );
        }
        return suggestions;
    }
}
