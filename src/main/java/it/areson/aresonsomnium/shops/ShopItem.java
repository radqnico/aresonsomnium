package it.areson.aresonsomnium.shops;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import it.areson.aresonsomnium.economy.CoinType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    public String toJson() {
        byte[] bytes = serializeAsBytes();
        String itemStackString = Base64.getEncoder().encodeToString(bytes);
        Map<String, Float> map = priceMap.entrySet().parallelStream().collect(Collectors.toMap(
                e -> e.getKey().name(),
                Map.Entry::getValue
        ));
        String priceMapJson = new Gson().toJson(map);
        SerializedShopItem serializedShopItem = new SerializedShopItem(itemStackString, priceMapJson);
        return new Gson().toJson(serializedShopItem);
    }

    public static class SerializedShopItem {
        @Expose
        private final String serializedItemStack;
        @Expose
        private final String priceMapJson;

        public SerializedShopItem(String serializedItemStack, String priceMapJson) {
            this.serializedItemStack = serializedItemStack;
            this.priceMapJson = priceMapJson;
        }
    }
}
