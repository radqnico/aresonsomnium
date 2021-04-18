package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Map;

public class InventoryListener extends GeneralEventListener {

    public InventoryListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        if (whoClicked instanceof Player && event.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {
            ItemStack handItemStack = event.getCursor();
            ItemStack clickedItemStack = event.getCurrentItem();

            if (handItemStack != null && handItemStack.getType().equals(Material.ENCHANTED_BOOK) && clickedItemStack != null) {
                isVanillaEnchantedBook(handItemStack);
                handleEnchantedBook(whoClicked, handItemStack, clickedItemStack);
            }
        }
    }

    private boolean isVanillaEnchantedBook(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        persistentDataContainer.getKeys().parallelStream().anyMatch((namespacedKey -> {
            System.out.println(namespacedKey.getKey());
            return false;
        }));
        return false;
    }

    private void handleEnchantedBook(HumanEntity humanEntity, ItemStack handItemStack, ItemStack clickedItemStack) {
        if (!aresonSomnium.isALockedEnchantFromEnchants(clickedItemStack)) {
            EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) handItemStack.getItemMeta();
            ItemMeta clickedItemMeta = clickedItemStack.getItemMeta();

            if (enchantmentMeta != null && clickedItemMeta != null) {
                Map<Enchantment, Integer> storedEnchants = enchantmentMeta.getStoredEnchants();

                if (aresonSomnium.hasCompatibleEnchants(clickedItemStack, storedEnchants)) {
                    handItemStack.setAmount(handItemStack.getAmount() - 1);
                    storedEnchants.entrySet().parallelStream().forEach((entry) -> clickedItemStack.addUnsafeEnchantment(entry.getKey(), entry.getValue()));
                }
            }
        } else {
            aresonSomnium.sendErrorMessage(humanEntity, "Questo oggetto è immodificabile");
        }
    }

}
