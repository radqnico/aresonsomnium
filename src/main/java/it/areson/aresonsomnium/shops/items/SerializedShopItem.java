package it.areson.aresonsomnium.shops.items;

import com.google.gson.Gson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;

public class SerializedShopItem {

    private final String serializedItemStack;
    private final BigDecimal basicCoins;
    private final BigInteger obols;
    private final BigInteger gems;

    public SerializedShopItem(String serializedItemStack, BigDecimal basicCoins, BigInteger obols, BigInteger gems) {
        this.serializedItemStack = serializedItemStack;
        this.basicCoins = basicCoins;
        this.obols = obols;
        this.gems = gems;
    }

    public static SerializedShopItem fromJson(String json) {
        return new Gson().fromJson(json, SerializedShopItem.class);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public ShopItem toShopItem() {
        return new ShopItem(
                org.bukkit.inventory.ItemStack.deserializeBytes(Base64.getDecoder().decode(serializedItemStack)),
                new Price(basicCoins, obols, gems)
        );
    }
}
