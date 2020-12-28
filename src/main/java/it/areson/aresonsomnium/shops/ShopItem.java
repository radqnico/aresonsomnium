package it.areson.aresonsomnium.shops;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import it.areson.aresonsomnium.economy.CoinType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.TreeMap;

public class ShopItem extends ItemStack {

    private TreeMap<CoinType, Float> priceMap;

    public ShopItem(ItemStack itemStack, TreeMap<CoinType, Float> priceMap) {
        super(itemStack);
        this.priceMap = priceMap;
    }

    public ShopItem(Material material, TreeMap<CoinType, Float> priceMap) {
        super(material);
        this.priceMap = priceMap;
    }

    public TreeMap<CoinType, Float> getPriceMap() {
        return priceMap;
    }

    public void setPriceMap(TreeMap<CoinType, Float> priceMap) {
        this.priceMap = priceMap;
    }

    public SerializedShopItem toSerialized() {
        byte[] bytes = serializeAsBytes();
        String itemStackString = Base64.getEncoder().encodeToString(bytes);
        return new SerializedShopItem(itemStackString, new TreeMap<>(priceMap));
    }

    public static class SerializedShopItem {
        @Expose
        private final String itemStack;
        @Expose
        private final TreeMap<CoinType, Float> prices;

        public SerializedShopItem(String serializedItemStack, TreeMap<CoinType, Float> priceMap) {
            this.itemStack = serializedItemStack;
            this.prices = priceMap;
        }
    }
}
