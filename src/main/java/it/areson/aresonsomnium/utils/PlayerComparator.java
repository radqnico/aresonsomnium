package it.areson.aresonsomnium.utils;

import org.bukkit.entity.Player;

import java.util.Comparator;

public class PlayerComparator implements Comparator<Player> {

    @Override
    public int compare(Player player1, Player player2) {
        return player1.getName().compareTo(player2.getName());
    }

}
