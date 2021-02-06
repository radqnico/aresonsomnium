package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonsomnium.AresonSomnium;
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
import java.util.List;


@SuppressWarnings("NullableProblems")
public class ConvertCharonCommand implements CommandExecutor, TabCompleter {

    private final PluginCommand command;
    private final AresonSomnium aresonSomnium;

    public ConvertCharonCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        command = this.aresonSomnium.getCommand("ConvertCharon");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'ConvertCharon' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 0:
                MessageUtils.notEnoughArguments(commandSender, command);
                break;
            case 1:
                handleConvertCharon(commandSender, args[0]);
                break;
            default:
                MessageUtils.tooManyArguments(commandSender, command);
        }
        return true;
    }

    private void handleConvertCharon(CommandSender commandSender, String playerName) {
        Player player = aresonSomnium.getServer().getPlayer(playerName);
        if (player != null) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
            if (somniumPlayer != null) {
                ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                if (Material.GOLD_NUGGET.equals(itemInMainHand.getType())) {
                    ItemMeta itemMeta = itemInMainHand.getItemMeta();
                    if (itemMeta != null && itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == Wallet.getCharonNuggetModelData()) {
                        if (itemInMainHand.getAmount() >= 10) {
                            itemInMainHand.setAmount(itemInMainHand.getAmount() - 10);
                            somniumPlayer.getWallet().changeCharonCoins(BigInteger.ONE);
                            player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("charon-coin-give"));
                        }
                    } else {
                        player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("charon-coin-not-nugget"));
                    }
                } else {
                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("charon-coin-not-nugget"));
                }
            } else {
                commandSender.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("somniumplayer-not-found"));
            }
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            StringUtil.copyPartialMatches(strings[0], aresonSomnium.getSomniumPlayerManager().getOnlinePlayersNames(), suggestions);
        } else if (strings.length == 2) {
            StringUtil.copyPartialMatches(strings[1], aresonSomnium.getShopManager().getGuis().keySet(), suggestions);
        }
        return suggestions;
    }
}
