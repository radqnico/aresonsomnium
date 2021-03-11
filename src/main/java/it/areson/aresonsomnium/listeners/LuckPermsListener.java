package it.areson.aresonsomnium.listeners;

import it.areson.aresonsomnium.AresonSomnium;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class LuckPermsListener {

    private final AresonSomnium aresonSomnium;
    private final HashMap<String, Long> playerEventNumbers;

    public LuckPermsListener(AresonSomnium aresonSomnium, LuckPerms luckPerms) {
        this.aresonSomnium = aresonSomnium;
        playerEventNumbers = new HashMap<>();

        EventBus eventBus = luckPerms.getEventBus();

        eventBus.subscribe(this.aresonSomnium, NodeMutateEvent.class, this::onNodeMutateEvent);
    }

    private void onNodeMutateEvent(NodeMutateEvent event) {
        if (event.isUser()) {
            String playerName = ((User) event.getTarget()).getUsername();
            if (playerName != null) {
                Player player = aresonSomnium.getServer().getPlayer(playerName);
                if (player != null) {
                    aresonSomnium.forceMultiplierRefresh(player, event.getDataAfter(), getEventNumber(playerName));
                    upgradeEventNumber(playerName);
                }
            }
        }
    }

    private synchronized long getEventNumber(String playerName) {
        Long actualEventNumber = playerEventNumbers.get(playerName);
        if (actualEventNumber == null) {
            playerEventNumbers.put(playerName, 0L);
            return 0L;
        }
        return actualEventNumber;
    }

    private synchronized void upgradeEventNumber(String playerName) {
        playerEventNumbers.put(playerName, playerEventNumbers.get(playerName));
    }

}
