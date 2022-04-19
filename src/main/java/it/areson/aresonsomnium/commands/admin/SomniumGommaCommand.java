package it.areson.aresonsomnium.commands.admin;

import com.destroystokyo.paper.block.TargetBlockInfo;
import it.areson.aresonlib.minecraft.files.MessageManager;
import it.areson.aresonlib.minecraft.utils.Substitution;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import java.util.*;

import static it.areson.aresonsomnium.Constants.GOMMA_MODEL_DATA;
import static it.areson.aresonsomnium.utils.MessageUtils.errorMessage;
import static it.areson.aresonsomnium.utils.MessageUtils.successMessage;


@SuppressWarnings("NullableProblems")
public class SomniumGommaCommand implements CommandExecutor, TabCompleter {

    private final String[] subCommands = new String[]{"setBlock", "addItem", "testGive", "giveGomma"};
    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;

    public SomniumGommaCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        this.messageManager = aresonSomnium.getMessageManager();
        PluginCommand command = this.aresonSomnium.getCommand("gomma");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'gomma' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] arguments) {
        switch (arguments.length) {
            case 0:
                MessageUtils.notEnoughArguments(commandSender, command);
                break;
            case 1:
                switch (arguments[0].toLowerCase()) {
                    case "setblock" -> handleSetBlock(commandSender);
                    case "additem" -> handleAddItem(commandSender);
                    case "testgive" -> handleTestGive(commandSender);
                    default -> commandSender.sendMessage(errorMessage("Funzione non trovata"));
                }
                break;
            case 3:
                if ("givegomma".equalsIgnoreCase(arguments[0])) {
                    try {
                        int amount = Integer.parseInt(arguments[2]);
                        String nick = arguments[1];
                        handleGiveGomma(nick, amount);
                    } catch (NumberFormatException e) {
                        messageManager.sendMessage(commandSender, "not-a-number");
                    }
                } else {
                    commandSender.sendMessage(errorMessage("Funzione non trovata"));
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void handleGiveGomma(String nick, int amount) {
        Player player = aresonSomnium.getServer().getPlayer(nick);
        if (player != null) {
            ItemStack gommaItem = new ItemStack(Material.valueOf(messageManager.getSimpleString("gomma-material")));

            ItemMeta itemMeta = gommaItem.getItemMeta();
            if (Objects.nonNull(itemMeta)) {
                itemMeta.displayName(messageManager.getMessageWithoutPrefix("gomma-item-name"));
                itemMeta.lore(messageManager.getMessageListWithoutPrefix("gomma-item-lore"));

                itemMeta.setCustomModelData(GOMMA_MODEL_DATA);
                itemMeta.addEnchant(Enchantment.DURABILITY, 2, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            gommaItem.setItemMeta(itemMeta);
            gommaItem.setAmount(amount);

            if (player.getInventory().addItem(gommaItem).isEmpty()) {
                messageManager.sendMessage(player, "gomma-give", new Substitution("%amount%", "" + amount));
            } else {
                player.sendMessage(MessageUtils.errorMessage("Non hai abbastanza spazio"));
            }
        } else {
            aresonSomnium.getLogger().severe("Player non trovato");
        }
    }

    private void handleTestGive(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            List<ItemStack> itemList = aresonSomnium.getGommaObjectsFileReader().getItemList();
            Collections.shuffle(itemList);
            ItemStack itemStack = itemList.get(new Random().nextInt(itemList.size()));
            if (player.getInventory().addItem(itemStack).isEmpty()) {
                player.sendMessage(MessageUtils.successMessage("Oggetto " + itemStack.getType().name() + " x" + itemStack.getAmount() + " ottenuto"));
            } else {
                player.sendMessage(MessageUtils.errorMessage("Non hai abbastanza spazio"));
            }
        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
    }

    private void handleAddItem(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
            if (!Material.AIR.equals(itemInMainHand.getType())) {
                aresonSomnium.getGommaObjectsFileReader().storeItem(itemInMainHand);
                commandSender.sendMessage(successMessage("Oggetto " + itemInMainHand.getType().name() + " x" + itemInMainHand.getAmount() + " aggiunto alla lista Gomma Gomma"));
            } else {
                commandSender.sendMessage(errorMessage("Non hai nulla in mano"));
            }

        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
    }

    private void handleSetBlock(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            Block targetBlock = player.getTargetBlock(100, TargetBlockInfo.FluidMode.NEVER);
            if (Objects.nonNull(targetBlock)) {
                Location location = targetBlock.getLocation();
                aresonSomnium.getGommaObjectsFileReader().setGommaBlock(location);
                commandSender.sendMessage(successMessage("Blocco Gomma Gomma impostato"));
            }
        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] arguments) {
        List<String> suggestions = new ArrayList<>();
        if (arguments.length == 1) {
            StringUtil.copyPartialMatches(arguments[0], Arrays.asList(subCommands), suggestions);
        }
        return suggestions;
    }
}
