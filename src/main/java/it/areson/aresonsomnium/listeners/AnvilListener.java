package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

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
                cancelEvent(event);
                aresonSomnium.sendErrorMessage(event.getView().getPlayer(), "Questo oggetto non è modificabile");
                return;
            }

            ItemMeta firstItemMeta = firstItem.getItemMeta();
            if (firstItemMeta != null) {
                EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) firstItemMeta;

                if (!aresonSomnium.hasCompatibleEnchants(firstItem, enchantmentMeta.getStoredEnchants())) {
                    cancelEvent(event);
                    aresonSomnium.sendErrorMessage(event.getView().getPlayer(), "Enchant invalido");
                }
            }

        }
    }

    private void cancelEvent(PrepareAnvilEvent event) {
        event.setResult(airStack);
        event.getView().close();
    }

}
