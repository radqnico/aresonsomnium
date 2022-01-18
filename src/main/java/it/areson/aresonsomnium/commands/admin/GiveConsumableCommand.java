package it.areson.aresonsomnium.commands.admin;

import it.areson.aresonlib.utils.DurationUtils;
import it.areson.aresonsomnium.AresonSomnium;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.areson.aresonsomnium.Constants.*;
import static net.md_5.bungee.api.ChatColor.*;

@SuppressWarnings("NullableProblems")
public class GiveConsumableCommand implements CommandExecutor, TabCompleter {

    private final AresonSomnium aresonSomnium;
    public HashMap<String, ItemStack> itemStacks;
    public final String multiplierIndexName = "multiplier";
    public final String bomb3IndexName = "bomb3";
    public final String repairAllIndexName = "repairAll";
    private final ArrayList<String> quantitySuggestions;
    private final ArrayList<String> multiplierSuggestions;
    private final ArrayList<String> durationSuggestions;

    public GiveConsumableCommand(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        quantitySuggestions = IntStream.rangeClosed(1, 64).boxed().map(Object::toString).collect(Collectors.toCollection(ArrayList::new));
        multiplierSuggestions = new ArrayList<>(Arrays.asList("100", "150", "200", "275"));
        durationSuggestions = new ArrayList<>(Arrays.asList("1d", "12h30m40s", "3h20m", "30m"));

        PluginCommand pluginCommand = aresonSomnium.getCommand("giveConsumable");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }

        initializeItemStacks();
    }

    private void initializeItemStacks() {
        itemStacks = new HashMap<>();

        itemStacks.put(multiplierIndexName, createConsumableItemStack(Material.CLOCK, "Moltiplicatore", new ArrayList<>(), MULTIPLIER_MODEL_DATA));
        itemStacks.put(bomb3IndexName, createConsumableItemStack(Material.TNT, "Bomb3", new ArrayList<>(), BOMB3_MODEL_DATA));
        itemStacks.put(repairAllIndexName, createConsumableItemStack(Material.PAPER, AQUA + "" + BOLD + "Pergamena della riparazione", new ArrayList<>(Arrays.asList(
                GRAY + "Consumando questo oggetto tutti",
                GRAY + "gli oggetti riparabili nel tuo inventario",
                GRAY + "verranno riparati"
        )), REPAIR_ALL_MODEL_DATA));
    }

    private ItemStack createConsumableItemStack(Material material, String displayName, ArrayList<String> lore, int modelValue) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.displayName(Component.text(displayName));

            List<Component> loreCorrectFormat = lore.parallelStream().map(Component::text).collect(Collectors.toList());
            loreCorrectFormat.add(Component.text(""));
            loreCorrectFormat.add(Component.text(GRAY + "Click destro per utilizzarlo"));
            itemMeta.lore(loreCorrectFormat);

            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.setCustomModelData(modelValue);
        }
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack alignMultiplierItemStack(ItemStack originalItem, int multiplier, Duration duration) {
        ItemStack finalItem = originalItem.clone();
        String visibleMultiplier = multiplier / 100.0 + "x";

        ItemMeta itemMeta = finalItem.getItemMeta();
        if (itemMeta != null) {
            String previousName;
            if (itemMeta.displayName() instanceof TextComponent textComponent) {
                previousName = textComponent.content();
            } else {
                previousName = "NOT_FOUND";
            }

            itemMeta.displayName(Component.text(AQUA + "" + BOLD + previousName + " " + visibleMultiplier));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(GRAY + "Moltiplicatore: " + GREEN + visibleMultiplier));
            lore.add(Component.text(GRAY + "Durata: " + GREEN + duration.toString().substring(2).toLowerCase()));
            List<Component> oldLore = itemMeta.lore();
            if (oldLore != null) {
                lore.addAll(oldLore);
            }

            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
            persistentDataContainer.set(aresonSomnium.multiplierValueNamespacedKey, PersistentDataType.DOUBLE, multiplier / 100.0);
            persistentDataContainer.set(aresonSomnium.multiplierDurationNamespacedKey, PersistentDataType.STRING, duration.toString());

            itemMeta.lore(lore);
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
                                Duration duration = Duration.ofMinutes(10);
                                if (arguments.length > 4) {
                                    duration = DurationUtils.getDurationFromString(arguments[4]);
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
                aresonSomnium.sendInfoMessage(commandSender, "Inserisci il nome del reward. Disponibili: " + itemStacks.keySet());
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
            suggestions = aresonSomnium.getServer().getOnlinePlayers()
                    .stream().map(HumanEntity::getName)
                    .filter(playerName -> playerName.startsWith(arguments[0]))
                    .collect(Collectors.toList());
        } else if (arguments.length == 2) {
            suggestions = itemStacks.keySet().stream().filter(key -> key.startsWith(arguments[1])).collect(Collectors.toList());
        } else if (arguments.length == 3) {
            suggestions = quantitySuggestions;
        } else if (arguments.length == 4 && arguments[1].equals(multiplierIndexName)) {
            suggestions = multiplierSuggestions;
        } else if (arguments.length == 5 && arguments[1].equals(multiplierIndexName)) {
            suggestions = durationSuggestions;
        }

        return suggestions;
    }

}
