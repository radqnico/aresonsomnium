package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener extends GeneralEventListener {

    public InventoryListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        if(whoClicked instanceof Player && event.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {
            Player player = (Player) whoClicked;
            ItemStack handItemStack = event.getCursor();
            ItemStack clickedItemStack = event.getCurrentItem();

            if(handItemStack != null && handItemStack.getType().equals(Material.ENCHANTED_BOOK) && clickedItemStack != null) {

                player.sendMessage("Okay");
            }
        }
    }

}
