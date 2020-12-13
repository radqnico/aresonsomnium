package it.areson.aresonsomnium.entities;

import org.bukkit.entity.Player;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "somniumPlayer")
public class SomniumPlayer {

    @Id private String playerName;
    private long timePlayed;

    @Transient private Player player;

    public SomniumPlayer(){
        this.playerName = "___DEFAULT___";
        this.timePlayed = 0;
    }

    public SomniumPlayer(Player player){
        this.player = player;
        this.playerName = player.getName();
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public long getTimePlayed() {
        return timePlayed;
    }

    public void setTimePlayed(long timePlayed) {
        this.timePlayed = timePlayed;
    }
}
