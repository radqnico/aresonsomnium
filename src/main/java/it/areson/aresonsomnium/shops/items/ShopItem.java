package it.areson.aresonsomnium.shops.items;

import java.util.Base64;

public class ShopItem {

    private final org.bukkit.inventory.ItemStack itemStack;
    private Price price;

    public ShopItem(org.bukkit.inventory.ItemStack itemStack, Price price) {
        this.itemStack = itemStack;
        this.price = price;
    }

    public ShopItem(org.bukkit.inventory.ItemStack itemStack) {
        this(itemStack, new Price());
    }

    public org.bukkit.inventory.ItemStack getItemStack() {
        return itemStack;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public SerializedShopItem toSerializedShopItem() {
        return new SerializedShopItem(
                Base64.getEncoder().encodeToString(itemStack.serializeAsBytes()),
                price.getCoins(),
                price.getObols(),
                price.getGems()
        );
    }
}
