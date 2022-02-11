package it.areson.aresonsomnium.economy.items;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.economy.guis.ItemListView;
import it.areson.aresonsomnium.economy.guis.ItemListViewEventsListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ShopItemsManager {

    private final AresonSomnium aresonSomnium;
    private final ItemsDBGateway itemsDBGateway;
    private final ItemListView itemListView;
    private final HashMap<String, Integer> playerWithEditorOpened;
    // Listeners
    private final ItemListViewEventsListener itemListViewEventsListener;

    public ShopItemsManager(AresonSomnium aresonSomnium, MySqlDBConnection mySqlDBConnection) {
        this.aresonSomnium = aresonSomnium;
        itemsDBGateway = new ItemsDBGateway(aresonSomnium, mySqlDBConnection);
        itemListView = new ItemListView(aresonSomnium, itemsDBGateway);
        playerWithEditorOpened = new HashMap<>();
        //TODO Andrebbe generalizzato e spostato tra i listener
        itemListViewEventsListener = new ItemListViewEventsListener(aresonSomnium, this);
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
        ShopItem shopItem = new ShopItem(aresonSomnium, -1, itemStack, itemStack.getAmount(), Price.zero(), Price.zero());
        itemsDBGateway.upsertShopItem(shopItem);
        reloadItems();
    }

    public void deleteItemInEditor(Player player, int slot) {
        runIfShopItemIsPresent(player, slot, shopItem -> {
            itemsDBGateway.removeItem(shopItem.getId());
            reloadItems();
        });
    }

    public void itemClickedInEditor(Player player, int slot) {
        runIfShopItemIsPresent(player, slot, shopItem -> {
            ItemStack clone = shopItem.getItemStack(true, true);
            player.getInventory().addItem(clone);
        });
    }

    public void sendPriceEditMessage(Player player, int slot, boolean isShoppingPrice) {
        runIfShopItemIsPresent(player, slot, shopItem -> {
            String initialCommand = "/shopadmin setitemprice" + (isShoppingPrice ? " buy " : " sell ");
            String buyOrSell = isShoppingPrice ? "ACQUISTO" : "VENDITA";
            TextComponent start = Component.text("Imposta il prezzo di " + buyOrSell + " dell'oggetto con ID " + shopItem.getId()).color(BLUE).decoration(TextDecoration.BOLD, true);
            TextComponent message = start
                    .append(Component.newline())
                    .append(Component.text("[MONETE]  ")
                            .color(YELLOW)
                            .decoration(TextDecoration.BOLD, true)
                            .clickEvent(ClickEvent.suggestCommand(initialCommand + shopItem.getId() + " monete "))
                            .hoverEvent(HoverEvent.showText(Component.text("Imposta le monete"))))
                    .append(Component.text("[OBOLI]  ")
                            .color(GOLD)
                            .decoration(TextDecoration.BOLD, true)
                            .clickEvent(ClickEvent.suggestCommand(initialCommand + shopItem.getId() + " oboli "))
                            .hoverEvent(HoverEvent.showText(Component.text("Imposta gli oboli"))))
                    .append(Component.text("[GEMME]")
                            .color(GREEN)
                            .decoration(TextDecoration.BOLD, true)
                            .clickEvent(ClickEvent.suggestCommand(initialCommand + shopItem.getId() + " gemme "))
                            .hoverEvent(HoverEvent.showText(Component.text("Imposta le gemme"))))
                    .append(Component.newline());
            player.sendMessage(message);
            player.closeInventory();
        });
    }

    private void runIfShopItemIsPresent(Player player, int slot, Consumer<? super ShopItem> consumer) {
        int page = playerWithEditorOpened.get(player.getName());
        Optional<ShopItem> shopItemOptional = itemListView.getShopItem(page, slot);
        shopItemOptional.ifPresent(consumer);
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
