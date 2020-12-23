package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.shops.GuiManager;
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

    private final GuiManager guiManager;

    public CustomGuiEventsListener(AresonSomnium aresonSomnium, GuiManager guiManager) {
        super(aresonSomnium);
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        Player player = (Player) event.getView().getPlayer();
        if (guiManager.isEditingCustomGui(player)) {
            if (guiManager.endEditGui(player, event.getInventory())) {
                aresonSomnium.getLogger().info(MessageUtils.successMessage("GUI modificata da '" + player.getName() + "' salvata su DB"));
            } else {
                aresonSomnium.getLogger().info(MessageUtils.warningMessage("GUI modificata da '" + player.getName() + "' NON salvata DB"));
            }
        } else if (guiManager.isViewingCustomGui(player)) {
            guiManager.playerCloseGui(player);
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        GuiManager guiManager = aresonSomnium.getGuiManager();
        if (guiManager.isViewingCustomGui(player)) {
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
