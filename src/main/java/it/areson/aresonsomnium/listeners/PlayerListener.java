package it.areson.aresonsomnium.listeners;

import it.areson.aresonlib.files.MessageManager;
import it.areson.aresonlib.utils.Substitution;
import it.areson.aresonsomnium.AresonSomnium;
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
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class PlayerListener extends GeneralEventListener {

    private final MessageManager messageManager;
    private final HashSet<String> stealCoinsWorlds;
    private final BigDecimal percentOfCoins;
    private final HashMap<String, Integer> playerBlocksBroken;

    public PlayerListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
        this.messageManager = aresonSomnium.getMessageManager();
        registerEvents();

        stealCoinsWorlds = new HashSet<>(aresonSomnium.getConfig().getStringList("steal-coins.allowed-worlds"));
        percentOfCoins = BigDecimal.valueOf(aresonSomnium.getConfig().getDouble("steal-coins.percent-of-coins") / 100);
        playerBlocksBroken = new HashMap<>();
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        // Listen for last hit
        if (event.getDamager() instanceof Player damager &&
                event.getEntity() instanceof Player damaged &&
                stealCoinsWorlds.contains(damaged.getWorld().getName())) {
            aresonSomnium.getLastHitPvP().setLastHit(damager, damaged);
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (stealCoinsWorlds.contains(event.getPlayer().getWorld().getName())) {
            Optional<Player> killer = aresonSomnium.getLastHitPvP().getKiller(event.getPlayer());
            if (killer.isPresent()) {
                Player playerKiller = killer.get();
                SomniumPlayer somniumPlayerKiller = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(playerKiller);
                SomniumPlayer somniumPlayer = aresonSomnium.getSomniumPlayerManager().getSomniumPlayer(event.getPlayer());
                if (somniumPlayer != null && somniumPlayerKiller != null) {
                    BigDecimal coinsPlayer = Wallet.getCoins(event.getPlayer());
                    BigDecimal amountToSteal = coinsPlayer.multiply(percentOfCoins).setScale(1, RoundingMode.HALF_UP);

                    Wallet.addCoins(event.getPlayer(), amountToSteal.negate());
                    Wallet.addCoins(playerKiller, amountToSteal);

                    messageManager.sendMessage(playerKiller, "steal-coins-earned",
                            new Substitution("%coins%", amountToSteal + ""),
                            new Substitution("%playerName%", event.getPlayer().getName())
                    );
                    messageManager.sendMessage(event.getPlayer(), "steal-coins-lost",
                            new Substitution("%coins%", amountToSteal + ""),
                            new Substitution("%playerName%", playerKiller.getName())
                    );
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            ItemStack itemInMainHand = event.getPlayer().getInventory().getItemInMainHand();
            if (itemInMainHand.getItemMeta() instanceof Damageable damageable) {
                //First control because every items seems to be Damageable. Even blocks
                if (damageable.getDamage() > 0 && (damageable.getDamage() * 100) / itemInMainHand.getType().getMaxDurability() > 90) {
                    if (isTimeToSendWarn(event.getPlayer().getName())) {
                        messageManager.sendMessage(event.getPlayer(), "item-low-life");
                    }
                    increaseBrokenBlocks(event.getPlayer().getName());
                }

            }
        }
    }

    private boolean isTimeToSendWarn(String playerName) {
        return !playerBlocksBroken.containsKey(playerName) || playerBlocksBroken.get(playerName) > 10;
    }

    private void increaseBrokenBlocks(String playerName) {
        if (playerBlocksBroken.containsKey(playerName)) {
            Integer oldValue = playerBlocksBroken.get(playerName);
            if (oldValue > 10) {
                playerBlocksBroken.put(playerName, 1);
            } else {
                playerBlocksBroken.put(playerName, oldValue + 1);
            }
        } else {
            playerBlocksBroken.put(playerName, 1);
        }
    }

}
