package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import it.areson.aresonsomnium.shops.BlockPrice;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
            double multiplier = 1.0;

            //Getting multiplier
            Optional<PermissionAttachmentInfo> optionalMultiplierPermission = player.getEffectivePermissions().stream().parallel()
                    .filter(permission -> permission.getPermission().startsWith(aresonSomnium.MULTIPLIER_PERMISSION))
                    .findFirst();

            if (optionalMultiplierPermission.isPresent()) {
                String permission = optionalMultiplierPermission.get().getPermission();
                int lastDotPosition = permission.lastIndexOf(".");
                String stringMultiplier = permission.substring(lastDotPosition);

                try {
                    double value = Double.parseDouble(stringMultiplier);
                    multiplier = value / 100;
                } catch (NumberFormatException event) {
                    aresonSomnium.getLogger().severe("Error while parsing string multiplier to double: " + stringMultiplier);
                }
            }

            //Getting amount
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

            coinsToGive = coinsToGive.multiply(BigDecimal.valueOf(multiplier));
            Wallet.addBasicCoins(player, coinsToGive);

        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }

}
