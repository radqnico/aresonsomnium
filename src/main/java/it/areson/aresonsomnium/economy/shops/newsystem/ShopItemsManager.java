package it.areson.aresonsomnium.economy.shops.newsystem;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.database.MySqlConfig;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShopItemsManager {

    private final AresonSomnium aresonSomnium;
    private final ItemsGateway itemsGateway;
    private final ItemListView itemListView;
    private final ItemListViewEventsListener itemListViewEventsListener;
    private final HashMap<String, Integer> playerWithEditorOpened;

    public ShopItemsManager(AresonSomnium aresonSomnium, MySqlDBConnection mySqlDBConnection) {
        this.aresonSomnium = aresonSomnium;
        itemsGateway = new ItemsGateway(mySqlDBConnection, MySqlConfig.ITEMS_TABLE_NAME);
        itemListView = new ItemListView(itemsGateway);
        playerWithEditorOpened = new HashMap<>();
        itemListViewEventsListener = new ItemListViewEventsListener(aresonSomnium);
    }

    public void openEditGuiToPlayer(Player player, int page) {
        Optional<Inventory> inventoryOptional = itemListView.getPage(page);
        if (inventoryOptional.isPresent()) {
            player.openInventory(inventoryOptional.get());
            if (playerWithEditorOpened.isEmpty()) {
                itemListViewEventsListener.registerEvents();
            }
            playerWithEditorOpened.put(player.getName(), page);
        } else {
            player.sendMessage("La Pagina " + (page + 1) + " non esiste.");
        }
    }

    public void playerClosedEditGui(Player player) {
        playerWithEditorOpened.remove(player.getName());
        if (playerWithEditorOpened.isEmpty()) {
            itemListViewEventsListener.unregisterEvents();
        }
    }

    public boolean checkIfIsItemsEditor(Player player, Inventory inventory) {
        return playerWithEditorOpened.containsKey(player.getName()) && itemListView.isInventoryOfView(inventory);
    }

    public boolean checkIfPlayerOpenedEditGui(Player player, Inventory inventory) {
        return playerWithEditorOpened.containsKey(player.getName()) && itemListView.isInventoryOfView(inventory);
    }

    public void itemPutIntoEditor(ItemStack itemStack) {
        aresonSomnium.getLogger().info("New item: " + itemStack.getType().name());
        ShopItem shopItem = new ShopItem(-1, itemStack, itemStack.getAmount(), Price.zero(), Price.zero());
        itemsGateway.insertItem(shopItem);
        reloadItems();
    }

    public void deleteItemInEditor(Player player, int slot) {
        int page = playerWithEditorOpened.get(player.getName());
        Optional<ShopItem> shopItemOptional = itemListView.getShopItem(page, slot);
        shopItemOptional.ifPresent(shopItem -> {
            itemsGateway.removeItem(shopItem.getId());
            reloadItems();
        });
    }

    public void itemClickedInEditor(Player player, int slot) {
        int page = playerWithEditorOpened.get(player.getName());
        Optional<ShopItem> shopItemOptional = itemListView.getShopItem(page, slot);
        shopItemOptional.ifPresent(shopItem -> {
            aresonSomnium.getLogger().info("ID: " + shopItem.getId());
        });
    }

    public void reloadItems() {
        for (Map.Entry<String, Integer> entry : playerWithEditorOpened.entrySet()) {
            Player player = aresonSomnium.getServer().getPlayer(entry.getKey());
            if (player != null) {
                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            }
        }
        itemListView.refreshInventories();
        for (Map.Entry<String, Integer> entry : playerWithEditorOpened.entrySet()) {
            Player player = aresonSomnium.getServer().getPlayer(entry.getKey());
            if (player != null) {
                openEditGuiToPlayer(player, entry.getValue());
            }
        }
    }

    public ItemsGateway getItemsGateway() {
        return itemsGateway;
    }

    public ItemListView getItemListView() {
        return itemListView;
    }
}
