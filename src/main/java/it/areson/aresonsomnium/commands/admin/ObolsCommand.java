package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.Constants;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
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

    private final PluginCommand command;
    private final AresonSomnium aresonSomnium;
    private final String[] subCommands = new String[]{"generateObolShard", "convertShards"};


    public ObolsCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        command = this.aresonSomnium.getCommand("obols");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'obols' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 0:
            case 1:
                MessageUtils.notEnoughArguments(commandSender, command);
                break;
            case 2:
                if ("convertshards".equals(args[0].toLowerCase())) {
                    handleConvertShards(args[1]);
                }
                break;
            case 3:
                if ("generateobolshard".equals(args[0].toLowerCase())) {
                    handleGenerateObolShard(args[1], Integer.parseInt(args[2]));
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
            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(Wallet.generateObolShard(aresonSomnium, amount));
            if (!remaining.isEmpty()) {
                for (Integer integer : remaining.keySet()) {
                    player.getWorld().dropItem(player.getLocation(), remaining.get(integer));
                }
            }
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
                            player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("obols-give"));
                        } else {
                            player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("obols-not-enough"));
                        }
                    } else {
                        player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("obols-not-nugget"));
                    }
                } else {
                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("obols-not-nugget"));
                }
            } else {
                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("somniumplayer-not-found"));
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
}
