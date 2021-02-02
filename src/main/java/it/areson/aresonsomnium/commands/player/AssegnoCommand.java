package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.shops.items.Price;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class AssegnoCommand implements CommandExecutor, TabCompleter {

    private final AresonSomnium aresonSomnium;

    public AssegnoCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        PluginCommand command = this.aresonSomnium.getCommand("assegno");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'assegno' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
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
                        commandSender.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("not-a-number"));
                    }
                } else {
                    commandSender.sendMessage(MessageUtils.errorMessage("Riscontrato un problema con i tuoi dati. Segnala il problema  allo staff."));
                }
            } else {
                MessageUtils.notEnoughArguments(commandSender, command);
            }
        } else {
            commandSender.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("player-only-command"));
        }
        return true;
    }

    private void createNewCheck(SomniumPlayer somniumPlayer, BigDecimal amount, CoinType type) {
        switch (type) {
            case CHARON:
                if (somniumPlayer.canAfford(new Price(BigDecimal.ZERO, amount.toBigInteger(), BigInteger.ZERO))) {
                    ItemStack itemStack = Wallet.generateCheck(amount.doubleValue(), type);
                    if (!somniumPlayer.getPlayer().getInventory().addItem(itemStack).isEmpty()) {
                        somniumPlayer.getPlayer().sendMessage(aresonSomnium.getMessageManager().getPlainMessage("item-buy-not-enough-space"));
                    }
                }
                somniumPlayer.getPlayer().sendMessage(aresonSomnium.getMessageManager().getPlainMessage("check-created"));
                break;
            case FORCED:
                if (somniumPlayer.canAfford(new Price(BigDecimal.ZERO, BigInteger.ZERO, amount.toBigInteger()))) {
                    ItemStack itemStack = Wallet.generateCheck(amount.doubleValue(), type);
                    if (!somniumPlayer.getPlayer().getInventory().addItem(itemStack).isEmpty()) {
                        somniumPlayer.getPlayer().sendMessage(aresonSomnium.getMessageManager().getPlainMessage("item-buy-not-enough-space"));
                    }
                }
                somniumPlayer.getPlayer().sendMessage(aresonSomnium.getMessageManager().getPlainMessage("check-created"));
                break;
            case BASIC:
                if (somniumPlayer.canAfford(new Price(amount, BigInteger.ZERO, BigInteger.ZERO))) {
                    ItemStack itemStack = Wallet.generateCheck(amount.doubleValue(), type);
                    if (!somniumPlayer.getPlayer().getInventory().addItem(itemStack).isEmpty()) {
                        somniumPlayer.getPlayer().sendMessage(aresonSomnium.getMessageManager().getPlainMessage("item-buy-not-enough-space"));
                    }
                }
                somniumPlayer.getPlayer().sendMessage(aresonSomnium.getMessageManager().getPlainMessage("check-created"));
                break;
            default:
                somniumPlayer.getPlayer().sendMessage(aresonSomnium.getMessageManager().getPlainMessage("coins-type-error"));
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
