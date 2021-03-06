package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonlib.minecraft.files.MessageManager;
import it.areson.aresonlib.minecraft.utils.Substitution;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Constants;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static it.areson.aresonsomnium.Constants.OBOL_MODEL_DATA;


@SuppressWarnings("NullableProblems")
public class ObolsCommand implements CommandExecutor, TabCompleter {

    private final AresonSomnium aresonSomnium;
    private final MessageManager messageManager;
    private final String[] subCommands = new String[]{"generateObolShard", "convertShards"};
    private final ItemStack shardShape;


    public ObolsCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        this.messageManager = aresonSomnium.getMessageManager();
        this.shardShape = createShardShape();

        PluginCommand command = this.aresonSomnium.getCommand("obols");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'obols' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        switch (arguments.length) {
            case 0:
            case 1:
                MessageUtils.notEnoughArguments(commandSender, command);
                break;
            case 2:
                if ("convertshards".equalsIgnoreCase(arguments[0])) {
                    handleConvertShards(arguments[1]);
                }
                break;
            case 3:
                if ("generateobolshard".equalsIgnoreCase(arguments[0])) {
                    handleGenerateObolShard(arguments[1], Integer.parseInt(arguments[2]));
                }
                break;
            default:
                MessageUtils.tooManyArguments(commandSender, command);
                break;
        }
        return true;
    }

    private void handleGenerateObolShard(String playerName, int amount) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (player != null) {
            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(shardShape.clone().asQuantity(amount));
            if (!remaining.isEmpty()) {
                for (Integer integer : remaining.keySet()) {
                    player.getWorld().dropItem(player.getLocation(), remaining.get(integer));
                }
            }
            messageManager.sendMessage(player, "obols-give-shard", new Substitution("%amount%", amount + ""));
        }
    }

    private void handleConvertShards(String playerName) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (player != null) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (somniumPlayer != null) {
                ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                if (Material.GOLD_NUGGET.equals(itemInMainHand.getType())) {
                    ItemMeta itemMeta = itemInMainHand.getItemMeta();
                    if (itemMeta != null && itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == OBOL_MODEL_DATA) {
                        if (itemInMainHand.getAmount() >= Constants.OBOLS_CHANGE_AMOUNT) {
                            itemInMainHand.setAmount(itemInMainHand.getAmount() - Constants.OBOLS_CHANGE_AMOUNT);
                            somniumPlayer.getWallet().changeObols(BigInteger.ONE);
                            messageManager.sendMessage(player, "obols-give");
                        } else {
                            messageManager.sendMessage(player, "obols-not-enough");
                        }
                    } else {
                        messageManager.sendMessage(player, "obols-not-nugget");
                    }
                } else {
                    messageManager.sendMessage(player, "obols-not-nugget");
                }
            } else {
                messageManager.sendMessage(player, "somniumplayer-not-found");
            }
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            StringUtil.copyPartialMatches(strings[0], Arrays.asList(subCommands.clone()), suggestions);
        } else if (strings.length == 2) {
            StringUtil.copyPartialMatches(strings[1], aresonSomnium.getSomniumPlayerManager().getOnlinePlayersNames(), suggestions);
        }
        return suggestions;
    }


    private ItemStack createShardShape() {
        ItemStack itemStack = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.displayName(messageManager.getMessageWithoutPrefix("obolshard-item-name"));
            itemMeta.lore(messageManager.getMessageListWithoutPrefix("obolshard-item-lore"));

            itemMeta.setCustomModelData(OBOL_MODEL_DATA);
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
