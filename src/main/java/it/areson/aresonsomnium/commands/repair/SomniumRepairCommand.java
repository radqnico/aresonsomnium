package it.areson.aresonsomnium.commands.repair;

import it.areson.aresonsomnium.AresonSomnium;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class SomniumRepairCommand implements CommandExecutor, TabCompleter {

    private final RepairCountdown repairCountdown;
    private final AresonSomnium aresonSomnium;

    public SomniumRepairCommand(AresonSomnium aresonSomnium) {
        this.repairCountdown = new RepairCountdown();
        this.aresonSomnium = aresonSomnium;
        PluginCommand command = aresonSomnium.getCommand("somniumrepair");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            aresonSomnium.getLogger().warning("Comando 'somniumrepair' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments) {
        if (commandSender.hasPermission("aresonsomnium.admin")) {
            if (arguments.length == 1) {
                String playerName = arguments[0];
                Player player = aresonSomnium.getServer().getPlayer(playerName);
                if (player != null) {
                    ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                    repairSingleItem(player, itemInMainHand);
                }
            } else {
                commandSender.sendMessage("Scrivi il nome del giocatore");
            }
        }
        return true;
    }

    private void repairSingleItem(Player player, ItemStack itemStack) {
        SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
        if (somniumPlayer != null) {
            if (!Objects.equals(itemStack.getType(), Material.AIR)) {
                Pair<Boolean, String> booleanStringPair = repairCountdown.canRepair(player.getName());
                if (booleanStringPair.left()) {
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta instanceof Damageable) {
                        Damageable damageable = (Damageable) itemMeta;
                        if (damageable.getDamage() != 0) {
                            repairCountdown.setLastRepairTime(player.getName());
                            damageable.setDamage(0);
                            itemStack.setItemMeta((ItemMeta) damageable);
                            Price repairPrice = getRepairPriceFromConfig(aresonSomnium);
                            if (somniumPlayer.takePriceAmount(repairPrice)) {
                                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-success"));
                            } else {
                                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-not-enough-coins"));
                            }
                        } else {
                            player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repaired-no-damage"));
                        }
                    } else {
                        player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repaired-cant-repair"));
                    }
                } else {
                    player.sendMessage(booleanStringPair.right());
                }
            }
        }
    }

    private Price getRepairPriceFromConfig(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        int costCoins = config.getInt("repair.cost.coins");
        int costObols = config.getInt("repair.cost.obols");
        int costGems = config.getInt("repair.cost.gems");
        return new Price(costCoins, costObols, costGems);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
