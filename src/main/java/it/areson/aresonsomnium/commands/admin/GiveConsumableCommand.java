package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.areson.aresonsomnium.Constants.MULTIPLIER_MODEL_DATA;
import static net.md_5.bungee.api.ChatColor.*;

@SuppressWarnings("NullableProblems")
public class GiveConsumableCommand implements CommandExecutor, TabCompleter {

    private final AresonSomnium aresonSomnium;
    public HashMap<String, ItemStack> itemStacks;
    public final String multiplierIndexName = "multiplier";

    public GiveConsumableCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;

        PluginCommand pluginCommand = aresonSomnium.getCommand("giveConsumable");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }

        initializeItemStacks();
    }

    private void initializeItemStacks() {
        itemStacks = new HashMap<>();

        itemStacks.put(multiplierIndexName, createConsumableItemStack(Material.CLOCK, "Moltiplicatore", new ArrayList<>(), MULTIPLIER_MODEL_DATA));
    }

    @SuppressWarnings("SameParameterValue")
    private ItemStack createConsumableItemStack(Material material, String displayName, ArrayList<String> lore, int modelValue) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(displayName);

            lore.add("");
            lore.add(GRAY + "Click destro per utilizzarlo");
            itemMeta.setLore(lore);

            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.setCustomModelData(modelValue);
        }
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack alignMultiplierItemStack(ItemStack originalItem, int multiplier, String duration) {
        ItemStack finalItem = originalItem.clone();
        String visibleMultiplier = (double) multiplier / 100 + "x";

        ItemMeta itemMeta = finalItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(AQUA + "" + BOLD + itemMeta.getDisplayName() + " " + visibleMultiplier);

            ArrayList<String> lore = new ArrayList<>();
            lore.add(GRAY + "Moltiplicatore: " + GREEN + visibleMultiplier);
            lore.add(GRAY + "Durata: " + GREEN + duration);
            lore.addAll(itemMeta.getLore());

            itemMeta.setLore(lore);
        }
        finalItem.setItemMeta(itemMeta);

        return finalItem;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        if (commandSender.isOp()) {
            if (arguments.length >= 2) {
                // Getting quantity
                int quantity = 1;
                if (arguments.length > 2) {
                    try {
                        quantity = Integer.parseInt(arguments[2]);
                    } catch (NumberFormatException e) {
                        aresonSomnium.sendErrorMessage(commandSender, arguments[2] + " non è una quantità valida. Imposto a 1");
                    }
                }

                String playerName = arguments[0];
                Player targetPlayer = aresonSomnium.getServer().getPlayerExact(playerName);

                if (!Objects.isNull(targetPlayer) && targetPlayer.isOnline()) {

                    String rewardName = arguments[1];
                    if (itemStacks.containsKey(rewardName)) {
                        try {
                            ItemStack reward = itemStacks.get(rewardName);

                            if (rewardName.equals(multiplierIndexName)) {
                                // Getting Multiplier
                                int multiplier = 100;
                                if (arguments.length > 3) {
                                    try {
                                        multiplier = Integer.parseInt(arguments[3]);
                                    } catch (NumberFormatException e) {
                                        aresonSomnium.sendErrorMessage(commandSender, arguments[3] + " non è una quantità valida. Imposto a 1");
                                    }
                                }

                                // Getting Duration
                                String duration = "10m";
                                if (arguments.length > 4) {
                                    duration = arguments[4];
                                    Period.parse("P" + duration);
                                }

                                reward = alignMultiplierItemStack(reward, multiplier, duration);
                            }

                            for (int i = 0; i < quantity; i++) {
                                targetPlayer.getInventory().addItem(reward);
                            }
                            aresonSomnium.sendSuccessMessage(commandSender, "Consumabile '" + rewardName + "' generato con successo");
                        } catch (DateTimeParseException exception) {
                            aresonSomnium.sendErrorMessage(commandSender, "Durata del consumabile non valida");
                            exception.printStackTrace();
                        }
                    } else {
                        aresonSomnium.sendErrorMessage(commandSender, "Non ho trovato alcun reward chiamato " + rewardName);
                    }
                } else {
                    aresonSomnium.sendErrorMessage(commandSender, "Non ho trovato alcun player di nome " + playerName);
                }
            } else if (arguments.length >= 1) {
                aresonSomnium.sendInfoMessage(commandSender, "Inserisci il nome del reward. Disponibili: " + itemStacks.keySet().toString());
            } else {
                aresonSomnium.sendInfoMessage(commandSender, "Utilizzo del comando: /giveConsumable nomePlayer nomeOggetto");
            }
        } else {
            aresonSomnium.sendErrorMessage(commandSender, "Non hai il permesso di farlo");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String cmd, String[] arguments) {
        List<String> suggestions = new ArrayList<>();

        if (arguments.length == 1) {
            List<String> playerNames = aresonSomnium.getServer().getOnlinePlayers()
                    .stream().map(HumanEntity::getName)
                    .filter(playerName -> playerName.startsWith(arguments[0]))
                    .collect(Collectors.toList());
            suggestions.addAll(playerNames);
        } else if (arguments.length == 2) {
            List<String> rewards = itemStacks.keySet().stream().filter(key -> key.startsWith(arguments[1])).collect(Collectors.toList());
            suggestions.addAll(rewards);
        } else if (arguments.length == 3) {
            List<String> quantities = IntStream.rangeClosed(1, 64).boxed().map(Object::toString).collect(Collectors.toList());
            suggestions.addAll(quantities);
        } else if (arguments.length == 4 && arguments[1].equals(multiplierIndexName)) {
            List<String> values = Arrays.asList("100", "150", "200", "275");
            suggestions.addAll(values);
        } else if (arguments.length == 5 && arguments[1].equals(multiplierIndexName)) {
            List<String> values = Arrays.asList("1d", "1d12h30m", "3h20m", "30m");
            suggestions.addAll(values);
        }

        return suggestions;
    }

}
