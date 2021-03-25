package it.areson.aresonsomnium.economy.shops.newsystem;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ItemEditor {

    private final ItemsDBGateway itemsDBGateway;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<ShopItem> editingShopItem;

    public ItemEditor(ItemsDBGateway itemsDBGateway) {
        this.itemsDBGateway = itemsDBGateway;
        editingShopItem = Optional.empty();
    }

    public void newShopItem(ItemStack itemStack) {
        editingShopItem = Optional.of(new ShopItem(-1, itemStack, itemStack.getAmount(), new Price(), new Price()));
    }

    public void discard() {
        editingShopItem = Optional.empty();
    }

    public boolean save() {
        if (editingShopItem.isPresent()) {
            return itemsDBGateway.insertItem(editingShopItem.get());
        } else {
            AresonSomniumAPI.instance.getLogger().warning("Tentato di salvare mentre non edita niente.");
        }
        return false;
    }

    public Optional<ShopItem> getEditingShopItem() {
        return editingShopItem;
    }
}
