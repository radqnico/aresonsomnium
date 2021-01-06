package it.areson.aresonsomnium.economy;

import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.ess3.api.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Wallet {

    private BigInteger charonCoins;
    private BigInteger forcedCoins;

    public Wallet(BigInteger charonCoins, BigInteger forcedCoins) {
        this.charonCoins = charonCoins;
        this.forcedCoins = forcedCoins;
    }

    public static Wallet getNewDefaultWallet() {
        return new Wallet(BigInteger.ZERO, BigInteger.ZERO);
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

    public BigInteger getCharonCoins() {
        return charonCoins;
    }

    public void setCharonCoins(BigInteger charonCoins) {
        this.charonCoins = charonCoins;
    }

    public BigInteger getForcedCoins() {
        return forcedCoins;
    }

    public void setForcedCoins(BigInteger forcedCoins) {
        this.forcedCoins = forcedCoins;
    }

    public void changeCharonCoins(BigInteger amount) {
        charonCoins = charonCoins.add(amount);
    }

    public void changeForcedCoins(BigInteger amount) {
        forcedCoins = forcedCoins.add(amount);
    }
}
