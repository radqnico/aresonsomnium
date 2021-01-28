package it.areson.aresonsomnium.economy;

import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.ess3.api.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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

    public static int getCheckModelData(){
        return 999;
    }

    public static ItemStack generateCheck(double amount, CoinType coinType) {
        ItemStack itemStack = new ItemStack(Material.PAPER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta != null){
            itemMeta.setDisplayName("&aAssegno di &l" + coinType.getCoinName());
            List<String> lore = new ArrayList<>();
            lore.add(""+amount);
            itemMeta.setLore(lore);
            itemMeta.setCustomModelData(getCheckModelData());
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
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
