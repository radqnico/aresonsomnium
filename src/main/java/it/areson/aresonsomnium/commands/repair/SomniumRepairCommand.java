package it.areson.aresonsomnium.commands.repair;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.elements.Pair;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SomniumRepairCommand implements CommandExecutor {

    private RepairCountdown repairCountdown;

    public SomniumRepairCommand() {
        this.repairCountdown = new RepairCountdown();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] arguments) {
        if (arguments.length == 1) {
            String playerName = arguments[0];
            Player player = AresonSomniumAPI.instance.getServer().getPlayer(playerName);
            if (player != null) {
                ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                if (!Objects.equals(itemInMainHand.getType(), Material.AIR)) {
                    Pair<Boolean, String> booleanStringPair = repairCountdown.canRepair(playerName);
                    if (booleanStringPair.left()) {
                        repairCountdown.setLastRepairTime(playerName);
                    } else {
                        player.sendMessage(booleanStringPair.right());
                    }
                }
            }
        } else {
            commandSender.sendMessage("Scrivi il nome del giocatore");
        }
        return true;
    }

}
