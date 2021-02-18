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

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

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
                ItemMeta clickedItemMeta = clickedItemStack.getItemMeta();
                if (enchantmentMeta != null && clickedItemMeta != null) {
                    Map<Enchantment, Integer> storedEnchants = enchantmentMeta.getStoredEnchants();

                    player.sendMessage("Here");


                    BiFunction<ItemStack, Map.Entry<Enchantment, Integer>, ItemStack> add = (a, b) -> a = a.add(2);
                    BinaryOperator<ItemStack> func2 = (old, niu) -> {
                        player.sendMessage("Old " + old.getAmount());
                        player.sendMessage("Niu " + niu.getAmount());
                        return old;
                    };
                    ItemStack reduce = storedEnchants.entrySet().stream().parallel().reduce(clickedItemStack, add, func2);
                    player.sendMessage(reduce.toString());


//                    boolean validateEnchants = storedEnchants.entrySet().stream().parallel().reduce(, (we, entry) -> {
//                        Enchantment enchantment = entry.getKey();
//                        Integer currentEnchantmentLevel = clickedItemStack.getEnchantments().get(enchantment);
//
//
//                        return we;
////                        return clickedItemStack;
////                        return enchantment.canEnchantItem(clickedItemStack)
////                                && !clickedItemMeta.hasConflictingEnchant(enchantment)
////                                && (currentEnchantmentLevel == null || currentEnchantmentLevel < entry.getValue());
//                    }, ItemStack::addEnchantment);
//
////                    if(validateEnchants) {
////                        storedEnchants.entrySet().stream().parallel().forEach(entry -> {
////                            clickedItemStack.addEnchantment(entry.getKey(), entry.getValue());
////                        });
////
////                    }
//                    player.sendMessage("Risultato: " + validateEnchants);
                }
            }
        }
    }

}
