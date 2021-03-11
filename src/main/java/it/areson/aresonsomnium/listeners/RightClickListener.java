package it.areson.aresonsomnium.listeners;

import elements.Pair;
import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import net.luckperms.api.model.data.TemporaryNodeMergeStrategy;
import net.luckperms.api.node.Node;
import org.bukkit.Location;
import org.bukkit.Material;
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

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static it.areson.aresonsomnium.Constants.*;
import static net.md_5.bungee.api.ChatColor.COLOR_CHAR;

@SuppressWarnings("FieldCanBeLocal")
public class RightClickListener extends GeneralEventListener {

    private final HashMap<String, Instant> playerDelays;
    private final int delaySeconds = 0;

    public RightClickListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);

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
                    playerDelays.put(player.getName(), Instant.now());

                    switch (Objects.requireNonNull(Objects.requireNonNull(itemStack).getItemMeta()).getCustomModelData()) {
                        case GOMMA_MODEL_DATA:
                            collectGommaReward(event);
                            break;
                        case CHECK_MODEL_DATA:
                            redeemCheck(event);
                            break;
                        case MULTIPLIER_MODEL_DATA:
                            activateMultiplier(event);
                            break;
                    }
                } else {
                    aresonSomnium.sendInfoMessage(player, "Devi aspettare qualche secondo prima di poterlo riutilizzare");
                }
            }
        }
    }

    private Optional<Pair<Double, Duration>> getMultiplierProperties(ItemStack itemStack) {
        List<String> lore = itemStack.getLore();

        if (lore != null && lore.size() >= 2) {
            try {
                String stringMultiplier = lore.get(0);
                stringMultiplier = stringMultiplier.replaceAll(COLOR_CHAR + ".", "");
                stringMultiplier = stringMultiplier.substring(stringMultiplier.indexOf(" ") + 1, stringMultiplier.length() - 1);
                double multiplier = Double.parseDouble(stringMultiplier);

                String stringDuration = lore.get(1);
                // Remove colors
                stringDuration = stringDuration.replaceAll(COLOR_CHAR + ".", "");
                // Take from the space
                stringDuration = stringDuration.substring(stringDuration.indexOf(" ") + 1);
                Duration duration = Duration.parse("PT" + stringDuration);

                return Optional.of(Pair.of(multiplier, duration));
            } catch (Exception exception) {
                aresonSomnium.getLogger().severe("Error while parsing from lore of multiplier consumable item");
                exception.printStackTrace();
            }
        }

        return Optional.empty();
    }

    private void activateMultiplier(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (itemStack != null) {
            if (aresonSomnium.luckPerms.isPresent()) {
                Optional<Pair<Double, Duration>> optionalProperties = getMultiplierProperties(itemStack);

                if (optionalProperties.isPresent()) {
                    Pair<Double, Duration> properties = optionalProperties.get();

                    if (properties.left() >= aresonSomnium.getCachedMultiplier(player).getValue()) {
                        String permission = PERMISSION_MULTIPLIER + "." + (int) (properties.left() * 100);

                        aresonSomnium.luckPerms.get().getUserManager().modifyUser(player.getUniqueId(), user ->
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
                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("gomma-item-give"));
                } else {
                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("gomma-error-give"));
                }
                event.setCancelled(true);
            }
        }
    }

    private void redeemCheck(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();

        if (itemStack != null && Objects.equals(itemStack.getType(), Material.PAPER)) {
            SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(event.getPlayer());

            if (somniumPlayer != null) {
                if (Wallet.applyCheck(somniumPlayer, itemStack)) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                    event.getPlayer().sendMessage(aresonSomnium.getMessageManager().getPlainMessage("check-applied"));
                    event.setCancelled(true);
                }
            } else {
                aresonSomnium.getDebugger().debugError(aresonSomnium.getMessageManager().getPlainMessage("somniumplayer-not-found"));
            }
        }
    }

}
