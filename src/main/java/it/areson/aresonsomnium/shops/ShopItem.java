package it.areson.aresonsomnium.shops;

import org.bukkit.inventory.ItemStack;

public class ShopItem {

    private final ItemStack itemStack;
    private float price;

    public ShopItem(ItemStack itemStack, float price) {
        this.itemStack = itemStack;
        this.price = price;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
