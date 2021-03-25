package it.areson.aresonsomnium.economy.guis;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.commands.shopadmin.BuyItemCommand;
import it.areson.aresonsomnium.economy.items.ShopItem;
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
import java.util.Optional;

public class ClickToShopEventsListener extends GeneralEventListener {

    public ClickToShopEventsListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();
        Player player = (Player) event.getWhoClicked();
        System.out.println(AresonSomniumAPI.instance.commandPanelsAPI.isPanelOpen(player));
        if (Objects.equals(clickedInventory, topInventory)) {
            System.out.println("Objects.equals(clickedInventory, topInventory)");
            // CLick su inventario in alto
            if (isLeftClicking(event)) {
                System.out.println("isLeftClicking(event)");
                System.out.println(AresonSomniumAPI.instance.commandPanelsAPI.isPanelOpen(player));
                // Click sinistro pulito
                ItemStack item = clickedInventory.getItem(event.getSlot());
                if (item != null) {
                    System.out.println("item != null");
                    // Cerco ID del mio plugin
                    int idFromItemData = ShopItem.getIdFromItem(item);
                    if (idFromItemData != -1) {
                        System.out.println("idFromItemData != -1");
                        System.out.println(AresonSomniumAPI.instance.commandPanelsAPI.isPanelOpen(player));
                        // Se c'è ID su oggetto, vedo se lo conosco
                        if (AresonSomniumAPI.instance.commandPanelsAPI.isPanelOpen(player)) {
                            System.out.println("AresonSomniumAPI.instance.commandPanelsAPI.isPanelOpen(player)");
                            Optional<ShopItem> itemById = AresonSomniumAPI.instance.shopItemsManager.getItemsGateway().getItemById(idFromItemData);
                            if (itemById.isPresent()) {
                                System.out.println("itemById.isPresent()");
                                // Se l'ID esiste, provo a farglielo acquistare
                                BuyItemCommand.buyItem(idFromItemData, player, AresonSomniumAPI.instance.getServer().getConsoleSender());
                            } else {
                                AresonSomniumAPI.instance.getLogger().warning("ID ShopItem " + idFromItemData + " non trovato.");
                            }
                        }
                    }
                }
            }
        }
    }

}
