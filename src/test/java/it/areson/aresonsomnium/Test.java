package it.areson.aresonsomnium;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import static org.junit.Assert.assertEquals;

public class Test {

    @org.junit.Test
    public void ciao() {
        ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD, 1);
        HashMap<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.ARROW_DAMAGE, 2);
        enchants.put(Enchantment.BINDING_CURSE, 2);

        System.out.println(itemStack.toString());

        //affilatezza
        //anatema
        //fold
        //reduce

        //ItemStack
        //null
//        BiFunction<ItemStack, Map.Entry<Enchantment, Integer>, ItemStack> biFunction = new BiFunction<ItemStack, Map.Entry<Enchantment, Integer>, ItemStack>() {
//            @Override
//            public ItemStack apply(ItemStack itemStack, Map.Entry<Enchantment, Integer> enchantmentIntegerEntry) {
//                return itemStack.add(1);
//            }
//        };
//        BinaryOperator<ItemStack> itemStackBinaryOperator = new BinaryOperator<ItemStack>() {
//            @Override
//            public ItemStack apply(ItemStack itemStack, ItemStack itemStack2) {
//                return itemStack2.add(itemStack.getAmount() + itemStack2.getAmount());
//            }
//        };
//        ItemStack reduce = enchants.entrySet().stream().reduce(itemStack, biFunction, itemStackBinaryOperator);
//        System.out.println(reduce.toString());


//        return enchantment.canEnchantItem(clickedItemStack)
//                && !clickedItemMeta.hasConflictingEnchant(enchantment)
//                && (currentEnchantmentLevel == null || currentEnchantmentLevel < entry.getValue());


        assertEquals(1, 1);
    }

}
