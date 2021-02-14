package it.areson.aresonsomnium.shops.items;

import com.google.gson.Gson;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;

public class SerializedShopItem {

    private final String serializedItemStack;
    private final BigDecimal shopCoins;
    private final BigInteger shopObols;
    private final BigInteger shopGems;
    private final BigDecimal sellCoins;
    private final BigInteger sellObols;
    private final BigInteger sellGems;

    public SerializedShopItem(String serializedItemStack, BigDecimal shopCoins, BigInteger shopObols, BigInteger shopGems, BigDecimal sellCoins, BigInteger sellObols, BigInteger sellGems) {
        this.serializedItemStack = serializedItemStack;
        this.shopCoins = shopCoins;
        this.shopObols = shopObols;
        this.shopGems = shopGems;
        this.sellCoins = sellCoins;
        this.sellObols = sellObols;
        this.sellGems = sellGems;
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
                new Price(shopCoins, shopObols, shopGems),
                new Price(sellCoins, sellObols, sellGems)
        );
    }
}
