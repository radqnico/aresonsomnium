package it.areson.aresonsomnium.economy.shops.items;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class OldShopItem {

    private final ItemStack itemStack;
    private final OldPrice shoppingOldPrice;
    private final OldPrice sellingOldPrice;

    public OldShopItem(ItemStack itemStack, OldPrice shoppingOldPrice, OldPrice sellingOldPrice) {
        this.itemStack = itemStack;
        this.shoppingOldPrice = shoppingOldPrice;
        this.sellingOldPrice = sellingOldPrice;
    }

    public OldShopItem(ItemStack itemStack) {
        this(itemStack, new OldPrice(), new OldPrice());
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public OldPrice getShoppingPrice() {
        return shoppingOldPrice;
    }

    public OldPrice getSellingPrice() {
        return sellingOldPrice;
    }

    public SerializedShopItem toSerializedShopItem() {
        return new SerializedShopItem(
                Base64.getEncoder().encodeToString(itemStack.serializeAsBytes()),
                shoppingOldPrice.getCoins(),
                shoppingOldPrice.getObols(),
                shoppingOldPrice.getGems(),
                sellingOldPrice.getCoins(),
                sellingOldPrice.getObols(),
                sellingOldPrice.getGems()
        );
    }

    public boolean isSellable(){
        return sellingOldPrice.isPriceReady();
    }
}
