package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CheckApplyListener extends GeneralEventListener {

    public CheckApplyListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (EquipmentSlot.HAND.equals(event.getHand())) {
            ItemStack item = event.getItem();
            if (item != null && !Material.AIR.equals(item.getType())) {
                if (Material.PAPER.equals(item.getType())) {
                    SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(event.getPlayer());
                    if (somniumPlayer != null) {
                        if (Wallet.applyCheck(somniumPlayer, item)) {
                            item.setAmount(item.getAmount() - 1);
                            event.getPlayer().sendMessage(aresonSomnium.getMessageManager().getPlainMessage("check-applied"));
                        }
                    } else {
                        aresonSomnium.getDebugger().debugError(aresonSomnium.getMessageManager().getPlainMessage("somniumplayer-not-found"));
                    }
                }
            }
        }
    }
}
