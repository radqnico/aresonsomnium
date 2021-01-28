package it.areson.aresonsomnium.gomma;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class GommaGommaEventListener extends GeneralEventListener {

    public GommaGommaEventListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (EquipmentSlot.HAND.equals(event.getHand())) {
            Block clickedBlock = event.getClickedBlock();
            if (Objects.nonNull(clickedBlock)) {
                Location location = clickedBlock.getLocation();
                Location gommaBlockLocation = aresonSomnium.getGommaObjectsFileReader().getGommaBlock();
                if (location.equals(gommaBlockLocation)) {
                    gommaPreconditionsOk(event.getPlayer());
                    event.setCancelled(true);
                }
            }
        }
    }

    public void gommaPreconditionsOk(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack itemInMainHand = playerInventory.getItemInMainHand();
        ItemMeta itemInMainHandItemMeta = itemInMainHand.getItemMeta();
        if (Objects.nonNull(itemInMainHandItemMeta) && itemInMainHandItemMeta.hasCustomModelData()) {
            int customModelData = itemInMainHandItemMeta.getCustomModelData();
            if (customModelData == GommaConstants.customModelData) {
                if (playerInventory.addItem(aresonSomnium.getGommaObjectsFileReader().getRandomItem()).isEmpty()) {
                    itemInMainHand.setAmount(itemInMainHand.getAmount() - 1);
                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("gomma-item-give"));
                } else {
                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("gomma-error-give"));
                }
            }
        }
    }

}
