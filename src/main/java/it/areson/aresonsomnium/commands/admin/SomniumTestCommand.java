package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.shops.guis.CustomShop;
import it.areson.aresonsomnium.shops.guis.ShopEditor;
import it.areson.aresonsomnium.shops.guis.ShopManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static it.areson.aresonsomnium.utils.MessageUtils.*;


@SuppressWarnings("NullableProblems")
public class SomniumTestCommand implements CommandExecutor, TabCompleter {

    private final String[] subCommands = new String[]{"serialize", "deserialize", "openPermanentGui", "openPricesGui"};
    private final AresonSomnium aresonSomnium;

    public SomniumTestCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        PluginCommand command = this.aresonSomnium.getCommand("somniumtest");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            this.aresonSomnium.getLogger().warning("Comando 'somniumtest' non dichiarato");
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 0:
                notEnoughArguments(commandSender, command);
                break;
            case 1:
                switch (args[0].toLowerCase()) {
                    case "serialize":
                    case "deserialize":
                    case "openpricesgui":
                        handleOpenPricesGui(commandSender);
                        break;
                    case "openpermanentgui":
                        notEnoughArguments(commandSender, command);
                        break;
                    default:
                        commandSender.sendMessage(errorMessage("Funzione non trovata"));
                }
                break;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "serialize":
                        itemStackSerializationHandler(commandSender, args[1]);
                        break;
                    case "deserialize":
                        itemStackDeserializationHandler(commandSender, args[1]);
                        break;
                    case "openpermanentgui":
                        openPermanentGuiHandler(commandSender, args[1]);
                        break;
                    default:
                        commandSender.sendMessage(errorMessage("Funzione non trovata"));
                }
                break;
        }
        return true;
    }

    private void handleOpenPricesGui(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.openInventory(aresonSomnium.getShopEditor().getPricesInventory());
        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
    }

    private void openPermanentGuiHandler(CommandSender commandSender, String guiName) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            ShopManager shopManager = aresonSomnium.getShopManager();
            if (shopManager.isPermanent(guiName)) {
                CustomShop permanentGui = shopManager.getPermanentGui(guiName);
                player.openInventory(permanentGui.createInventory());
            } else {
                player.sendMessage("La GUI richiesta non è una GUI salvata");
            }
        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
    }

    private void itemStackDeserializationHandler(CommandSender commandSender, String path) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            byte[] bytes = aresonSomnium.getDataFile().readBytes(path);
            ItemStack itemStack = ItemStack.deserializeBytes(bytes);
            HashMap<Integer, ItemStack> ignore = player.getInventory().addItem(itemStack);
            if (!ignore.isEmpty()) {
                player.sendMessage(warningMessage("Non hai spazio nell'inventario"));
            }
        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
    }

    private void itemStackSerializationHandler(CommandSender commandSender, String path) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
            try {
                byte[] bytes = itemInMainHand.serializeAsBytes();
                player.sendMessage(successMessage("Serializzazione di '" + path + "' completata"));
                aresonSomnium.getDataFile().writeBytes(path, bytes);
            } catch (IllegalArgumentException exception) {
                player.sendMessage(errorMessage("Errore: " + exception.getMessage()));
            }
        } else {
            commandSender.sendMessage(errorMessage("Comando disponibile solo da Player"));
        }
        commandSender.sendMessage();
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
