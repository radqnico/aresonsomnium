package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.players.SomniumPlayerManager;
import it.areson.aresonsomnium.shops.CustomShop;
import it.areson.aresonsomnium.shops.ShopManager;
import it.areson.aresonsomnium.utils.MessageUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CustomGuiEventsListener extends GeneralEventListener {

    private final ShopManager shopManager;

    public CustomGuiEventsListener(AresonSomnium aresonSomnium, ShopManager shopManager) {
        super(aresonSomnium);
        this.shopManager = shopManager;
    }

    private void sendCopyMessage(Player player, String pricesJSON, String indexAndNameJSON) {
        TextComponent textComponent = new TextComponent("-------------------\nClicca questo messaggio per copiare\nOggetti:\n");
        textComponent.addExtra(MessageUtils.successMessage(indexAndNameJSON));
        textComponent.addExtra("\n\nPrezzi:\n");
        textComponent.addExtra(MessageUtils.successMessage(pricesJSON));
        textComponent.addExtra("\n\nCopia e incolla solo la parte dei prezzi con\n/somniumadmin setShopPrices <nomeShop> <jsonPrezzi>\n-------------------");
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, indexAndNameJSON + "\n\n-------------\n\n" + pricesJSON));
        player.spigot().sendMessage(textComponent);
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        Player player = (Player) event.getView().getPlayer();
        if (shopManager.isEditingCustomGui(player)) {
            CustomShop editingCustomShop = shopManager.getEditingCustomShop(player);
            if (shopManager.endEditGui(player, event.getInventory())) {
                aresonSomnium.getLogger().info(MessageUtils.successMessage("GUI modificata da '" + player.getName() + "' salvata su DB"));
                String pricesJSON = editingCustomShop.getPricesJSON();
                String indexAndNameJSON = editingCustomShop.getIndexAndNameJSON();
                sendCopyMessage(player, pricesJSON, indexAndNameJSON);
            } else {
                aresonSomnium.getLogger().info(MessageUtils.warningMessage("GUI modificata da '" + player.getName() + "' NON salvata DB"));
            }
        } else if (shopManager.isViewingCustomGui(player)) {
            shopManager.playerCloseGui(player);
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ShopManager shopManager = aresonSomnium.getShopManager();
        if (shopManager.isViewingCustomGui(player)) {
            Inventory clickedInventory = event.getClickedInventory();
            CustomShop customShop = shopManager.getViewingCustomShop(player);
            if (Objects.nonNull(clickedInventory)) {
                if (clickedInventory.getType().equals(InventoryType.CHEST)) {
                    ItemStack currentItem = event.getCurrentItem();
                    if (Objects.nonNull(currentItem) && !currentItem.getType().equals(Material.AIR)) {
                        switch (event.getClick()) {
                            case LEFT:
                                int slot = event.getSlot();
                                Float price = customShop.getPriceOfSlot(slot);
                                if (Objects.nonNull(price)) {
                                    handleBuyItem(player, CoinType.BASIC, price, currentItem);
                                } else {
                                    player.sendMessage(MessageUtils.errorMessage("Il prezzo di questo oggetto non e' valido. Segnala il problema allo Staff."));
                                }
                                break;
                        }
                    }
                }
            }
            event.setCancelled(true);
        }
    }

    private void handleBuyItem(Player player, CoinType coinType, Float price, ItemStack itemStack) {
        SomniumPlayerManager somniumPlayerManager = aresonSomnium.getSomniumPlayerManager();
        SomniumPlayer somniumPlayer = somniumPlayerManager.getSomniumPlayer(player);
        if (somniumPlayer.canAfford(coinType, price)) {
            if (player.getInventory().addItem(itemStack.clone()).isEmpty()) {
                somniumPlayer.changeCoins(coinType, -price);
                player.sendMessage(MessageUtils.successMessage("Oggetto acquistato. Ora hai " + somniumPlayer.getWallet().getBasicCoins() + " " + coinType.getCoinName() + " Coins."));
            } else {
                player.sendMessage(MessageUtils.errorMessage("Non hai abbastanza spazio nell'inventario."));
            }
            player.closeInventory();
        } else {
            player.sendMessage(MessageUtils.errorMessage("Non hai abbastanza " + coinType.getCoinName() + " Coins."));
        }
    }

}
