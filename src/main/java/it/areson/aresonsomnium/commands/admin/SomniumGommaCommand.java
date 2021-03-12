package it.areson.aresonsomnium.commands.admin;

import com.destroystokyo.paper.block.TargetBlockInfo;
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

    public SomniumGommaCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        PluginCommand command = this.aresonSomnium.getCommand("gomma");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'gomma' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 0:
                MessageUtils.notEnoughArguments(commandSender, command);
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "setblock":
                        handleSetBlock(commandSender);
                        break;
                    case "additem":
                        handleAddItem(commandSender);
                        break;
                    case "testgive":
                        handleTestGive(commandSender);
                        break;
                    default:
                        commandSender.sendMessage(errorMessage("Funzione non trovata"));
                        break;
                }
                break;
            case 3:
                if ("givegomma".equals(args[0].toLowerCase())) {
                    try {
                        int amount = Integer.parseInt(args[2]);
                        String nick = args[1];
                        handleGiveGomma(nick, amount);
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("not-a-number"));
                    }
                } else {
                    commandSender.sendMessage(errorMessage("Funzione non trovata"));
                }
                break;
        }
        return true;
    }

    private void handleGiveGomma(String nick, int amount) {
        Player player = aresonSomnium.getServer().getPlayer(nick);
        if (player != null) {
            ItemStack gommaItem = new ItemStack(Material.valueOf(aresonSomnium.getMessageManager().getPlainMessageNoPrefix("gomma-material")));

            ItemMeta itemMeta = gommaItem.getItemMeta();
            if (Objects.nonNull(itemMeta)) {
                itemMeta.setDisplayName(aresonSomnium.getMessageManager().getPlainMessageNoPrefix("gomma-item-name"));

                String loreString = aresonSomnium.getMessageManager().getPlainMessageNoPrefix("gomma-item-lore");
                String[] split = loreString.split("\\n");
                ArrayList<String> lore = new ArrayList<>(Arrays.asList(split));
                itemMeta.setLore(lore);

                itemMeta.setCustomModelData(GOMMA_MODEL_DATA);
                itemMeta.addEnchant(Enchantment.DURABILITY, 2, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            gommaItem.setItemMeta(itemMeta);
            gommaItem.setAmount(amount);

            if (player.getInventory().addItem(gommaItem).isEmpty()) {
                player.sendMessage(MessageUtils.successMessage("Ti e' stata data una gomma"));
            } else {
                player.sendMessage(MessageUtils.errorMessage("Non hai abbastanza spazio"));
            }
        } else {
            aresonSomnium.getLogger().severe("Player non trovato");
        }
    }

    private void handleTestGive(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
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
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
            if (!Material.AIR.equals(itemInMainHand.getType())) {
                ItemStack itemStack = itemInMainHand.asOne();
                aresonSomnium.getGommaObjectsFileReader().storeItem(itemStack);
                commandSender.sendMessage(successMessage("Oggetto " + itemStack.getType().name() + " x" + itemStack.getAmount() + " aggiunto alla lista Gomma Gomma"));
            } else {
                commandSender.sendMessage(errorMessage("Non hai nulla in mano"));
            }

        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
    }

    private void handleSetBlock(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
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
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            StringUtil.copyPartialMatches(strings[0], Arrays.asList(subCommands), suggestions);
        }
        return suggestions;
    }
}
