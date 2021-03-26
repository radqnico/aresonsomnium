package it.areson.aresonsomnium.economy.guis;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ItemListViewEventsListener extends GeneralEventListener {

    public ItemListViewEventsListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();
        if (aresonSomnium.shopItemsManager.checkIfIsItemsEditor(player, clickedInventory)) {
            event.setCancelled(true);
            if (isShiftClicking(event) && isLeftClicking(event)) {
                // SHIFT + SINISTRO = cancella
                aresonSomnium.shopItemsManager.deleteItemInEditor(player, event.getSlot());
            } else if (!isShiftClicking(event) && isLeftClicking(event)) {
                // NON SHIFT + SINISTRO = clona
                aresonSomnium.shopItemsManager.itemClickedInEditor(player, event.getSlot());
            } else if (!isShiftClicking(event) && isRightClicking(event)) {
                // NON SHIFT + DESTRO = imposta prezzo acquisto
                aresonSomnium.shopItemsManager.sendPriceEditMessage(player, event.getSlot(), true);
            } else if (isShiftClicking(event) && isRightClicking(event)) {
                // SHIFT + DESTRO = imposta prezzo vendita
                aresonSomnium.shopItemsManager.sendPriceEditMessage(player, event.getSlot(), false);
            } else if (isPuttingNewItem(event)) {
                // nuovo oggetto
                ItemStack cursor = event.getCursor();
                if (Objects.nonNull(cursor)) {
                    ItemStack clone = cursor.clone();
                    aresonSomnium.shopItemsManager.itemPutIntoEditor(clone);
                }
            }
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
        if (aresonSomnium.shopItemsManager.checkIfIsItemsEditor(player, topInventory)) {
            aresonSomnium.shopItemsManager.playerClosedEditGui(player);
        }
    }

}
