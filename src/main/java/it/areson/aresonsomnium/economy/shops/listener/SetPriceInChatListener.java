package it.areson.aresonsomnium.economy.shops.listener;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.exceptions.PriceConfigNotReadyException;
import it.areson.aresonsomnium.listeners.GeneralEventListener;
import it.areson.aresonsomnium.economy.shops.guis.EditPriceConfig;
import it.areson.aresonsomnium.economy.shops.guis.ShopEditor;
import it.areson.aresonsomnium.economy.shops.guis.ShopManager;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.math.BigDecimal;

public class SetPriceInChatListener extends GeneralEventListener {

    private final ShopManager shopManager;
    private final ShopEditor shopEditor;

    public SetPriceInChatListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
        this.shopManager = aresonSomnium.getShopManager();
        shopEditor = aresonSomnium.getShopEditor();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chatMessageListener(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (shopEditor.isEditingPrice(player)) {
            String message = event.getMessage();
            try {
                BigDecimal bigDecimal = BigDecimal.valueOf(Double.parseDouble(message));
                EditPriceConfig editingPriceConfig = shopEditor.getEditingPriceConfig(player);
                editingPriceConfig.setPrice(bigDecimal);

                editingPriceConfig.execute();

                player.sendMessage(MessageUtils.successMessage("Prezzo impostato."));
                Bukkit.getScheduler().runTask(
                        aresonSomnium,
                        () -> player.openInventory(shopEditor.getEditingCustomShop(player).createInventory(false))
                );

                shopEditor.endEditPrice(player);

                this.unregisterEvents();
            } catch (NumberFormatException e) {
                player.sendMessage("Inserisci un numero decimale.");
            } catch (PriceConfigNotReadyException exception) {
                aresonSomnium.getDebugger().debugError("Ricomincia la procedura. E' successo un errore: " + exception.getMessage());
                exception.printStackTrace();
            }
            event.setCancelled(true);
        }
    }

}
