package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.players.SomniumPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;

public class GatewayListener extends GeneralEventListener {

    public GatewayListener(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SomniumPlayerManager somniumPlayerManager = aresonSomnium.getSomniumPlayerManager();
        somniumPlayerManager.addSomniumPlayer(player);

        // Add cached multiplier
        aresonSomnium.forceMultiplierRefresh(player, new HashSet<>());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SomniumPlayerManager somniumPlayerManager = aresonSomnium.getSomniumPlayerManager();
        somniumPlayerManager.saveAndRemoveSomniumPlayer(player);

        // Remove cached multiplier
        aresonSomnium.playerMultipliers.remove(player.getName());
    }

}
