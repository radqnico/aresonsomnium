package it.areson.aresonsomnium.economy.shops.listener;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.economy.shops.guis.*;
import it.areson.aresonsomnium.economy.shops.items.Price;
import it.areson.aresonsomnium.economy.shops.items.ShopItem;
import it.areson.aresonsomnium.utils.MessageUtils;
import elements.Pair;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        aresonSomnium.getShopEditor().endEditGui(player);
        aresonSomnium.getShopEditor().endEditPrice(player);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null) {
            if (shopEditor.isEditingCustomGui(player)) {
                // Editing
                CustomShop editingCustomShop = shopEditor.getEditingCustomShop(player);
                if (shopEditor.isEditingPrice(player)) {
                    switchPriceAction(player, event);
                    aresonSomnium.getDebugger().debugInfo("Price Action");
                } else {
                    switchEditingAction(player, editingCustomShop, event);
                    aresonSomnium.getDebugger().debugInfo("Edit Action");
                }
            } else if (shopManager.isViewingCustomGui(player)) {
                // Shopping
                switchUserAction(player, event);
                event.setCancelled(true);
            }
        }
    }

    private void switchUserAction(Player player, InventoryClickEvent event) {
        switch (event.getAction()) {
            case PICKUP_ALL:
                prepareBuyItem(player, event);
                break;
            case PICKUP_HALF:
                prepareSellItem(player, event);
                break;
            default:
                event.setCancelled(true);
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
                if (Objects.nonNull(involvedItem) && (event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.PLAYER))) {
                    MoveShopItemAction moveShopItemAction = shopEditor.beginMoveItemAction(player);
                    moveShopItemAction.setSource(Pair.of(event.getClickedInventory(), event.getSlot()));
                    moveShopItemAction.setItem(involvedItem);
                }
                if (Objects.nonNull(involvedItem) && event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
                    EditPriceConfig editPriceConfig = shopEditor.newEditPrice(player, customShop, false);
                    player.openInventory(shopEditor.getPricesInventory(false));
                    editPriceConfig.setSlot(event.getSlot());
                }
                break;
            case CLONE_STACK:
                if (Objects.nonNull(involvedItem) && (event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.PLAYER))) {
                    MoveShopItemAction moveShopItemAction = shopEditor.beginMoveItemAction(player);
                    moveShopItemAction.setSource(Pair.of(event.getClickedInventory(), event.getSlot()));
                    moveShopItemAction.setItem(involvedItem);
                }
                if (Objects.nonNull(involvedItem) && event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
                    EditPriceConfig editPriceConfig = shopEditor.newEditPrice(player, customShop, true);
                    player.openInventory(shopEditor.getPricesInventory(true));
                    editPriceConfig.setSlot(event.getSlot());
                }
                break;
            default:
                event.setCancelled(true);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void switchPriceAction(Player player, InventoryClickEvent event) {
        if (InventoryType.CHEST.equals(event.getClickedInventory().getType())) {
            int slot = event.getSlot();
            EditPriceConfig editingPriceConfig = shopEditor.getEditingPriceConfig(player);
            if (Objects.nonNull(editingPriceConfig)) {
                switch (slot) {
                    case 11:
                        editingPriceConfig.setCoinType(CoinType.MONETE);
                        player.closeInventory();
                        player.sendMessage(MessageUtils.warningMessage(" --> Inserisci Monete <--"));
                        break;
                    case 13:
                        editingPriceConfig.setCoinType(CoinType.OBOLI);
                        player.closeInventory();
                        player.sendMessage(MessageUtils.warningMessage(" --> Inserisci Oboli <--"));
                        break;
                    case 15:
                        editingPriceConfig.setCoinType(CoinType.GEMME);
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

    @SuppressWarnings("ConstantConditions")
    private void prepareBuyItem(Player player, InventoryClickEvent event) {
        if (event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
            if (event.isLeftClick()) {
                CustomShop shop = shopManager.getViewingCustomShop(player);
                ShopItem shopItem = shop.getItems().get(event.getSlot());
                if (Objects.nonNull(shopItem)) {
                    buyItem(player, shopItem);
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void prepareSellItem(Player player, InventoryClickEvent event) {
        if (event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
            if (event.isRightClick()) {
                CustomShop shop = shopManager.getViewingCustomShop(player);
                ShopItem shopItem = shop.getItems().get(event.getSlot());
                if (Objects.nonNull(shopItem)) {
                    sellItem(player, shopItem);
                }
            }
        }
    }

    private boolean checkIfEnchantsAreEqual(ItemStack itemStack1, ItemStack itemStack2) {
        EnchantmentStorageMeta bookMeta1 = (EnchantmentStorageMeta) itemStack1.getItemMeta();
        EnchantmentStorageMeta bookMeta2 = (EnchantmentStorageMeta) itemStack2.getItemMeta();
        if (bookMeta1 != null && bookMeta2 != null) {
            Map<Enchantment, Integer> storedEnchants1 = bookMeta1.getStoredEnchants();
            Map<Enchantment, Integer> storedEnchants2 = bookMeta2.getStoredEnchants();

            for (Map.Entry<Enchantment, Integer> entry1 : storedEnchants1.entrySet()) {
                boolean isThereEnchant = false;
                for (Map.Entry<Enchantment, Integer> entry2 : storedEnchants2.entrySet()) {
                    Enchantment key1 = entry1.getKey();
                    Enchantment key2 = entry2.getKey();
                    boolean keyEquals = key1.getKey().equals(key2.getKey());
                    boolean valueEquals = entry1.getValue().equals(entry2.getValue());
                    if (keyEquals && valueEquals) {
                        isThereEnchant = true;
                        break;
                    }
                }
                if (!isThereEnchant) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private void sellItem(Player player, ShopItem shopItem) {
        SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
        if (Objects.nonNull(somniumPlayer)) {
            Price price = shopItem.getSellingPrice();
            ItemStack shopItemStack = shopItem.getItemStack();
            Material sellItemType = shopItem.getItemStack().getType();
            if (shopItem.isSellable()) {
                if (player.getInventory().contains(shopItemStack.getType())) {

                    long totalAmountOfItem = Arrays.stream(player.getInventory().getContents()).parallel()
                            .reduce(0, (integer, itemStack) -> {
                                if (itemStack != null && itemStack.getType().equals(sellItemType)) {
                                    return integer + (itemStack.getAmount());
                                }
                                return integer;
                            }, Integer::sum);
                    aresonSomnium.getLogger().info("Total: " + totalAmountOfItem);

                    if (totalAmountOfItem >= shopItemStack.getAmount()) {
                        int toRemove = shopItemStack.getAmount();
                        while (toRemove > 0) {
                            Optional<ItemStack> first = Arrays.stream(player.getInventory().getContents()).parallel()
                                    .filter(itemStack -> itemStack != null && itemStack.getType().equals(sellItemType))
                                    .findFirst();
                            if (first.isPresent()) {
                                ItemStack itemStack = first.get();
                                price.addTo(somniumPlayer);
                                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage(
                                        "item-sell-success",
                                        Pair.of("%coins%", price.getCoins().toString()),
                                        Pair.of("%obols%", price.getObols().toString()),
                                        Pair.of("%gems%", price.getGems().toString())
                                ));
                                if (toRemove > itemStack.getAmount()) {
                                    toRemove -= itemStack.getAmount();
                                    itemStack.setAmount(0);
                                } else {
                                    itemStack.setAmount(itemStack.getAmount() - toRemove);
                                    toRemove = 0;
                                }
                            } else {
                                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("item-sell-not-enough"));
                            }
                        }
                    } else {
                        player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("item-sell-not-enough"));
                    }
                } else {
                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("item-sell-not-present"));
                }
            } else {
                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("item-sell-not-sellable"));
            }
        } else {
            player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("item-sell-error"));
        }
    }

    private void buyItem(Player player, ShopItem shopItem) {
        SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(player);
        if (Objects.nonNull(somniumPlayer)) {
            Price price = shopItem.getShoppingPrice();
            if (somniumPlayer.canAfford(price)) {
                if (player.getInventory().addItem(new ItemStack(shopItem.getItemStack())).isEmpty()) {
                    price.removeFrom(somniumPlayer);
                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage(
                            "item-buy-success",
                            Pair.of("%coins%", price.getCoins().toString()),
                            Pair.of("%obols%", price.getObols().toString()),
                            Pair.of("%gems%", price.getGems().toString())
                    ));
                } else {
                    player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("item-buy-not-enough-space"));
                }
            } else {
                player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("item-buy-not-enough-money"));
            }
        } else {
            player.sendMessage(aresonSomnium.getMessageManager().getPlainMessage("item-buy-error"));
        }
    }

}
