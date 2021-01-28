package it.areson.aresonsomnium.commands.player;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Constants;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class SellCommand implements CommandExecutor, TabCompleter {

    private final PluginCommand pluginCommand;
    private final AresonSomnium aresonSomnium;
    private final HashMap<Material, String> blocksPermission;

    public SellCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        pluginCommand = this.aresonSomnium.getCommand("sell");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'sell' non dichiarato");
        }

        blocksPermission = new HashMap<>();
        blocksPermission.put(Material.COBBLESTONE, Constants.permissionSelva);
        blocksPermission.put(Material.NETHERRACK, Constants.permissionAntinferno);
        blocksPermission.put(Material.COAL_BLOCK, Constants.permissionSecondoGirone);
        blocksPermission.put(Material.RED_NETHER_BRICKS, Constants.permissionQuartoGirone);
        blocksPermission.put(Material.MAGMA_BLOCK, Constants.permissionSestoGirone);
        blocksPermission.put(Material.RED_CONCRETE, Constants.permissionOttavoGirone);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            commandSender.sendMessage("Fiko");
        } else {
            commandSender.sendMessage("Comando eseguibile solo da giocatore");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }

}
