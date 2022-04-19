package it.areson.aresonsomnium.listeners;

import it.areson.aresonlib.minecraft.files.MessageManager;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.elements.Pair;
import net.luckperms.api.model.data.TemporaryNodeMergeStrategy;
import net.luckperms.api.node.Node;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static it.areson.aresonsomnium.Constants.*;

@SuppressWarnings("FieldCanBeLocal")
public class RightClickListener extends GeneralEventListener {

    private final MessageManager messageManager;
    private final HashMap<String, Instant> playerDelays;
    private final int delaySeconds = 2;

    public RightClickListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
        this.messageManager = aresonSomnium.getMessageManager();

        playerDelays = new HashMap<>();
    }

    private boolean isLegitRightClick(EquipmentSlot equipmentSlot, Action action, Event.Result useClickedBlock) {
        return Objects.equals(equipmentSlot, EquipmentSlot.HAND) &&
                (
                        (action.equals(Action.RIGHT_CLICK_BLOCK) && useClickedBlock.equals(Event.Result.ALLOW)) || action.equals(Action.RIGHT_CLICK_AIR)
                );
    }

    private boolean hasCustomItemRequirements(ItemStack itemStack) {
        return !Objects.isNull(itemStack) && !Objects.isNull(itemStack.getItemMeta()) && itemStack.getItemMeta().hasCustomModelData();
    }

    private boolean canUseAnotherConsumable(Player player) {
        String playerName = player.getName();
        return !playerDelays.containsKey(playerName) || Duration.between(playerDelays.get(playerName), Instant.now()).getSeconds() >= delaySeconds;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (isLegitRightClick(event.getHand(), event.getAction(), event.useInteractedBlock())) {

            ItemStack itemStack = event.getItem();
            if (hasCustomItemRequirements(itemStack)) {
                Player player = event.getPlayer();

                if (canUseAnotherConsumable(player)) {

                    switch (Objects.requireNonNull(Objects.requireNonNull(itemStack).getItemMeta()).getCustomModelData()) {
                        case GOMMA_MODEL_DATA -> {
                            playerDelays.put(player.getName(), Instant.now());
                            collectGommaReward(event);
                        }
                        case MULTIPLIER_MODEL_DATA -> {
                            playerDelays.put(player.getName(), Instant.now());
                            activateMultiplier(event);
                        }
                        case REPAIR_ALL_MODEL_DATA -> {
                            playerDelays.put(player.getName(), Instant.now());
                            repairAll(event);
                        }
                        default -> {
                        }
                    }
                } else {
                    aresonSomnium.sendInfoMessage(player, "Devi aspettare qualche secondo prima di poterlo riutilizzare");
                }
            }
        }
    }

    private Optional<Pair<Double, Duration>> getMultiplierProperties(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            try {
                PersistentDataContainer persistentDataContainer = itemStack.getItemMeta().getPersistentDataContainer();
                Double multiplier = persistentDataContainer.get(aresonSomnium.getMultiplierValueNamespacedKey(), PersistentDataType.DOUBLE);

                String duration = persistentDataContainer.getOrDefault(aresonSomnium.getMultiplierDurationNamespacedKey(), PersistentDataType.STRING, "PT10M");
                Duration parsedDuration = Duration.parse(duration);

                return Optional.of(Pair.of(multiplier, parsedDuration));
            } catch (Exception exception) {
                aresonSomnium.getLogger().severe("Error while parsing multiplier of consumable item");
                exception.printStackTrace();
            }
        }

        return Optional.empty();
    }

    private void activateMultiplier(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (itemStack != null) {
            if (aresonSomnium.getLuckPerms().isPresent()) {
                Optional<Pair<Double, Duration>> optionalProperties = getMultiplierProperties(itemStack);

                if (optionalProperties.isPresent()) {
                    Pair<Double, Duration> properties = optionalProperties.get();

                    if (properties.left() >= aresonSomnium.getCachedMultiplier(player).value()) {
                        String permission = PERMISSION_MULTIPLIER + "." + (int) (properties.left() * 100);
                        aresonSomnium.getLuckPerms().get().getUserManager().modifyUser(player.getUniqueId(), user ->
                                user.data().add(Node.builder(permission).expiry(properties.right()).build(), TemporaryNodeMergeStrategy.ADD_NEW_DURATION_TO_EXISTING)
                        );

                        itemStack.setAmount(itemStack.getAmount() - 1);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1f, 1f);
                        aresonSomnium.sendSuccessMessage(player, "Hai attivato il moltiplicatore " + properties.left() + "x per " + properties.right().toString().substring(2).toLowerCase());
                        event.setCancelled(true);
                    } else {
                        aresonSomnium.sendErrorMessage(player, "Hai già un moltiplicatore maggiore attivo");
                    }
                }
            } else {
                aresonSomnium.getLogger().severe("Errore nell'API di LuckPerms");
            }
        }
    }

    private void collectGommaReward(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null) {
            Location clickedBlockLocation = clickedBlock.getLocation();
            Location gommaBlockLocation = aresonSomnium.getGommaObjectsFileReader().getGommaBlock();

            if (clickedBlockLocation.equals(gommaBlockLocation)) {
                PlayerInventory playerInventory = player.getInventory();
                ItemStack itemInMainHand = playerInventory.getItemInMainHand();

                if (playerInventory.addItem(aresonSomnium.getGommaObjectsFileReader().getRandomItem()).isEmpty()) {
                    itemInMainHand.setAmount(itemInMainHand.getAmount() - 1);

                    messageManager.sendMessage(player, "gomma-item-give");
                } else {
                    messageManager.sendMessage(player, "gomma-error-give");
                }
                event.setCancelled(true);
            }
        }
    }

    public void repairAll(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();

        if (itemStack != null) {
            if (aresonSomnium.fullRepair(event.getPlayer(), false, true)) {
                itemStack.setAmount(itemStack.getAmount() - 1);
            }
        }
    }

}
