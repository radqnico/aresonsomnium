package it.areson.aresonsomnium.economy.shops.newsystem;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.database.MySqlConfig;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
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
            player.sendMessage("La Pagina " + page + " non esiste.");
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
        aresonSomnium.getLogger().info("New item");

    }

    public void itemClickedInEditor(Player player, int slot) {
        System.out.println(aresonSomnium);
        System.out.println(playerWithEditorOpened.get(player.getName()));
        int page = playerWithEditorOpened.get(player.getName());
        Optional<ShopItem> shopItemOptional = itemListView.getShopItem(page, slot);
        shopItemOptional.ifPresent(shopItem -> {
            aresonSomnium.getLogger().info("ID: " + shopItem.getId());
        });
    }

    public ItemsGateway getItemsGateway() {
        return itemsGateway;
    }

    public ItemListView getItemListView() {
        return itemListView;
    }
}
