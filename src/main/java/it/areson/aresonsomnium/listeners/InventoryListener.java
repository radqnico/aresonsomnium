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

import java.util.Map;

public class InventoryListener extends GeneralEventListener {

    public InventoryListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        if (whoClicked instanceof Player && event.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {
            Player player = (Player) whoClicked;
            ItemStack handItemStack = event.getCursor();
            ItemStack clickedItemStack = event.getCurrentItem();

            if (handItemStack != null && handItemStack.getType().equals(Material.ENCHANTED_BOOK) && clickedItemStack != null) {
                EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) handItemStack.getItemMeta();
                if (enchantmentMeta != null) {
                    Map<Enchantment, Integer> storedEnchants = enchantmentMeta.getStoredEnchants();

                    boolean validateEnchants = storedEnchants.entrySet().stream().parallel().allMatch(entry -> {
                        Enchantment enchantment = entry.getKey();
                        Integer currentEnchantmentLevel = clickedItemStack.getEnchantments().get(enchantment);
                        return enchantment.canEnchantItem(clickedItemStack) && (currentEnchantmentLevel == null || currentEnchantmentLevel < entry.getValue());
                    });

//                    if(validateEnchants) {
//                        storedEnchants.entrySet().stream().parallel().forEach(entry -> {
//                            clickedItemStack.addEnchantment(entry.getKey(), entry.getValue());
//                        });
//
//                    }
                    player.sendMessage("Risultato: " + validateEnchants);
                }
            }
        }
    }

}
