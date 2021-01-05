package it.areson.aresonsomnium.economy;

import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.ess3.api.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class Wallet {

    private int charonCoins;
    private int forcedCoins;

    public Wallet(int charonCoins, int forcedCoins) {
        this.charonCoins = charonCoins;
        this.forcedCoins = forcedCoins;
    }

    public static Wallet getNewDefaultWallet() {
        return new Wallet(0, 0);
    }

    public static BigDecimal getBasicCoins(Player player) {
        try {
            return Economy.getMoneyExact(player.getName());
        } catch (UserDoesNotExistException exception) {
            Bukkit.getLogger().severe("Il giocatore '" + player.getName() + "' non esiste");
            return new BigDecimal(0);
        }
    }

    public static void setBasicCoins(Player player, BigDecimal amount) {
        try {
            Economy.setMoney(player.getName(), amount);
        } catch (UserDoesNotExistException | NoLoanPermittedException exception) {
            exception.printStackTrace();
        }
    }

    public static void addBasicCoins(Player player, BigDecimal amount) {
        try {
            Economy.add(player.getName(), amount);
        } catch (UserDoesNotExistException | NoLoanPermittedException exception) {
            exception.printStackTrace();
        }
    }

    public int getCharonCoins() {
        return charonCoins;
    }

    public void setCharonCoins(int charonCoins) {
        this.charonCoins = charonCoins;
    }

    public int getForcedCoins() {
        return forcedCoins;
    }

    public void setForcedCoins(int forcedCoins) {
        this.forcedCoins = forcedCoins;
    }

    public void changeCharonCoins(int amount) {
        charonCoins += amount;
    }

    public void changeForcedCoins(int amount) {
        forcedCoins += amount;
    }
}
