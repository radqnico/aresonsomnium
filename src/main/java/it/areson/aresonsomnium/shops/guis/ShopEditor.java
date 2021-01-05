package it.areson.aresonsomnium.shops.guis;

import it.areson.aresonsomnium.shops.items.ShopItem;
import org.bukkit.entity.Player;

import java.util.TreeMap;

public class ShopEditor {

    private static final TreeMap<Player, ShopItem> pickupItems = new TreeMap<>();

    public static void addNewItemToShop(CustomShop shop, int slot, ShopItem shopItem) {
        shop.getItems().put(slot, shopItem);
    }

    public static void removeItemFromShop(CustomShop shop, int slot) {
        shop.getItems().remove(slot);
    }

    public static void setPickupItems(Player player, ShopItem shopItem) {
        pickupItems.put(player, shopItem);
    }

    public static ShopItem getPickupItem(Player player) {
        return pickupItems.remove(player);
    }

}
