package it.areson.aresonsomnium.shops;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class ShopItem {

    private final ItemStack itemStack;
    private Price price;

    public ShopItem(ItemStack itemStack, Price price) {
        this.itemStack = itemStack;
        this.price = price;
    }

    public ShopItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.price = new Price();
    }

    public ItemStack getItemStack() {
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
                price.getBasicCoins(),
                price.getCharonCoins(),
                price.getForcedCoins()
        );
    }
}
