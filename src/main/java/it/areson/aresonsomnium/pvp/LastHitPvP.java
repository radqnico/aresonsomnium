package it.areson.aresonsomnium.pvp;

import it.areson.aresonsomnium.elements.Pair;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

public class LastHitPvP {

    public static int SECONDS_LAST_HIT = 10;
    private HashMap<Player, Pair<Player, LocalDateTime>> lastHit;

    public LastHitPvP() {
        lastHit = new HashMap<>();
    }

    public void setLastHit(Player hitter, Player hit) {
        lastHit.put(hit, Pair.of(hitter, LocalDateTime.now()));
    }

    public Optional<Player> getKiller(Player player) {
        Pair<Player, LocalDateTime> playerLocalDateTimePair = lastHit.get(player);
        if (playerLocalDateTimePair == null) {
            return Optional.empty();
        }
        LocalDateTime time = playerLocalDateTimePair.right();
        long seconds = Duration.between(time, LocalDateTime.now()).getSeconds();
        if (seconds > SECONDS_LAST_HIT) {
            return Optional.empty();
        }
        return Optional.ofNullable(lastHit.get(player).left());
    }

}
