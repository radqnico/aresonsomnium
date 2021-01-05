package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.exceptions.MaterialNotSellableException;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.shops.BlockPrice;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

public class SellAllCommand implements CommandExecutor, TabCompleter {

    private final PluginCommand command;
    private final AresonSomnium aresonSomnium;

    private BigDecimal price;

    public SellAllCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        command = this.aresonSomnium.getCommand("sellall");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'sellAll' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            SomniumPlayer somniumPlayer = (SomniumPlayer) commandSender;
            Player player = somniumPlayer.getPlayer();

            for (ItemStack item : player.getInventory().getContents()) {
                try {
                    price = BlockPrice.getPrice(item.getType());
                } catch (MaterialNotSellableException e) {
                    e.printStackTrace();
                }
                BigDecimal amount = BigDecimal.valueOf(item.getAmount());
                player.getInventory().remove(item);
                Wallet.addBasicCoins(player, price.multiply(amount));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
