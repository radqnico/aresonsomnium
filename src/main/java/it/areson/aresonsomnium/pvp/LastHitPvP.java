package it.areson.aresonsomnium.pvp;

import it.areson.aresonsomnium.AresonSomnium;
import it.areson.aresonsomnium.elements.Pair;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

public class LastHitPvP {

    public final int secondsLastHit;
    private final HashMap<Player, Pair<Player, LocalDateTime>> lastHits;

    public LastHitPvP(AresonSomnium aresonSomnium) {
        lastHits = new HashMap<>();
        secondsLastHit = aresonSomnium.getConfig().getInt("steal-coins.seconds-last-hit");
    }

    public void setLastHit(Player hitter, Player hitted) {
        lastHits.put(hitted, Pair.of(hitter, LocalDateTime.now()));
    }

    public Optional<Player> getKiller(Player player) {
        Pair<Player, LocalDateTime> playerLocalDateTimePair = lastHits.get(player);
        if (playerLocalDateTimePair == null) {
            return Optional.empty();
        }
        LocalDateTime time = playerLocalDateTimePair.right();
        long seconds = Duration.between(time, LocalDateTime.now()).getSeconds();
        if (seconds > secondsLastHit) {
            return Optional.empty();
        }
        return Optional.of(playerLocalDateTimePair.left());
    }

}
