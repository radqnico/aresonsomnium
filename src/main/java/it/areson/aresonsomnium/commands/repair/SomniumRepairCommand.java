package it.areson.aresonsomnium.commands.repair;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.elements.Pair;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
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
            System.out.println("Permesso");
            if (arguments.length == 1) {
                System.out.println("Length");
                String playerName = arguments[0];
                Player player = aresonSomnium.getServer().getPlayer(playerName);
                if (player != null) {
                    System.out.println("player");
                    ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                    if (!Objects.equals(itemInMainHand.getType(), Material.AIR)) {
                        System.out.println("item");
                        Pair<Boolean, String> booleanStringPair = repairCountdown.canRepair(playerName);
                        if (booleanStringPair.left()) {
                            repairCountdown.setLastRepairTime(playerName);
                            ItemMeta itemMeta = itemInMainHand.getItemMeta();
                            if (itemMeta instanceof Damageable) {
                                Damageable damageable = (Damageable) itemMeta;
                                if (damageable.getDamage() != 0) {
                                    damageable.setDamage(0);
                                    itemInMainHand.setItemMeta((ItemMeta) damageable);
                                }else{
                                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repaired-no-damage"));
                                }
                                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repaired-success"));
                            } else {
                                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repaired-cant-repair"));
                            }
                        } else {
                            player.sendMessage(booleanStringPair.right());
                        }
                    }
                }
            } else {
                commandSender.sendMessage("Scrivi il nome del giocatore");
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
