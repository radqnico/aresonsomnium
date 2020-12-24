package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.shops.ShopManager;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
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
            if (shopManager.endEditGui(player, event.getInventory())) {
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
            if(Objects.nonNull(clickedInventory)) {
                if(clickedInventory.getType().equals(InventoryType.CHEST)){
                    ItemStack currentItem = event.getCurrentItem();
                    if(Objects.nonNull(currentItem) && !currentItem.getType().equals(Material.AIR)){
                        HashMap<Integer, ItemStack> integerItemStackHashMap = player.getInventory().addItem(currentItem.clone());
                        if(integerItemStackHashMap.isEmpty()){
                            // Scala monete
                        } else {
                            player.sendMessage(MessageUtils.errorMessage("Hai l'inventario pieno!"));
                        }
                    }
                }
            }
            event.setCancelled(true);
        }
    }

}
