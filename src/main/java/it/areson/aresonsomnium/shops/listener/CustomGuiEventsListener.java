package it.areson.aresonsomnium.shops.listener;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.shops.guis.CustomShop;
import it.areson.aresonsomnium.shops.guis.EditPriceConfig;
import it.areson.aresonsomnium.shops.guis.ShopEditor;
import it.areson.aresonsomnium.shops.guis.ShopManager;
import it.areson.aresonsomnium.shops.items.Price;
import it.areson.aresonsomnium.shops.items.ShopItem;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;

public class CustomGuiEventsListener extends GeneralEventListener {

    private final ShopManager shopManager;
    private final ShopEditor shopEditor;

    public CustomGuiEventsListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
        this.shopManager = aresonSomnium.getShopManager();
        shopEditor = aresonSomnium.getShopEditor();
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        Player player = (Player) event.getView().getPlayer();
        if (!shopEditor.isEditingPrice(player)) {
            if (shopEditor.isEditingCustomGui(player)) {
                CustomShop customShop = shopEditor.getEditingCustomShop(player);
                if (shopEditor.endEditGui(player)) {
                    customShop.saveToDB();
                    aresonSomnium.getLogger().info(MessageUtils.successMessage("GUI modificata da '" + player.getName() + "' salvata su DB"));
                } else {
                    aresonSomnium.getLogger().info(MessageUtils.warningMessage("GUI modificata da '" + player.getName() + "' NON salvata DB"));
                }
            } else if (shopManager.isViewingCustomGui(player)) {
                shopManager.playerCloseGui(player);
            }
        }
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (shopEditor.isEditingCustomGui(player) || shopManager.isViewingCustomGui(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        Inventory clickedInventory = event.getClickedInventory();

        if (shopManager.isViewingCustomGui(player)) {
            // Click to shop
            CustomShop customShop = shopManager.getViewingCustomShop(player);
            ClickType click = event.getClick();
            prepareBuyItem(customShop, clickedInventory, click, slot, player);
            event.setCancelled(true);
        } else if (shopEditor.isEditingCustomGui(player)) {
            // Click to edit
            CustomShop customShop = shopEditor.getEditingCustomShop(player);
            InventoryAction action = event.getAction();
            if (Objects.nonNull(clickedInventory)) {
                aresonSomnium.getDebugger().debugInfo(clickedInventory.getType().name());
                if (shopEditor.isEditingPrice(player)) {
                    // Select coin type
                    EditPriceConfig editingPriceConfig = shopEditor.getEditingPriceConfig(player);
                    selectCoinType(player, clickedInventory, editingPriceConfig, slot);
                    event.setCancelled(true);
                } else {
                    // Change shop items
                    if (!changeShopItems(event, action, clickedInventory, customShop, player, slot)) {
                        event.setCancelled(true);
                    }
                }
            }
            aresonSomnium.getDebugger().debugInfo(customShop.toString());
        }
    }

    private void selectCoinType(Player player, Inventory clickedInventory, EditPriceConfig editingPriceConfig, int slot) {
        if (Objects.nonNull(clickedInventory)) {
            if (clickedInventory.getType().equals(InventoryType.CHEST)) {
                switch (slot) {
                    case 11:
                        editingPriceConfig.setCoinType(CoinType.BASIC);
                        player.closeInventory();
                        player.sendMessage(MessageUtils.warningMessage(" --> Inserisci Basic Coins <--"));
                        break;
                    case 13:
                        editingPriceConfig.setCoinType(CoinType.CHARON);
                        player.closeInventory();
                        player.sendMessage(MessageUtils.warningMessage(" --> Inserisci Monete di Caronte <--"));
                        break;
                    case 15:
                        editingPriceConfig.setCoinType(CoinType.FORCED);
                        player.closeInventory();
                        player.sendMessage(MessageUtils.warningMessage(" --> Inserisci Gemme <--"));
                        break;
                    default:
                        return;
                }
                aresonSomnium.getSetPriceInChatListener().registerEvents();
            }
        }
    }

    private boolean changeShopItems(InventoryClickEvent event, InventoryAction action, Inventory clickedInventory, CustomShop customShop, Player player, int slot) {
        switch (action) {
            case PICKUP_ALL:
                pickupItemFromShop(clickedInventory, customShop, player, slot);
                return true;
            case PLACE_ALL:
                //placeItemInShop(clickedInventory, customShop, player, slot);
                placeItemInShop(event, player);
                return true;
            case PICKUP_HALF:
                EditPriceConfig editPriceConfig = shopEditor.newEditPrice(player, customShop);
                player.openInventory(shopEditor.getPricesInventory());
                editPriceConfig.setSlot(slot);
                return true;
            default:
                return false;
        }
    }

    private void prepareBuyItem(CustomShop customShop, Inventory clickedInventory, ClickType clickType, int slot, Player player) {
        if (Objects.nonNull(clickedInventory)) {
            if (clickedInventory.getType().equals(InventoryType.CHEST)) {
                if (clickType == ClickType.LEFT) {
                    ShopItem shopItem = customShop.getItems().get(slot);
                    if (Objects.nonNull(shopItem)) {
                        buyItem(player, shopItem);
                    }
                }
            }
        }
    }

    private void buyItem(Player player, ShopItem shopItem) {
        SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
        if (Objects.nonNull(somniumPlayer)) {
            Price price = shopItem.getPrice();
            if (somniumPlayer.canAfford(price)) {
                if (player.getInventory().addItem(new ItemStack(shopItem.getItemStack())).isEmpty()) {
                    price.removeFrom(somniumPlayer);
                    player.sendMessage(MessageUtils.successMessage("Oggetto acquistato"));
                } else {
                    player.sendMessage(MessageUtils.warningMessage("Non hai abbastanza spazio nell'inventario"));
                }
            } else {
                player.sendMessage(MessageUtils.errorMessage("Non puoi permetterti questo oggetto"));
            }
        } else {
            player.sendMessage(MessageUtils.errorMessage("Riscontrato un problema con i tuoi dati. Segnala il problema  allo staff."));
        }

    }

    private void pickupItemFromShop(Inventory clickedInventory, CustomShop customShop, Player player, int slot) {
        if (Objects.nonNull(clickedInventory) && clickedInventory.getType().equals(InventoryType.CHEST)) {
            ShopItem shopItem = customShop.getItems().get(slot);
            if (Objects.nonNull(shopItem)) {
                shopEditor.setPickupItems(player, shopItem);
                shopEditor.removeItemFromShop(customShop, slot);
            }
            aresonSomnium.getDebugger().debugInfo("Oggetto rimosso");
        } else {
            // Remove saved item
            shopEditor.getPickupItem(player);
        }
    }

    private void placeItemInShop(InventoryClickEvent event, Player player) {
        System.out.println("SLOT: " + event.getSlot());
        System.out.println("CURRENT ITEM: " + event.getCurrentItem());
        System.out.println("CURSOR ITEM: " + event.getCursor());
        System.out.println("SLOT ITEM: " + event.getClickedInventory().getItem(event.getSlot()));

    }

//    private void placeItemInShop(Inventory clickedInventory, CustomShop customShop, Player player, int slot) {
//        if (Objects.nonNull(clickedInventory) && clickedInventory.getType().equals(InventoryType.CHEST)) {
//            ShopItem pickupItem = aresonSomnium.getShopEditor().getPickupItem(player);
//            if (Objects.nonNull(pickupItem)) {
//                shopEditor.addNewItemToShop(customShop, slot, pickupItem);
//                aresonSomnium.getDebugger().debugInfo("Oggetto salvato recuperato");
//            } else {
//                ItemStack currentItem = clickedInventory.getItem(slot);
//                shopEditor.addNewItemToShop(customShop, slot, new ShopItem(currentItem));
//                aresonSomnium.getDebugger().debugInfo("Oggetto nuovo inserito");
//            }
//        } else {
//            // Remove saved item and mark it as invalid
//            ShopItem pickupItem = shopEditor.getPickupItem(player);
//            ItemStack itemStack = pickupItem.getItemStack();
//            ItemMeta itemMeta = itemStack.getItemMeta();
//            if (Objects.nonNull(itemMeta)) {
//                List<String> lore = itemMeta.getLore();
//                if (Objects.nonNull(lore)) {
//                    lore.add(MessageUtils.errorMessage("NON VALIDO PER NEGOZIO"));
//                }
//                itemMeta.setLore(lore);
//            }
//            itemStack.setItemMeta(itemMeta);
//        }
//    }

}
