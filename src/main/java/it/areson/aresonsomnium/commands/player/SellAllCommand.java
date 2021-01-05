package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import it.areson.aresonsomnium.shops.BlockPrice;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class SellAllCommand implements CommandExecutor, TabCompleter {

    private final AresonSomnium aresonSomnium;

    public SellAllCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        PluginCommand command = this.aresonSomnium.getCommand("sellall");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().severe("Errore durante l'inizializzazione del comando 'sellAll'");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            BigDecimal coinsToGive = Arrays.stream(player.getInventory().getContents()).parallel().reduce(BigDecimal.ZERO, (total, itemStack) -> {
                try {
                    if (itemStack != null) {
                        BigDecimal itemValue = BlockPrice.getPrice(itemStack.getType());
                        itemValue = itemValue.multiply(BigDecimal.valueOf(itemStack.getAmount()));

                        total = total.add(itemValue);
                        player.getInventory().remove(itemStack);
                    }

                } catch (MaterialNotSellableException ignored) {
                }
                return total;
            }, BigDecimal::add);

            Wallet.addBasicCoins(player, coinsToGive);

        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }

}
