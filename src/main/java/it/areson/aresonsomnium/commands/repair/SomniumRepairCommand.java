package it.areson.aresonsomnium.commands.repair;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Constants;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.file.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings("NullableProblems")
public class SomniumRepairCommand implements CommandExecutor {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;
    private final HashMap<String, LocalDateTime> singleRepairTimes;
    private final HashMap<String, LocalDateTime> fullRepairTimes;
    private final long singleFreeRepairDelay;
    private final long fullRepairDelay;
    private final Price singleRepairCoinsPrice;
    private final Price singleRepairObolsPrice;
    private final Price singleRepairGemsPrice;

    public SomniumRepairCommand(AresonSomnium aresonSomnium, String command) {
        this.aresonSomnium = aresonSomnium;
        messageManager = aresonSomnium.getMessageManager();
        singleRepairTimes = new HashMap<>();
        fullRepairTimes = new HashMap<>();
        singleFreeRepairDelay = aresonSomnium.getConfig().getLong("repair.single-delay-seconds");
        fullRepairDelay = aresonSomnium.getConfig().getLong("repair.full-delay-seconds");

        singleRepairCoinsPrice = new Price(aresonSomnium.getConfig().getInt("repair.cost.coins"), 0, 0);
        singleRepairObolsPrice = new Price(0, aresonSomnium.getConfig().getInt("repair.cost.obols"), 0);
        singleRepairGemsPrice = new Price(0, 0, aresonSomnium.getConfig().getInt("repair.cost.gems"));

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
                case Constants.SINGLE_FREE_REPAIR_COMMAND -> singleFreeRepair(player);
                case Constants.SINGLE_REPAIR_COMMAND -> {
                    if (arguments.length >= 2) {
                        try {
                            CoinType coinType = CoinType.valueOf(arguments[1].toUpperCase());
                            singleRepair(player, coinType);
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
                    fullRepair(player, ignoreLastRepairTime, ignorePermission);
                }
                default -> commandSender.sendMessage("Comando non mappato");
            }

        }
        return true;
    }

    public void singleFreeRepair(Player player) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (isDamageable(itemInMainHand)) {
            if (hasDamage(itemInMainHand)) {
                if (canSingleRepairByLastRepair(player)) {
                    singleRepairTimes.put(player.getName(), LocalDateTime.now());
                    eventuallyRepairItemStack(itemInMainHand);
                    messageManager.sendPlainMessage(player, "single-repair-success");
                } else {
                    messageManager.sendPlainMessage(player, "cannot-repair-yet");
                }
            } else {
                messageManager.sendPlainMessage(player, "nothing-to-repair");
            }
        } else {
            messageManager.sendPlainMessage(player, "repair-cant-repair");
        }
    }

    public void singleRepair(Player player, CoinType coinType) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (isDamageable(itemInMainHand)) {
            if (hasDamage(itemInMainHand)) {
                Price repairPrice = getSingleRepairPrice(coinType);
                SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
                if (somniumPlayer != null && somniumPlayer.canAfford(repairPrice)) {
                    boolean result = somniumPlayer.takePriceAmount(repairPrice);
                    if (result) {
                        singleRepairTimes.put(player.getName(), LocalDateTime.now());
                        eventuallyRepairItemStack(itemInMainHand);
                        messageManager.sendPlainMessage(player, "single-repair-success");
                    } else {
                        messageManager.sendPlainMessage(player, "generic-error");
                    }
                } else {
                    messageManager.sendPlainMessage(player, "repair-not-enough-coins");
                }
            } else {
                messageManager.sendPlainMessage(player, "nothing-to-repair");
            }
        } else {
            messageManager.sendPlainMessage(player, "repair-cant-repair");
        }
    }

    public Price getSingleRepairPrice(CoinType coinType) {
        switch (coinType) {
            case MONETE -> {
                return singleRepairCoinsPrice;
            }
            case GEMME -> {
                return singleRepairGemsPrice;
            }
            case OBOLI -> {
                return singleRepairObolsPrice;
            }
            default -> {
            }
        }
        return singleRepairCoinsPrice;
    }

    public void fullRepair(Player player, boolean ignoreLastRepairTime, boolean ignorePermission) {
        if (ignorePermission || player.hasPermission(Constants.FULL_REPAIR_PERMISSION)) {
            if (hasSomethingToRepair(player)) {
                if (ignoreLastRepairTime || canFullRepairByLastRepair(player)) {
                    fullRepairTimes.put(player.getName(), LocalDateTime.now());
                    Arrays.stream(player.getInventory().getContents()).parallel().forEach(this::eventuallyRepairItemStack);
                    messageManager.sendPlainMessage(player, "full-repair-success");
                } else {
                    messageManager.sendPlainMessage(player, "cannot-repair-yet");
                }
            } else {
                messageManager.sendPlainMessage(player, "nothing-to-repair");
            }
        } else {
            messageManager.sendPlainMessage(player, "no-permissions");
        }
    }

    public boolean hasSomethingToRepair(Player player) {
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (hasDamage(itemStack)) {
                return true;
            }
        }
        return false;
    }

    public boolean canSingleRepairByLastRepair(Player player) {
        String playerName = player.getName();

        if (singleRepairTimes.containsKey(playerName)) {
            LocalDateTime lastRepair = singleRepairTimes.get(playerName);
            return Duration.between(lastRepair, LocalDateTime.now()).getSeconds() >= singleFreeRepairDelay;
        } else {
            return true;
        }
    }

    public boolean canFullRepairByLastRepair(Player player) {
        String playerName = player.getName();

        if (fullRepairTimes.containsKey(playerName)) {
            LocalDateTime lastRepair = fullRepairTimes.get(playerName);
            return Duration.between(lastRepair, LocalDateTime.now()).getSeconds() >= fullRepairDelay;
        } else {
            return true;
        }
    }

    public boolean isDamageable(ItemStack itemStack) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof Damageable;
    }

    public boolean hasDamage(ItemStack itemStack) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof Damageable damageable && damageable.hasDamage();
    }

    public void eventuallyRepairItemStack(ItemStack itemStack) {
        if (isDamageable(itemStack)) {
            Damageable damageable = (Damageable) itemStack.getItemMeta();
            damageable.setDamage(0);
            itemStack.setItemMeta(damageable);
        }
    }

}
