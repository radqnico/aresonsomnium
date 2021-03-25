package it.areson.aresonsomnium.economy.items;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.database.MySqlConfig;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.economy.guis.OpClickShopItemWithPanelOpenedEventsListener;
import it.areson.aresonsomnium.economy.guis.ItemListView;
import it.areson.aresonsomnium.economy.guis.ItemListViewEventsListener;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Optional;

public class ShopItemsManager {

    private final AresonSomnium aresonSomnium;
    private final ItemsDBGateway itemsDBGateway;
    private final ItemListView itemListView;
    private final HashMap<String, Integer> playerWithEditorOpened;
    // Listeners
    private final ItemListViewEventsListener itemListViewEventsListener;
    private final OpClickShopItemWithPanelOpenedEventsListener opClickShopItemWithPanelOpenedEventsListener;

    public ShopItemsManager(AresonSomnium aresonSomnium, MySqlDBConnection mySqlDBConnection) {
        this.aresonSomnium = aresonSomnium;
        itemsDBGateway = new ItemsDBGateway(mySqlDBConnection, MySqlConfig.ITEMS_TABLE_NAME);
        itemListView = new ItemListView(itemsDBGateway);
        playerWithEditorOpened = new HashMap<>();
        itemListViewEventsListener = new ItemListViewEventsListener(aresonSomnium);

        opClickShopItemWithPanelOpenedEventsListener = new OpClickShopItemWithPanelOpenedEventsListener(aresonSomnium);
        opClickShopItemWithPanelOpenedEventsListener.registerEvents();
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
        ShopItem shopItem = new ShopItem(-1, itemStack, itemStack.getAmount(), Price.zero(), Price.zero());
        itemsDBGateway.upsertShopItem(shopItem);
        reloadItems();
    }

    public void deleteItemInEditor(Player player, int slot) {
        int page = playerWithEditorOpened.get(player.getName());
        Optional<ShopItem> shopItemOptional = itemListView.getShopItem(page, slot);
        shopItemOptional.ifPresent(shopItem -> {
            itemsDBGateway.removeItem(shopItem.getId());
            reloadItems();
        });
    }

    public void itemClickedInEditor(Player player, int slot) {
        int page = playerWithEditorOpened.get(player.getName());
        Optional<ShopItem> shopItemOptional = itemListView.getShopItem(page, slot);
        shopItemOptional.ifPresent(shopItem -> {
            ItemStack clone = shopItem.getItemStack(true);
            System.out.println("ID FROM ITEM: " + ShopItem.getIdFromItem(clone));
            player.getInventory().addItem(clone);
        });
    }

    public void reloadItems() {
        itemListView.refreshInventories();
    }

    public ItemsDBGateway getItemsGateway() {
        return itemsDBGateway;
    }

    public ItemListView getItemListView() {
        return itemListView;
    }
}
