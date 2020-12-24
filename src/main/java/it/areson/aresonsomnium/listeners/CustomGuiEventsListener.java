package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.shops.CustomShop;
import it.areson.aresonsomnium.shops.ShopManager;
import it.areson.aresonsomnium.utils.MessageUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Objects;

public class CustomGuiEventsListener extends GeneralEventListener {

    private final ShopManager shopManager;

    public CustomGuiEventsListener(AresonSomnium aresonSomnium, ShopManager shopManager) {
        super(aresonSomnium);
        this.shopManager = shopManager;
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
                TextComponent textComponent = new TextComponent("\nClicca questo messaggio per copiare\nOggetti:\n");
                textComponent.addExtra(MessageUtils.successMessage(indexAndNameJSON));
                textComponent.addExtra("\n\nPrezzi:\n");
                textComponent.addExtra(MessageUtils.successMessage(pricesJSON));
                textComponent.addExtra("\n\nCopia e incolla solo la parte dei prezzi con\n/somniumadmin setShopPrices <nomeShop> <jsonPrezzi>\n");
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, indexAndNameJSON + "\n\n-------------\n\n" + pricesJSON));
                player.spigot().sendMessage(textComponent);
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
                    switch (event.getClick()) {
                        case LEFT:
                            int slot = event.getSlot();
                            Float price = customShop.getPriceOfSlot(slot);
                            if (Objects.nonNull(price)) {
                                player.sendMessage("Price: " + price);
                            }
                            break;
                    }
                }
            }
            event.setCancelled(true);
        }
    }

}
