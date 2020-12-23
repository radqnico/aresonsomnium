package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.shops.GuiManager;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

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
            System.out.println("Dentro");
            event.setCancelled(true);
        }
    }

}
