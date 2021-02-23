package it.areson.aresonsomnium.shops.items;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class ShopItem {

    private final ItemStack itemStack;
    private final Price shoppingPrice;
    private final Price sellingPrice;

    public ShopItem(ItemStack itemStack, Price shoppingPrice, Price sellingPrice) {
        this.itemStack = itemStack;
        this.shoppingPrice = shoppingPrice;
        this.sellingPrice = sellingPrice;
    }

    public ShopItem(ItemStack itemStack) {
        this(itemStack, new Price(), new Price());
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Price getShoppingPrice() {
        return shoppingPrice;
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
