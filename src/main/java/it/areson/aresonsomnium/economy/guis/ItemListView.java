package it.areson.aresonsomnium.economy.guis;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.items.ItemsDBGateway;
import it.areson.aresonsomnium.economy.items.ShopItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class ItemListView {

    private final AresonSomnium aresonSomnium;
    private final ItemsDBGateway itemsDBGateway;
    private final List<Inventory> inventories;

    public ItemListView(AresonSomnium aresonSomnium, ItemsDBGateway itemsDBGateway) {
        this.aresonSomnium = aresonSomnium;
        this.inventories = new ArrayList<>();
        this.itemsDBGateway = itemsDBGateway;
        refreshInventories();
    }

    public void refreshInventories() {
        List<ShopItem> items = itemsDBGateway.getAllItems(true);
        int neededInventories = (items.size() / 54) + 1;
        if (neededInventories < inventories.size()) {
            ArrayList<Inventory> clone = new ArrayList<>(this.inventories.subList(0, neededInventories));
            inventories.clear();
            inventories.addAll(clone);
        }
        for (int i = 0; i < neededInventories; i++) {
            Inventory inventory;
            if (inventories.size() > i) {
                inventory = inventories.get(i);
                inventory.clear();
            } else {
                inventory = Bukkit.createInventory(null, 54, Component.text("Lista oggetti | PAG " + (i + 1)).color(RED));
            }
            for (int j = 0; j < 54; j++) {
                int index = (54 * i) + j;
                if (items.size() > index) {
                    inventory.addItem(items.get(index).getItemStack(true, true));
                }
            }
            if (inventories.size() <= i) {
                inventories.add(inventory);
            }
        }
    }

    public Optional<Inventory> getPage(int page) {
        try {
            return Optional.of(inventories.get(page));
        } catch (IndexOutOfBoundsException ignored) {
            aresonSomnium.getLogger().warning("ItemListView pagina " + page + " non esiste");
        }
        return Optional.empty();
    }

    public boolean isInventoryOfView(Inventory inventory) {
        return inventory != null && inventories.parallelStream().anyMatch(inventory1 -> inventory1.equals(inventory));
    }

    public Optional<ShopItem> getShopItem(int page, int slot) {
        List<ShopItem> items = itemsDBGateway.getAllItems(false);
        int index = (54 * page) + slot;
        if (items.size() > index) {
            return Optional.of(items.get(index));
        }
        return Optional.empty();
    }

    public int getNumberOfPages() {
        return inventories.size();
    }

}
