package it.areson.aresonsomnium.shops.guis;

import it.areson.aresonsomnium.shops.items.ShopItem;
import it.areson.aresonsomnium.utils.PlayerComparator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.TreeMap;

public class ShopEditor {

    private static Inventory pricesInventory;

    private static final TreeMap<Player, ShopItem> pickupItems = new TreeMap<>(new PlayerComparator());

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

    public static Inventory getPricesInventory() {
        if(Objects.isNull(pricesInventory)) {
            pricesInventory = Bukkit.createInventory(null, InventoryType.CHEST, "Seleziona la moneta");
            pricesInventory.setItem(13, new ItemStack(Material.IRON_NUGGET));
            pricesInventory.setItem(15, new ItemStack(Material.SUNFLOWER));
            pricesInventory.setItem(17, new ItemStack(Material.EMERALD));
        }
        return pricesInventory;
    }
}
