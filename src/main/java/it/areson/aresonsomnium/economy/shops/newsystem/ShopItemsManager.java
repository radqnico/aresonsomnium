package it.areson.aresonsomnium.economy.shops.newsystem;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.database.MySqlConfig;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Optional;

public class ShopItemsManager {

    private AresonSomnium aresonSomnium;
    private ItemsGateway itemsGateway;
    private ItemListView itemListView;
    private ItemListViewEventsListener itemListViewEventsListener;
    private HashSet<String> playerWithEditorOpened;

    public ShopItemsManager(AresonSomnium aresonSomnium, MySqlDBConnection mySqlDBConnection) {
        itemsGateway = new ItemsGateway(mySqlDBConnection, MySqlConfig.ITEMS_TABLE_NAME);
        itemListView = new ItemListView(itemsGateway);
        playerWithEditorOpened = new HashSet<>();
        itemListViewEventsListener = new ItemListViewEventsListener(aresonSomnium);
    }

    public void openEditGuiToPlayer(Player player, int page) {
        Optional<Inventory> inventoryOptional = itemListView.getPage(page);
        if (inventoryOptional.isPresent()) {
            player.openInventory(inventoryOptional.get());
            if (playerWithEditorOpened.isEmpty()) {
                itemListViewEventsListener.registerEvents();
            }
            playerWithEditorOpened.add(player.getName());
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

    public boolean checkIfPlayerClickedInItemsEditor(Player player, Inventory inventory) {
        return playerWithEditorOpened.contains(player.getName()) && itemListView.isInventoryOfView(inventory);
    }

    public ItemsGateway getItemsGateway() {
        return itemsGateway;
    }

    public ItemListView getItemListView() {
        return itemListView;
    }
}
