package it.areson.aresonsomnium.economy.items;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Price;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.bukkit.persistence.PersistentDataType.INTEGER;

public class ShopItem {

    private final int id;
    private final ItemStack itemStack;
    private final int amount;
    private final Price shoppingPrice;
    private final Price sellingPrice;
    private final NamespacedKey itemIdNamespacedKey;

    public ShopItem(AresonSomnium aresonSomnium, int id, ItemStack itemStack, int amount, Price shoppingPrice, Price sellingPrice) {
        this.id = id;
        this.itemStack = itemStack.asOne();
        this.amount = amount;
        this.shoppingPrice = shoppingPrice;
        this.sellingPrice = sellingPrice;
        this.itemIdNamespacedKey = aresonSomnium.getItemIdNamespacedKey();
    }

    public int getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public ItemStack getItemStack(boolean setLorePrices, boolean putSomniumIdInLore) {
        return getItemStack(setLorePrices, putSomniumIdInLore, true);
    }

    public ItemStack getItemStack(boolean setLorePrices, boolean putSomniumIdInLore, boolean putTags) {
        ItemStack clone = itemStack.clone();
        clone.setAmount(amount);
        ItemMeta itemMeta = clone.getItemMeta();
        if (Objects.nonNull(itemMeta)) {
            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();

            if (putTags) {
                persistentDataContainer.set(itemIdNamespacedKey, INTEGER, id);
            } else {
                persistentDataContainer.remove(itemIdNamespacedKey);
            }

            List<Component> lore = itemMeta.lore();

            if (lore == null) {
                lore = new ArrayList<>();
            }

            if (setLorePrices) {
                lore.add(Component.empty());
                lore.addAll(shoppingPrice.toLore(true));
                lore.addAll(sellingPrice.toLore(false));
            }

            if (putSomniumIdInLore) {
                lore.add(Component.empty());
                lore.add(Component.text("SomniumID: " + id).color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
            }

            if (putTags) {
                itemMeta.lore(lore);
            } else {
                itemMeta.lore(null);
            }

            clone.setItemMeta(itemMeta);
        }
        return clone;
    }

    public Price getShoppingPrice() {
        return shoppingPrice;
    }

    public Price getSellingPrice() {
        return sellingPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShopItem shopItem)) return false;
        return id == shopItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
