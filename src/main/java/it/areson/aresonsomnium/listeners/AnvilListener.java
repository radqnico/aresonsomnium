package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public class AnvilListener extends GeneralEventListener {

    private final ItemStack airStack;

    public AnvilListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
        airStack = new ItemStack(Material.AIR);
    }

    @EventHandler
    public void onAnvilUpdate(PrepareAnvilEvent event) {
        ItemStack firstItem = event.getInventory().getFirstItem();
        ItemStack secondItem = event.getInventory().getSecondItem();

        if (firstItem != null && secondItem != null && secondItem.getType().equals(Material.ENCHANTED_BOOK)) {
            if (aresonSomnium.isALockedEnchantFromEnchants(firstItem)) {
                event.setResult(airStack);
                aresonSomnium.sendErrorMessage(event.getView().getPlayer(), "Questo oggetto è immodificabile");
            }
        }
    }

}
