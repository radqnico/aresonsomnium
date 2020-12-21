package it.areson.aresonsomnium.entities;

import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.utils.PlayerComparator;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SomniumPlayerManager {

    private final TreeMap<Player, SomniumPlayer> onlinePlayers;
    private final MySqlDBConnection connection;
    private final String tableName;

    public SomniumPlayerManager(MySqlDBConnection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
        onlinePlayers = new TreeMap<>(new PlayerComparator());
    }

    public SomniumPlayer getSomniumPlayer(Player player) {
        return onlinePlayers.get(player);
    }

    public SomniumPlayer addSomniumPlayer(Player player) {
        SomniumPlayer somniumPlayer = new SomniumPlayer(connection, tableName, player);
        onlinePlayers.put(player, somniumPlayer);
        return somniumPlayer;
    }

    public void savePlayer(Player player) {
        onlinePlayers.get(player).saveToDB();
    }

    public void removeSomniumPlayer(Player player) {
        onlinePlayers.remove(player);
    }

    public void saveAndRemoveSomniumPlayer(Player player) {
        onlinePlayers.remove(player).saveToDB();
    }

    public void saveAll() {
        for (SomniumPlayer somniumPlayer : onlinePlayers.values()) {
            somniumPlayer.saveToDB();
        }
    }

    public List<String> getOnlinePlayersNames() {
        return onlinePlayers.values().stream().map(SomniumPlayer::getPlayerName).collect(Collectors.toList());
    }
}
