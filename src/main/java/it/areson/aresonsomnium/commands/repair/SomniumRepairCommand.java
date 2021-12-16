package it.areson.aresonsomnium.commands.repair;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Constants;
import it.areson.aresonsomnium.economy.CoinType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

@SuppressWarnings("NullableProblems")
public class SomniumRepairCommand implements CommandExecutor {

    private final AresonSomnium aresonSomnium;


    public SomniumRepairCommand(AresonSomnium aresonSomnium, String command) {
        this.aresonSomnium = aresonSomnium;

        PluginCommand pluginCommand = this.aresonSomnium.getCommand(command);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando " + command + " non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        // somniumsinglerepair playerName coinType
        // somniumfullrepair playerName ignoreLastRepairTime ignorePermission
        if (commandSender.hasPermission("aresonsomnium.admin")) {

            String playerName;
            Player player;
            if (arguments.length >= 1) {
                playerName = arguments[0];
                player = aresonSomnium.getServer().getPlayer(playerName);
                if (player == null) {
                    commandSender.sendMessage("Player non trovato: " + playerName);
                    return true;
                }
            } else {
                commandSender.sendMessage("Inserisci il nome del giocatore");
                return true;
            }

            switch (command.getName().toLowerCase()) {
                case Constants.SINGLE_FREE_REPAIR_COMMAND -> aresonSomnium.singleFreeRepair(player);
                case Constants.SINGLE_REPAIR_COMMAND -> {
                    if (arguments.length >= 2) {
                        try {
                            CoinType coinType = CoinType.valueOf(arguments[1].toUpperCase());
                            aresonSomnium.singleRepair(player, coinType);
                        } catch (IllegalArgumentException illegalArgumentException) {
                            commandSender.sendMessage("Tipo di valuta non valida");
                            return true;
                        }
                    } else {
                        commandSender.sendMessage("Inserisci la valuta di pagamento");
                    }
                }
                case Constants.FULL_REPAIR_COMMAND -> {
                    boolean ignoreLastRepairTime = arguments.length >= 2 && Boolean.parseBoolean(arguments[1]);
                    boolean ignorePermission = arguments.length >= 3 && Boolean.parseBoolean(arguments[2]);
                    aresonSomnium.fullRepair(player, ignoreLastRepairTime, ignorePermission);
                }
                default -> commandSender.sendMessage("Comando non mappato");
            }

        }
        return true;
    }

}
