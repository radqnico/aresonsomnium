package it.areson.aresonsomnium.economy.guis;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.economy.items.ShopItem;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class OpClickShopItemWithPanelOpenedEventsListener extends GeneralEventListener {

    public OpClickShopItemWithPanelOpenedEventsListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

//    @EventHandler
//    public void onInventoryClickEvent(InventoryClickEvent event) {
//        Inventory clickedInventory = event.getClickedInventory();
//        Player player = (Player) event.getWhoClicked();
//        if (player.hasPermission("aresonsomnium.admin") && AresonSomniumAPI.instance.commandPanelsAPI.isPanelOpen(player)) {
//            // CLick su inventario in alto
//            if (isLeftClicking(event) && clickedInventory != null) {
//                // Click sinistro pulito
//                ItemStack item = clickedInventory.getItem(event.getSlot());
//                if (item != null) {
//                    // Cerco ID del mio plugin
//                    int idFromItemData = ShopItem.getIdFromItem(item);
//                    if (idFromItemData != -1) {
//                        // Se c'è ID su oggetto, vedo se lo conosco
//                        Optional<ShopItem> itemById = AresonSomniumAPI.instance.shopItemsManager.getItemsGateway().getItemById(idFromItemData);
//                        if (itemById.isPresent()) {
//                            player.sendMessage("ID ShopItem: " + idFromItemData);
//                        } else {
//                            player.sendMessage(ChatColor.RED + "ATTENZIONE: ID ShopItem " + idFromItemData + " NON TROVATO sul DataBase. Prova con un reload degli items.");
//                        }
//                    }
//                }
//            }
//        }
//    }

}
