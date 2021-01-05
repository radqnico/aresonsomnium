package it.areson.aresonsomnium.shops.items;

import com.google.gson.Gson;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Base64;

public class SerializedShopItem {

    private final String serializedItemStack;
    private final BigDecimal basicCoins;
    private final int charonCoins;
    private final int forcedCoins;

    public SerializedShopItem(String serializedItemStack, BigDecimal basicCoins, int charonCoins, int forcedCoins) {
        this.serializedItemStack = serializedItemStack;
        this.basicCoins = basicCoins;
        this.charonCoins = charonCoins;
        this.forcedCoins = forcedCoins;
    }

    public static SerializedShopItem fromJson(String json) {
        return new Gson().fromJson(json, SerializedShopItem.class);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public ShopItem toShopItem() {
        return new ShopItem(
                ItemStack.deserializeBytes(Base64.getDecoder().decode(serializedItemStack)),
                new Price(basicCoins, charonCoins, forcedCoins)
        );
    }
}