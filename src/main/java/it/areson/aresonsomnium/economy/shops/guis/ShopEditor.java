package it.areson.aresonsomnium.economy.shops.guis;

import it.areson.aresonsomnium.AresonSomnium;
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

    private final TreeMap<Player, MoveShopItemAction> movingItems;
    private final TreeMap<Player, String> editingGuis;
    private final TreeMap<Player, EditPriceConfig> activePriceConfigs;
    private final AresonSomnium aresonSomnium;
    private Inventory pricesInventory;

    public ShopEditor(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;

        PlayerComparator playerComparator = new PlayerComparator();
        this.movingItems = new TreeMap<>(playerComparator);
        this.editingGuis = new TreeMap<>(playerComparator);
        this.activePriceConfigs = new TreeMap<>(playerComparator);
    }

    public MoveShopItemAction beginMoveItemAction(Player player) {
        MoveShopItemAction moveShopItemAction = new MoveShopItemAction();
        movingItems.put(player, moveShopItemAction);
        return moveShopItemAction;
    }

    public MoveShopItemAction getMoveItemAction(Player player) {
        return movingItems.get(player);
    }

    public void endMoveItemAction(Player player, CustomShop customShop) {
        MoveShopItemAction remove = movingItems.remove(player);
        if (Objects.nonNull(remove)) {
            remove.executeIfValid(customShop);
        }
    }

    public Inventory getPricesInventory(boolean isSelling) {
        if (Objects.isNull(pricesInventory)) {
            ItemStack shopItemBasic = new ItemStack(Material.IRON_NUGGET);
            setItemDisplayName(shopItemBasic, "Monete");

            ItemStack shopItemObol = new ItemStack(Material.SUNFLOWER);
            setItemDisplayName(shopItemObol, "Oboli");

            ItemStack shopItemGem = new ItemStack(Material.EMERALD);
            setItemDisplayName(shopItemGem, "Gemme");

            pricesInventory = Bukkit.createInventory(null, InventoryType.CHEST, isSelling ? "Prezzo di VENDITA" : "Prezzo di ACQUISTO");
            pricesInventory.setItem(11, shopItemBasic);
            pricesInventory.setItem(13, shopItemObol);
            pricesInventory.setItem(15, shopItemGem);
        }
        return pricesInventory;
    }

    public void setItemDisplayName(ItemStack shopItem, String name) {
        ItemMeta itemMeta = shopItem.getItemMeta();
        if (Objects.nonNull(itemMeta)) {
            itemMeta.setDisplayName(name);
            shopItem.setItemMeta(itemMeta);
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

    public EditPriceConfig newEditPrice(Player player, CustomShop customShop, boolean isSelling) {
        EditPriceConfig editPriceConfig = new EditPriceConfig(customShop, isSelling);
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
