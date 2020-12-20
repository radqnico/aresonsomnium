package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.entities.SomniumPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SomniumPlayerDBEvents extends GeneralEventListener {

    public SomniumPlayerDBEvents(AresonSomnium aresonSomnium) {
        super(aresonSomnium);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SomniumPlayerManager somniumPlayerManager = aresonSomnium.getSomniumPlayerManager();
        somniumPlayerManager.addSomniumPlayer(player);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SomniumPlayerManager somniumPlayerManager = aresonSomnium.getSomniumPlayerManager();
        somniumPlayerManager.saveAndRemoveSomniumPlayer(player);
    }

}
