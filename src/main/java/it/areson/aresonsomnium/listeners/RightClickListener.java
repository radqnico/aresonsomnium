package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
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

import java.util.Objects;

import static it.areson.aresonsomnium.Constants.*;

public class RightClickListener extends GeneralEventListener {

    public RightClickListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
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

                        break;
                }
            }
        }
    }

    public void collectGommaReward(PlayerInteractEvent event) {
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
