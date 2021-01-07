package it.areson.aresonsomnium.shops.listener;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import it.areson.aresonsomnium.shops.guis.CustomShop;
import it.areson.aresonsomnium.shops.guis.EditPriceConfig;
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

    public CustomGuiEventsListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
        this.shopManager = aresonSomnium.getShopManager();
        shopEditor = aresonSomnium.getShopEditor();
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        Player player = (Player) event.getView().getPlayer();
        if (!shopEditor.isEditingPrice(player)) {
            if (shopEditor.isEditingCustomGui(player)) {
                CustomShop customShop = shopEditor.getEditingCustomShop(player);
                if (shopEditor.endEditGui(player)) {
                    customShop.saveToDB();
                    aresonSomnium.getLogger().info(MessageUtils.successMessage("GUI modificata da '" + player.getName() + "' salvata su DB"));
                } else {
                    aresonSomnium.getLogger().info(MessageUtils.warningMessage("GUI modificata da '" + player.getName() + "' NON salvata DB"));
                }
            } else if (shopManager.isViewingCustomGui(player)) {
                shopManager.playerCloseGui(player);
            }
        } else {
            shopEditor.endEditPrice(player);
            shopEditor.endEditGui(player);
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        Inventory clickedInventory = event.getClickedInventory();

        if (shopManager.isViewingCustomGui(player)) {
            // Click to shop
            CustomShop customShop = shopManager.getViewingCustomShop(player);
            ClickType click = event.getClick();
            shopClickOnItem(customShop, clickedInventory, click, slot, player);
            event.setCancelled(true);
        } else if (shopEditor.isEditingCustomGui(player)) {
            // Click to edit
            CustomShop customShop = shopEditor.getEditingCustomShop(player);
            InventoryAction action = event.getAction();
            if (shopEditor.isEditingPrice(player)) {
                EditPriceConfig editingPriceConfig = shopEditor.getEditingPriceConfig(player);
                switch (slot) {
                    case 11:
                        editingPriceConfig.setCoinType(CoinType.BASIC);
                        player.closeInventory();
                        player.sendMessage(MessageUtils.warningMessage(" --> Inserisci Basic Coins <--"));
                        break;
                    case 13:
                        editingPriceConfig.setCoinType(CoinType.CHARON);
                        player.closeInventory();
                        player.sendMessage(MessageUtils.warningMessage(" --> Inserisci Monete di Caronte <--"));
                        break;
                    case 15:
                        editingPriceConfig.setCoinType(CoinType.FORCED);
                        player.closeInventory();
                        player.sendMessage(MessageUtils.warningMessage(" --> Inserisci Gemme <--"));
                        break;
                }
                event.setCancelled(true);
            } else {
                switch (action) {
                    case PICKUP_ALL:
                        handlePickupAll(clickedInventory, customShop, player, slot);
                        break;
                    case PLACE_ALL:
                        handlePlaceAll(clickedInventory, customShop, player, slot);
                        break;
                    case PICKUP_HALF:
                        EditPriceConfig editPriceConfig = shopEditor.newEditPrice(player, customShop);
                        player.openInventory(shopEditor.getPricesInventory());
                        editPriceConfig.setSlot(slot);
                        aresonSomnium.getSetPriceInChatListener().registerEvents();
                        break;
                    default:
                        event.setCancelled(true);
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (shopEditor.isEditingCustomGui(player) || shopManager.isViewingCustomGui(player)) {
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

    private void handlePlaceAll(Inventory clickedInventory, CustomShop customShop, Player player, int slot) {
        if (Objects.nonNull(clickedInventory) && clickedInventory.getType().equals(InventoryType.CHEST)) {
            ShopItem pickupItem = aresonSomnium.getShopEditor().getPickupItem(player);
            if (Objects.nonNull(pickupItem)) {
                shopEditor.addNewItemToShop(customShop, slot, pickupItem);
                aresonSomnium.getDebugger().debugInfo("Oggetto salvato recuperato");
            } else {
                ItemStack currentItem = clickedInventory.getItem(slot);
                shopEditor.addNewItemToShop(customShop, slot, new ShopItem(currentItem));
                aresonSomnium.getDebugger().debugInfo("Oggetto nuovo inserito");
            }
        } else {
            // Remove saved item
            shopEditor.getPickupItem(player);
        }
    }

}
