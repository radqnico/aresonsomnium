package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.Pair;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static it.areson.aresonsomnium.Constants.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class RightClickListener extends GeneralEventListener {

    private final Optional<LuckPerms> luckPerms;

    public RightClickListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);

        RegisteredServiceProvider<LuckPerms> provider = aresonSomnium.getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = Optional.of(provider.getProvider());
        } else {
            luckPerms = Optional.empty();
        }
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

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (isLegitRightClick(event.getHand(), event.getAction(), event.useInteractedBlock())) {

            ItemStack itemStack = event.getItem();
            if (hasCustomItemRequirements(itemStack)) {

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
            }
        }
    }

    private Optional<Pair<Integer, Duration>> getMultiplierProperties(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            List<String> lore = itemMeta.getLore();
            if (lore != null && lore.size() >= 2) {
                try {
                    String stringMultiplier = lore.get(0);
                    stringMultiplier = stringMultiplier.substring(stringMultiplier.indexOf(' ') + 1, stringMultiplier.length() - 1);
                    int multiplier = (int) Double.parseDouble(stringMultiplier) * 100;

                    String stringDuration = lore.get(1);
                    stringDuration = "PT" + stringDuration.substring(stringDuration.indexOf(' ') + 1).toUpperCase();
                    Duration duration = Duration.parse(stringDuration);

                    return Optional.of(Pair.of(multiplier, duration));
                } catch (Exception exception) {
                    aresonSomnium.getLogger().severe("Error while parsing from lore of multiplier consumable item");
                    exception.printStackTrace();
                }
            }
        }

        return Optional.empty();
    }

    private void activateMultiplier(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();


        //TODO prevent activating lower multipliers
        if (itemStack != null) {
            if (luckPerms.isPresent()) {
                Optional<Pair<Integer, Duration>> optionalProperties = getMultiplierProperties(itemStack);
                if (optionalProperties.isPresent()) {
                    Pair<Integer, Duration> properties = optionalProperties.get();
                    String permission = SELL_MULTIPLIER_PERMISSION + "." + properties.left();


                    luckPerms.get().getUserManager().modifyUser(player.getUniqueId(), user -> {
                        Optional<Node> activeMultiplier = user.getNodes().parallelStream().filter(node -> node.getKey().equals(permission)).findFirst();
                        if(activeMultiplier.isPresent()) {

                            if(activeMultiplier.get().getExpiryDuration() != null) {
                                System.out.println(activeMultiplier.get().getExpiryDuration());
                                System.out.println(activeMultiplier.get().getExpiryDuration().plus(properties.right()));
                            }

                            activeMultiplier.get().toBuilder().expiry(activeMultiplier.get().getExpiryDuration().plus(properties.right())).build();
                        }

                        user.data().add(Node.builder(permission).expiry(properties.right()).build());
                    });

                    itemStack.setAmount(itemStack.getAmount() - 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1f, 1f);
                    aresonSomnium.sendSuccessMessage(player, "Hai attivato il moltiplicatore " + properties.left() / 100 + "x per " + properties.right().toString().substring(2).toLowerCase());
                    event.setCancelled(true);
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
