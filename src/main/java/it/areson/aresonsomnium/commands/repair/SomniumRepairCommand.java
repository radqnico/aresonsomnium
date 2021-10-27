package it.areson.aresonsomnium.commands.repair;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Constants;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.elements.Pair;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.file.MessageManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

@SuppressWarnings("NullableProblems")
public class SomniumRepairCommand implements CommandExecutor {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;
    private final RepairCountdown singleRepairCountdown;
    private final HashMap<String, LocalDateTime> fullRepairTimes;
    private final long fullRepairDelay;

    public SomniumRepairCommand(AresonSomnium aresonSomnium, String command) {
        this.aresonSomnium = aresonSomnium;
        messageManager = aresonSomnium.getMessageManager();
        singleRepairCountdown = new RepairCountdown();
        fullRepairTimes = new HashMap<>();
        fullRepairDelay = aresonSomnium.getConfig().getLong("repair.full-delay-seconds");

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
        // somniumfullrepair playerName ignoreLastRepairTime
        if (commandSender.hasPermission("aresonsomnium.admin")) {
            if (arguments.length >= 1) {
                String playerName = arguments[0];
                Player player = aresonSomnium.getServer().getPlayer(playerName);
                if (player != null) {
                    switch (command.getName().toLowerCase()) {
                        case Constants.SINGLE_REPAIR_COMMAND -> {
                            if (arguments.length >= 2) {
                                try {
                                    CoinType coinType = CoinType.valueOf(arguments[1].toUpperCase());
                                    switchActionCoins(player, coinType);
                                } catch (IllegalArgumentException exception) {
                                    commandSender.sendMessage("Tipo di valuta non valida");
                                    return true;
                                }
                            } else {
                                commandSender.sendMessage("Inserisci la valuta di pagamento");
                            }
                        }
                        case Constants.FULL_REPAIR_COMMAND -> {
                            boolean ignoreLastRepairTime;
                            if (arguments.length >= 2) {
                                ignoreLastRepairTime = Boolean.parseBoolean(arguments[1]);
                            } else {
                                ignoreLastRepairTime = false;
                            }
                            fullRepair(player, ignoreLastRepairTime);
                        }
                        default -> commandSender.sendMessage("Comando non mappato");
                    }
                } else {
                    commandSender.sendMessage("Player non trovato: " + playerName);
                }
            } else {
                commandSender.sendMessage("Inserisci il nome del giocatore");
            }
        }
        return true;
    }

    public void fullRepair(Player player, boolean ignoreLastRepairTime) {
        if (player.hasPermission(Constants.FULL_REPAIR_PERMISSION)) {
            if (ignoreLastRepairTime || canFullRepairByLastRepair(player)) {
                fullRepairTimes.put(player.getName(), LocalDateTime.now());
                Arrays.stream(player.getInventory().getContents()).parallel().forEach(this::eventuallyRepairItemStack);
                messageManager.sendPlainMessage(player, "repair-success");
            } else {
                messageManager.sendPlainMessage(player, "cannot-repair-yet");
            }
        } else {
            messageManager.sendPlainMessage(player, "no-permissions");
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

    public void eventuallyRepairItemStack(ItemStack itemStack) {
        if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(0);
            itemStack.setItemMeta(damageable);
        }
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
                Pair<Boolean, String> booleanStringPair = singleRepairCountdown.canRepair(player.getName());
                if (booleanStringPair.left()) {
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta instanceof Damageable damageable) {
                        if (damageable.getDamage() != 0) {
                            singleRepairCountdown.setLastRepairTime(player.getName());
                            Price repairPrice = getRepairPriceFromConfig(CoinType.MONETE);
                            if (somniumPlayer.takePriceAmount(repairPrice)) {
                                damageable.setDamage(0);
                                itemStack.setItemMeta(damageable);
                                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-success"));
                            } else {
                                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-not-enough-coins"));
                            }
                        } else {
                            player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-no-damage"));
                        }
                    } else {
                        player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("repair-cant-repair-time-left"));
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
                        singleRepairCountdown.setLastRepairTime(player.getName());
                        Price repairPrice = getRepairPriceFromConfig(CoinType.GEMME);
                        if (somniumPlayer.takePriceAmount(repairPrice)) {
                            damageable.setDamage(0);
                            itemStack.setItemMeta(damageable);
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

    private Price getRepairPriceFromConfig(CoinType coinType) {
        FileConfiguration config = aresonSomnium.getConfig();
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
