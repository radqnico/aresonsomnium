package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.meta.Damageable;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

public class PlayerListener extends GeneralEventListener {

    private final HashSet<String> stealCoinsWorlds;

    public PlayerListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
        registerEvents();

        stealCoinsWorlds = new HashSet<>(aresonSomnium.getConfig().getStringList("steal-coins-worlds"));
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        // Listen for last hit
        if (event.getDamager() instanceof Player damager &&
                event.getEntity() instanceof Player damaged &&
                stealCoinsWorlds.contains(damaged.getWorld().getName())) {
            AresonSomniumAPI.instance.getLastHitPvP().setLastHit(damager, damaged);
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        // Listen for death
        if (event.getEntity() instanceof Player damaged &&
                event.getDamage() >= damaged.getHealth() &&
                stealCoinsWorlds.contains(damaged.getWorld().getName())) {
            Optional<Player> killer = AresonSomniumAPI.instance.getLastHitPvP().getKiller(damaged);
            killer.ifPresent(playerKiller -> {
                SomniumPlayer somniumPlayerKiller = AresonSomniumAPI.instance.getSomniumPlayerManager().getSomniumPlayer(playerKiller);
                SomniumPlayer somniumPlayer = AresonSomniumAPI.instance.getSomniumPlayerManager().getSomniumPlayer(damaged);
                if (somniumPlayer != null && somniumPlayerKiller != null) {
                    BigDecimal coinsPlayer = Wallet.getCoins(damaged);
                    BigDecimal amountToSteal = coinsPlayer.multiply(BigDecimal.valueOf(0.05));
                    Wallet.addCoins(damaged, amountToSteal.negate());
                    Wallet.addCoins(playerKiller, amountToSteal);
                }
            });
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if(!event.isCancelled()) {
            if(event.getPlayer().getItemInUse() instanceof Damageable damageable) {
                event.getPlayer().sendMessage(damageable.getDamage() + "");
            }
        }
    }

}
