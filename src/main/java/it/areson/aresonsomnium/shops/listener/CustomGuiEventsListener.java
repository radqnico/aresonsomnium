package it.areson.aresonsomnium.shops.listener;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import it.areson.aresonsomnium.shops.guis.CustomShop;
import it.areson.aresonsomnium.shops.guis.ShopManager;
import it.areson.aresonsomnium.shops.items.ShopItem;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

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
        if (shopManager.isViewingCustomGui(player)) {
            Inventory clickedInventory = event.getClickedInventory();
            CustomShop customShop = shopManager.getViewingCustomShop(player);
            if (Objects.nonNull(clickedInventory)) {
                if (clickedInventory.getType().equals(InventoryType.CHEST)) {
                    switch (event.getClick()) {
                        case LEFT:
                            int slot = event.getSlot();
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
            player.sendMessage("Editing");
            player.sendMessage("Action: " + event.getAction().name() + "\n");
            player.sendMessage(event.toString());
        }
    }

}
