package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.math.BigDecimal;
import java.util.Optional;

public class PlayerListener extends GeneralEventListener {

    public PlayerListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
        registerEvents();
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        System.out.println(event.getDamager().getType());
        System.out.println(event.getEntity().getType());

//        if (event.getEntity() instanceof Player player) {
//            //Set last hit
//            event.get
//            AresonSomniumAPI.instance.getLastHitPvP().setLastHit(player, );
//
//            //Listen for death
//            if (event.getDamage() >= player.getHealth()) {
//                Optional<Player> killer = AresonSomniumAPI.instance.getLastHitPvP().getKiller(player);
//                killer.ifPresent(playerKiller -> {
//                    SomniumPlayer somniumPlayerKiller = AresonSomniumAPI.instance.getSomniumPlayerManager().getSomniumPlayer(playerKiller);
//                    SomniumPlayer somniumPlayer = AresonSomniumAPI.instance.getSomniumPlayerManager().getSomniumPlayer(player);
//                    if (somniumPlayer != null && somniumPlayerKiller != null) {
//                        BigDecimal coinsPlayer = Wallet.getCoins(player);
//                        BigDecimal amountToSteal = coinsPlayer.multiply(BigDecimal.valueOf(0.05));
//                        Wallet.addCoins(player, amountToSteal.negate());
//                        Wallet.addCoins(playerKiller, amountToSteal);
//                    }
//                });
//            }
//        }
    }

}
