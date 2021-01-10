package it.areson.aresonsomnium.shops.listener;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.shops.guis.*;
import it.areson.aresonsomnium.shops.items.Price;
import it.areson.aresonsomnium.shops.items.ShopItem;
import it.areson.aresonsomnium.utils.MessageUtils;
import it.areson.aresonsomnium.utils.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
        Inventory clickedInventory = event.getClickedInventory();
        if (Objects.nonNull(clickedInventory)) {
            if (shopEditor.isEditingCustomGui(player)) {
                // Editing
                CustomShop editingCustomShop = shopEditor.getEditingCustomShop(player);
                if (shopEditor.isEditingPrice(player)) {
                    switchPriceAction(player, event);
                } else {
                    switchEditingAction(player, editingCustomShop, event);
                }
            } else if (shopManager.isViewingCustomGui(player)) {
                // Shopping
            }
        }
    }

    private void switchEditingAction(Player player, CustomShop customShop, InventoryClickEvent event) {
        ItemStack involvedItem = getInvolvedItem(event);
        switch (event.getAction()) {
            case PICKUP_ALL:
                if (Objects.nonNull(involvedItem)) {
                    MoveShopItemAction moveShopItemAction = shopEditor.beginMoveItemAction(player);
                    moveShopItemAction.setSource(Pair.of(event.getClickedInventory(), event.getSlot()));
                    moveShopItemAction.setItem(involvedItem);
                }
                break;
            case PLACE_ALL:
                if (Objects.nonNull(involvedItem)) {
                    MoveShopItemAction moveShopItemAction = shopEditor.getMoveItemAction(player);
                    moveShopItemAction.setDestination(Pair.of(event.getClickedInventory(), event.getSlot()));
                    shopEditor.endMoveItemAction(player, customShop);
                }
                break;
            case PICKUP_HALF:
                if (Objects.nonNull(involvedItem)) {
                    EditPriceConfig editPriceConfig = shopEditor.newEditPrice(player, customShop);
                    player.openInventory(shopEditor.getPricesInventory());
                    editPriceConfig.setSlot(event.getSlot());
                }
                break;
            default:
                event.setCancelled(true);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void switchPriceAction(Player player, InventoryClickEvent event) {
        if(InventoryType.CHEST.equals(event.getClickedInventory().getType())){
            int slot = event.getSlot();
            EditPriceConfig editingPriceConfig = shopEditor.getEditingPriceConfig(player);
            if(Objects.nonNull(editingPriceConfig)) {
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
            } else {
                aresonSomnium.getDebugger().debugError("Errore: EditPriceConfig non trovato");
            }
        }
        event.setCancelled(true);
    }

    private boolean checkItem(org.bukkit.inventory.ItemStack itemStack) {
        return Objects.nonNull(itemStack) && !itemStack.getType().equals(Material.AIR);
    }

    private org.bukkit.inventory.ItemStack getInvolvedItem(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (Objects.nonNull(currentItem) && checkItem(currentItem)) {
            return new ItemStack(currentItem);
        }
        ItemStack cursor = event.getCursor();
        if (Objects.nonNull(cursor) && checkItem(cursor)) {
            return new ItemStack(cursor);
        }
        return null;
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
                if (player.getInventory().addItem(new org.bukkit.inventory.ItemStack(shopItem.getItemStack())).isEmpty()) {
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

}
