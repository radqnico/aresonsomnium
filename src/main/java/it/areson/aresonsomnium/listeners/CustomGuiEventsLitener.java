package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.shops.GuiManager;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class CustomGuiEventsLitener extends GeneralEventListener {

    private final GuiManager guiManager;

    public CustomGuiEventsLitener(AresonSomnium aresonSomnium, GuiManager guiManager) {
        super(aresonSomnium);
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (guiManager.someoneEditing()) {
            Player player = (Player) event.getView().getPlayer();
            if (guiManager.endEditGui(player, event.getInventory())) {
                aresonSomnium.getLogger().info(MessageUtils.successMessage("GUI modificata da '" + player.getName() + "' salvata su DB"));
            } else {
                aresonSomnium.getLogger().info(MessageUtils.warningMessage("GUI modificata da '" + player.getName() + "' NON salvata DB"));
            }
        }
    }

}
