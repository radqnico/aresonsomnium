package it.areson.aresonsomnium.commands.repair;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.elements.Pair;
import it.areson.aresonsomnium.players.SomniumPlayer;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@SuppressWarnings("NullableProblems")
public class SomniumRepairCommand implements CommandExecutor {

    private final RepairCountdown repairCountdown;
    private final AresonSomnium aresonSomnium;

    public SomniumRepairCommand(AresonSomnium aresonSomnium) {
        this.repairCountdown = new RepairCountdown();
        this.aresonSomnium = aresonSomnium;
        PluginCommand command = aresonSomnium.getCommand("somniumrepair");
        if (command != null) {
            command.setExecutor(this);
        } else {
            aresonSomnium.getLogger().warning("Comando 'somniumrepair' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        //somniumrepair playerName single/full coinType
        if (commandSender.hasPermission("aresonsomnium.admin")) {
            if (arguments.length == 3) {
                String playerName = arguments[0];
                String repairModality = arguments[1];
                try {
                    CoinType coinType = CoinType.valueOf(arguments[2]);

                    Player player = aresonSomnium.getServer().getPlayer(playerName);
                    if (player != null) {
                        switchActionCoins(player, coinType);
                    }
                } catch (IllegalArgumentException exception) {
                    commandSender.sendMessage("Tipo di valuta non valida");
                    return true;
                }
            } else {
                commandSender.sendMessage("Scrivi il nome del giocatore");
            }
        }
        return true;
    }

    public void switchActionCoins(Player player, CoinType coinType) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        switch (coinType) {
            case MONETE:
                repairSingleItemCoins(player, itemInMainHand);
                break;
            case GEMME:
                repairSingleItemGems(player, itemInMainHand);
                break;
            case OBOLI:
                break;
        }
    }

    private void repairSingleItemCoins(Player player, ItemStack itemStack) {
        SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
        if (somniumPlayer != null) {
            if (!Objects.equals(itemStack.getType(), Material.AIR)) {
                Pair<Boolean, String> booleanStringPair = repairCountdown.canRepair(player.getName());
                if (booleanStringPair.left()) {
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta instanceof Damageable damageable) {
                        if (damageable.getDamage() != 0) {
                            repairCountdown.setLastRepairTime(player.getName());
                            damageable.setDamage(0);
                            itemStack.setItemMeta(damageable);
                            Price repairPrice = getRepairPriceFromConfig(aresonSomnium, CoinType.MONETE);
                            if (somniumPlayer.takePriceAmount(repairPrice)) {
                                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-success"));
                            } else {
                                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-not-enough-coins"));
                            }
                        } else {
                            player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-no-damage"));
                        }
                    } else {
                        player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-cant-repair"));
                    }
                } else {
                    player.sendMessage(booleanStringPair.right());
                }
            }
        }
    }

    private void repairSingleItemGems(Player player, ItemStack itemStack) {
        SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
        if (somniumPlayer != null) {
            if (!Objects.equals(itemStack.getType(), Material.AIR)) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta instanceof Damageable damageable) {
                    if (damageable.getDamage() != 0) {
                        repairCountdown.setLastRepairTime(player.getName());
                        damageable.setDamage(0);
                        itemStack.setItemMeta(damageable);
                        Price repairPrice = getRepairPriceFromConfig(aresonSomnium, CoinType.GEMME);
                        if (somniumPlayer.takePriceAmount(repairPrice)) {
                            player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-success"));
                        } else {
                            player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-not-enough-coins"));
                        }
                    } else {
                        player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-no-damage"));
                    }
                } else {
                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-cant-repair"));
                }
            }
        }
    }

    private Price getRepairPriceFromConfig(JavaPlugin plugin, CoinType coinType) {
        FileConfiguration config = plugin.getConfig();
        int costCoins = config.getInt("repair.cost.coins");
        int costObols = config.getInt("repair.cost.obols");
        int costGems = config.getInt("repair.cost.gems");
        return switch (coinType) {
            case MONETE -> new Price(costCoins, 0, 0);
            case GEMME -> new Price(0, 0, costGems);
            case OBOLI -> new Price(0, costObols, 0);
        };
    }

}
