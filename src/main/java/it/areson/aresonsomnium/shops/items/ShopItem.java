package it.areson.aresonsomnium.shops.items;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class ShopItem {

    private final ItemStack itemStack;
    private Price shoppingPrice;
    private Price sellingPrice;

    public ShopItem(ItemStack itemStack, Price shoppingPrice, Price sellingPrice) {
        this.itemStack = itemStack;
        this.shoppingPrice = shoppingPrice;
        this.sellingPrice = sellingPrice;
    }

    public ShopItem(org.bukkit.inventory.ItemStack itemStack) {
        this(itemStack, new Price(), new Price());
    }

    public org.bukkit.inventory.ItemStack getItemStack() {
        return itemStack;
    }

    public Price getShoppingPrice() {
        return shoppingPrice;
    }

    public void setShoppingPrice(Price shoppingPrice) {
        this.shoppingPrice = shoppingPrice;
    }

    public Price getSellingPrice() {
        return sellingPrice;
    }

    public SerializedShopItem toSerializedShopItem() {
        return new SerializedShopItem(
                Base64.getEncoder().encodeToString(itemStack.serializeAsBytes()),
                shoppingPrice.getCoins(),
                shoppingPrice.getObols(),
                shoppingPrice.getGems(),
                sellingPrice.getCoins(),
                sellingPrice.getObols(),
                sellingPrice.getGems()
        );
    }

    public boolean isSellable(){
        return sellingPrice.isPriceReady();
    }
}
