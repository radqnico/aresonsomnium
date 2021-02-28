package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.entity.Player;

public class LuckPermsListener {

    private final AresonSomnium aresonSomnium;

    public LuckPermsListener(AresonSomnium aresonSomnium, LuckPerms luckPerms) {
        this.aresonSomnium = aresonSomnium;

        EventBus eventBus = luckPerms.getEventBus();

        eventBus.subscribe(this.aresonSomnium, UserDataRecalculateEvent.class, this::onUserDataRecalculateEvent);
    }

    private void onUserDataRecalculateEvent(UserDataRecalculateEvent event) {
        String username = event.getUser().getUsername();
        if (username != null) {
            Player player = aresonSomnium.getServer().getPlayer(username);
            if (player != null) {
                aresonSomnium.forceMultiplierRefresh(player);
            }
        }

    }

}
