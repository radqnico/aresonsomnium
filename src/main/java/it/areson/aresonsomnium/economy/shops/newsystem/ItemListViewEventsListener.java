package it.areson.aresonsomnium.economy.shops.newsystem;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class ItemListViewEventsListener extends GeneralEventListener {

    public ItemListViewEventsListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        Player player = (Player) event.getWhoClicked();
        if (aresonSomnium.shopItemsManager.checkIfPlayerClickedInItemsEditor(player, topInventory)) {
            player.sendMessage("Inventario nostro");
            event.setCancelled(true);
        }
    }

}
