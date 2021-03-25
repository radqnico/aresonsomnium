package it.areson.aresonsomnium.economy.shops.newsystem;

import it.areson.aresonsomnium.api.AresonSomniumAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemListView {

    private final List<ShopItem> items;
    private final ItemsGateway itemsGateway;
    private final List<Inventory> inventories;

    public ItemListView(ItemsGateway itemsGateway) {
        this.inventories = new ArrayList<>();
        this.items = new ArrayList<>();
        this.itemsGateway = itemsGateway;
        refreshInventories();
    }

    public void refreshInventories() {
        items.clear();
        List<ShopItem> allItems = itemsGateway.getAllItems(true);
        items.addAll(allItems);
        int neededInventories = (items.size() / 54) + 1;
        for (int i = 0; i < neededInventories; i++) {
            Inventory inventory = Bukkit.createInventory(null, 54, Component.text("Lista oggetti shop | PAGINA " + i).decorate(TextDecoration.BOLD));
            for (int j = 0; j < 54; j++) {
                int index = (54 * i) + j;
                if (items.size() > index) {
                    inventory.addItem(items.get(index).getItemStack());
                }
            }
            inventories.add(inventory);
        }
    }

    public Optional<Inventory> getPage(int page) {
        try {
            return Optional.of(inventories.get(page));
        } catch (IndexOutOfBoundsException ignored) {
            AresonSomniumAPI.instance.getLogger().warning("ItemListView pagina " + page + " non esiste");
        }
        return Optional.empty();
    }

    public boolean isInventoryOfView(Inventory inventory) {
        return inventory != null && inventories.parallelStream().anyMatch(inventory1 -> inventory1.equals(inventory));
    }

    public Optional<ShopItem> getShopItem(int page, int slot) {
        int index = (54 * page) + slot;
        if (items.size() > index) {
            return Optional.of(items.get(index));
        }
        return Optional.empty();
    }

}
