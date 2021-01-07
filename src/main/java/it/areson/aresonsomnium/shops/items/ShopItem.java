package it.areson.aresonsomnium.shops.items;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class ShopItem {

    private final ItemStack itemStack;
    private Price price;
    private boolean loreSet;

    public ShopItem(ItemStack itemStack, Price price, boolean loreSet) {
        this.itemStack = itemStack;
        this.price = price;
        this.loreSet = loreSet;
    }

    public ShopItem(ItemStack itemStack) {
        this(itemStack, new Price(), false);
    }

    public boolean isLoreSet() {
        return loreSet;
    }

    public void setLoreSet(boolean loreSet) {
        this.loreSet = loreSet;
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
                price.getForcedCoins(),
                loreSet);
    }
}
