package it.areson.aresonsomnium.shops.listener;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import it.areson.aresonsomnium.shops.guis.CustomShop;
import it.areson.aresonsomnium.shops.guis.ShopEditor;
import it.areson.aresonsomnium.shops.guis.ShopManager;
import it.areson.aresonsomnium.shops.items.ShopItem;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CustomGuiEventsListener extends GeneralEventListener {

    private final ShopManager shopManager;

    public CustomGuiEventsListener(AresonSomnium aresonSomnium, ShopManager shopManager) {
        super(aresonSomnium);
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        Player player = (Player) event.getView().getPlayer();
        if (shopManager.isEditingCustomGui(player)) {
            if (shopManager.endEditGui(player)) {
                aresonSomnium.getLogger().info(MessageUtils.successMessage("GUI modificata da '" + player.getName() + "' salvata su DB"));
            } else {
                aresonSomnium.getLogger().info(MessageUtils.warningMessage("GUI modificata da '" + player.getName() + "' NON salvata DB"));
            }
        } else if (shopManager.isViewingCustomGui(player)) {
            shopManager.playerCloseGui(player);
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ShopManager shopManager = aresonSomnium.getGuiManager();
        int slot = event.getSlot();
        Inventory clickedInventory = event.getClickedInventory();
        if (shopManager.isViewingCustomGui(player)) {
            CustomShop customShop = shopManager.getViewingCustomShop(player);
            if (Objects.nonNull(clickedInventory)) {
                if (clickedInventory.getType().equals(InventoryType.CHEST)) {
                    switch (event.getClick()) {
                        case LEFT:
                            ShopItem shopItem = customShop.getItems().get(slot);
                            if (Objects.nonNull(shopItem)) {
                                player.sendMessage(shopItem.getPrice().toString());
                            }
                            break;
                    }
                }
            }
            event.setCancelled(true);
        } else if (shopManager.isEditingCustomGui(player)) {
            CustomShop customShop = shopManager.getEditingCustomShop(player);
            InventoryAction action = event.getAction();
            switch (action) {
                case PICKUP_ALL:
                    if (Objects.nonNull(clickedInventory) && clickedInventory.getType().equals(InventoryType.CHEST)) {
                        handlePickupItem(customShop, player, slot);
                    } else {
                        ShopEditor.getPickupItem(player);
                    }
                    break;
                case PLACE_ALL:
                    if (Objects.nonNull(clickedInventory) && clickedInventory.getType().equals(InventoryType.CHEST)) {
                        handlePlaceAll(customShop, player, slot, event.getCurrentItem());
                    } else {
                        ShopEditor.getPickupItem(player);
                    }
                    break;
                default:
                    event.setCancelled(true);
                    break;
            }
        }

    }

    private void handlePickupItem(CustomShop customShop, Player player, int slot) {
        ShopItem shopItem = customShop.getItems().get(slot);
        if (Objects.nonNull(shopItem)) {
            ShopEditor.setPickupItems(player, shopItem);
            ShopEditor.removeItemFromShop(customShop, slot);
        }
        player.sendMessage("Oggetto rimosso");
    }

    private void handlePlaceAll(CustomShop customShop, Player player, int slot, ItemStack currentItem) {
        ShopItem pickupItem = ShopEditor.getPickupItem(player);
        if (Objects.nonNull(pickupItem)) {
            ShopEditor.addNewItemToShop(customShop, slot, pickupItem);
            player.sendMessage("Oggetto SALVATO rimesso");
        } else {
            ShopEditor.addNewItemToShop(customShop, slot, new ShopItem(currentItem));
            player.sendMessage("Oggetto nuovo");
        }
    }

}
