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
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CustomGuiEventsListener extends GeneralEventListener {

    private final ShopManager shopManager;
    private final ShopEditor shopEditor;

    public CustomGuiEventsListener(AresonSomnium aresonSomnium, ShopManager shopManager) {
        super(aresonSomnium);
        this.shopManager = shopManager;
        shopEditor = aresonSomnium.getShopEditor();
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
        ShopManager shopManager = aresonSomnium.getShopManager();
        int slot = event.getSlot();
        Inventory clickedInventory = event.getClickedInventory();

        if (shopManager.isViewingCustomGui(player)) {
            // Click to shop
            CustomShop customShop = shopManager.getViewingCustomShop(player);
            ClickType click = event.getClick();
            shopClickOnItem(customShop, clickedInventory, click, slot, player);
            event.setCancelled(true);
        } else if (shopManager.isEditingCustomGui(player)) {
            // Click to edit
            CustomShop customShop = shopManager.getEditingCustomShop(player);
            InventoryAction action = event.getAction();
            switch (action) {
                case PICKUP_ALL:
                    handlePickupAll(clickedInventory, customShop, player, slot);
                    break;
                case PLACE_ALL:
                    handlePlaceAll(clickedInventory, customShop, player, slot, event.getCurrentItem());
                    break;
                default:
                    event.setCancelled(true);
                    break;
            }
        }
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        ShopManager shopManager = aresonSomnium.getShopManager();
        if (shopManager.isEditingCustomGui(player) || shopManager.isViewingCustomGui(player)) {
            event.setCancelled(true);
        }
    }

    private void shopClickOnItem(CustomShop customShop, Inventory clickedInventory, ClickType clickType, int slot, Player player) {
        if (Objects.nonNull(clickedInventory)) {
            if (clickedInventory.getType().equals(InventoryType.CHEST)) {
                if (clickType == ClickType.LEFT) {
                    ShopItem shopItem = customShop.getItems().get(slot);
                    if (Objects.nonNull(shopItem)) {
                        player.sendMessage(shopItem.getPrice().toString());
                    }
                }
            }
        }
    }

    private void handlePickupAll(Inventory clickedInventory, CustomShop customShop, Player player, int slot) {
        if (Objects.nonNull(clickedInventory) && clickedInventory.getType().equals(InventoryType.CHEST)) {
            ShopItem shopItem = customShop.getItems().get(slot);
            if (Objects.nonNull(shopItem)) {
                shopEditor.setPickupItems(player, shopItem);
                shopEditor.removeItemFromShop(customShop, slot);
            }
            aresonSomnium.getDebugger().debugInfo("Oggetto rimosso");
        } else {
            // Remove saved item
            shopEditor.getPickupItem(player);
        }
    }

    private void handlePlaceAll(Inventory clickedInventory, CustomShop customShop, Player player, int slot, ItemStack currentItem) {
        if (Objects.nonNull(clickedInventory) && clickedInventory.getType().equals(InventoryType.CHEST)) {
            ShopItem pickupItem = aresonSomnium.getShopEditor().getPickupItem(player);
            if (Objects.nonNull(pickupItem)) {
                shopEditor.addNewItemToShop(customShop, slot, pickupItem);
                aresonSomnium.getDebugger().debugInfo("Oggetto salvato recuperato");
            } else {
                shopEditor.addNewItemToShop(customShop, slot, new ShopItem(currentItem));
                aresonSomnium.getDebugger().debugInfo("Oggetto nuovo inserito");
            }
        } else {
            // Remove saved item
            shopEditor.getPickupItem(player);
        }
    }

}
