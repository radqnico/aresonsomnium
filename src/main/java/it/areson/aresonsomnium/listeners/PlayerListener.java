package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.api.AresonSomniumAPI;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.math.BigDecimal;
import java.util.HashSet;

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
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        // Listen for death
        if (stealCoinsWorlds.contains(event.getEntity().getWorld().getName())) {
//            Optional<Player> killer = AresonSomniumAPI.instance.getLastHitPvP().getKiller(event.getEntity());
//            killer.ifPresent(playerKiller -> {
            Player playerKiller = event.getEntity().getKiller();
            if (playerKiller != null) {
                SomniumPlayer somniumPlayerKiller = AresonSomniumAPI.instance.getSomniumPlayerManager().getSomniumPlayer(playerKiller);
                SomniumPlayer somniumPlayer = AresonSomniumAPI.instance.getSomniumPlayerManager().getSomniumPlayer(event.getEntity());
                if (somniumPlayer != null && somniumPlayerKiller != null) {
                    BigDecimal coinsPlayer = Wallet.getCoins(event.getEntity());
                    BigDecimal amountToSteal = coinsPlayer.multiply(BigDecimal.valueOf(0.05));
                    Wallet.addCoins(event.getEntity(), amountToSteal.negate());
                    Wallet.addCoins(playerKiller, amountToSteal);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            ItemStack itemInMainHand = event.getPlayer().getInventory().getItemInMainHand();
            if (itemInMainHand.getItemMeta() instanceof Damageable damageable) {
                if ((damageable.getDamage() * 100) / itemInMainHand.getType().getMaxDurability() > 95) {
                    aresonSomnium.sendInfoMessage(event.getPlayer(), "Il tuo strumento ha meno del 5% di vita!");
                }
            }
        }
    }

}
