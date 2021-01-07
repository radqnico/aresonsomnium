package it.areson.aresonsomnium.shops.guis;

import it.areson.aresonsomnium.AresonSomnium;
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
    private final TreeMap<Player, String> editingGuis;
    private final TreeMap<Player, EditPriceConfig> activePriceConfigs;
    private final AresonSomnium aresonSomnium;
    private Inventory pricesInventory;

    public ShopEditor(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;

        PlayerComparator playerComparator = new PlayerComparator();
        this.pickupItems = new TreeMap<>(playerComparator);
        this.editingGuis = new TreeMap<>(playerComparator);
        this.activePriceConfigs = new TreeMap<>(playerComparator);
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

    public void beginEditGui(Player player, String guiName) {
        editingGuis.put(player, guiName);
    }

    public boolean endEditGui(Player player) {
        return Objects.nonNull(editingGuis.remove(player));
    }

    public CustomShop getEditingCustomShop(Player player) {
        if (isEditingCustomGui(player)) {
            return aresonSomnium.getShopManager().getGuis().get(editingGuis.get(player));
        }
        return null;
    }

    public boolean isEditingCustomGui(Player player) {
        return editingGuis.containsKey(player);
    }

    public EditPriceConfig newEditPrice(Player player, CustomShop customShop) {
        EditPriceConfig editPriceConfig = new EditPriceConfig(customShop);
        activePriceConfigs.put(player, editPriceConfig);
        return editPriceConfig;
    }

    public EditPriceConfig getEditingPriceConfig(Player player) {
        return activePriceConfigs.get(player);
    }

    public boolean isEditingPrice(Player player) {
        return activePriceConfigs.containsKey(player);
    }

    public void endEditPrice(Player player) {
        activePriceConfigs.remove(player);
    }

}
