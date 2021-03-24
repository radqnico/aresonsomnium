package it.areson.aresonsomnium.economy.shops.guis.newsystem;

import it.areson.aresonsomnium.Constants;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShopItem {

    private final int id;
    private final ItemStack itemStack;
    private final int amount;
    private final Price shoppingPrice;
    private final Price sellingPrice;

    public ShopItem(int id, ItemStack itemStack, int amount, Price shoppingPrice, Price sellingPrice) {
        this.id = id;
        this.itemStack = itemStack.asOne();
        this.amount = amount;
        this.shoppingPrice = shoppingPrice;
        this.sellingPrice = sellingPrice;
    }

    public ShopItem(int id, ItemStack itemStack) {
        this(id, itemStack, 1, new Price(), new Price());
    }

    public int getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public ItemStack getItemStack() {
        ItemStack clone = itemStack.clone();
        clone.setAmount(amount);
        ItemMeta itemMeta = clone.getItemMeta();
        if (Objects.nonNull(itemMeta)) {
            itemMeta.setCustomModelData(id + Constants.SHOP_ITEM_OFFSET_DATA);
            List<Component> lore = itemMeta.lore();
            if(lore==null){
                lore = new ArrayList<>();
            }
            lore.add(Component.empty());
            lore.addAll(shoppingPrice.toLore(true));
            lore.addAll(sellingPrice.toLore(false));
            itemMeta.lore(lore);
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
        if (!(o instanceof ShopItem)) return false;
        ShopItem shopItem = (ShopItem) o;
        return id == shopItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
