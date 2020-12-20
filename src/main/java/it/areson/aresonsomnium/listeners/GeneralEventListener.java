package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class GeneralEventListener implements Listener {

    protected AresonSomnium aresonSomnium;

    public GeneralEventListener(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
    }

    public void registerEvents() {
        aresonSomnium.getServer().getPluginManager().registerEvents(this, aresonSomnium);
    }

    public void unregisterEvents() {
        HandlerList.unregisterAll(this);
    }

}
