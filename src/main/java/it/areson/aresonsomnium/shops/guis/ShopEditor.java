package it.areson.aresonsomnium.shops.guis;

import it.areson.aresonsomnium.shops.items.ShopItem;
import it.areson.aresonsomnium.utils.PlayerComparator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.TreeMap;

public class ShopEditor {

    private final TreeMap<Player, ShopItem> pickupItems;
    private Inventory pricesInventory;

    public ShopEditor() {
        this.pickupItems = new TreeMap<>(new PlayerComparator());
    }

    public void addNewItemToShop(CustomShop shop, int slot, ShopItem shopItem) {
        shop.getItems().put(slot, shopItem);
    }

    public void removeItemFromShop(CustomShop shop, int slot) {
        shop.getItems().remove(slot);
    }

    public void setPickupItems(Player player, ShopItem shopItem) {
        pickupItems.put(player, shopItem);
    }

    public ShopItem getPickupItem(Player player) {
        return pickupItems.remove(player);
    }

    public Inventory getPricesInventory() {
        if (Objects.isNull(pricesInventory)) {
            ItemStack itemStackBasic = new ItemStack(Material.IRON_NUGGET);
            setItemDisplayName(itemStackBasic, "Monete Base");

            ItemStack itemStackCharon = new ItemStack(Material.SUNFLOWER);
            setItemDisplayName(itemStackCharon, "Monete di Caronte");

            ItemStack itemStackForced = new ItemStack(Material.EMERALD);
            setItemDisplayName(itemStackForced, "Monete Forzate");

            pricesInventory = Bukkit.createInventory(null, InventoryType.CHEST, "Seleziona la moneta");
            pricesInventory.setItem(11, itemStackBasic);
            pricesInventory.setItem(13, itemStackCharon);
            pricesInventory.setItem(15, itemStackForced);
        }
        return pricesInventory;
    }

    public void setItemDisplayName(ItemStack itemStack, String name) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (Objects.nonNull(itemMeta)) {
            itemMeta.setDisplayName(name);
            itemStack.setItemMeta(itemMeta);
        }
    }

}
