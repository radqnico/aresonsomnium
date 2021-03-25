package it.areson.aresonsomnium.economy.shops.newsystem;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class ItemListViewEventsListener extends GeneralEventListener {

    public ItemListViewEventsListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();
        if (aresonSomnium.shopItemsManager.checkIfIsItemsEditor(player, clickedInventory)) {
            player.sendMessage("Inventario nostro");
            aresonSomnium.shopItemsManager.itemClickedInEditor(player, event.getSlot());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDragEvent(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getWhoClicked();
        if (aresonSomnium.shopItemsManager.checkIfIsItemsEditor(player, inventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        Player player = (Player) event.getPlayer();
        InventoryCloseEvent.Reason reason = event.getReason();
        if (aresonSomnium.shopItemsManager.checkIfIsItemsEditor(player, topInventory)) {
            if (!reason.equals(InventoryCloseEvent.Reason.OPEN_NEW)) {
                aresonSomnium.shopItemsManager.playerClosedEditGui(player);
            }
        }
    }

}
