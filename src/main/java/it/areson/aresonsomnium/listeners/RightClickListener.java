package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.time.Duration;
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

    public void addPermission(User user, String permission) {

    }

    private void activateMultiplier(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("Debug");
        luckPerms.ifPresent(lp -> {


            User user = lp.getPlayerAdapter(Player.class).getUser(player);
            // Add the permission
            Duration parse = Duration.parse("1h30m");

            System.out.println(parse.toString());
            System.out.println(parse.getSeconds());
            user.data().add(Node.builder("pezzoDiMerda").expiry(parse).build());
            // Now we need to save changes.
            lp.getUserManager().saveUser(user);
        });
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
                }
            } else {
                aresonSomnium.getDebugger().debugError(aresonSomnium.getMessageManager().getPlainMessage("somniumplayer-not-found"));
            }
        }
    }

}
