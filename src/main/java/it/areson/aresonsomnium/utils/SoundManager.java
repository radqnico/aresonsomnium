package it.areson.aresonsomnium.utils;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class SoundManager {

    public static void playCoinsSound(Player player){
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, .7f, 1.5f);
    }

    public static void playDeniedSound(Player player){
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, .7f, 1.5f);
    }
    
}
