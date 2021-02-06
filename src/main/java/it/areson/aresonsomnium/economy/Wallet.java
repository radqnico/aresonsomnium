package it.areson.aresonsomnium.economy;

import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import it.areson.aresonsomnium.players.SomniumPlayer;
import net.ess3.api.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Wallet {

    private BigInteger obols;
    private BigInteger gems;

    public Wallet(BigInteger obols, BigInteger gems) {
        this.obols = obols;
        this.gems = gems;
    }

    public static Wallet getNewDefaultWallet() {
        return new Wallet(BigInteger.ZERO, BigInteger.ZERO);
    }

    public static int getCheckModelData() {
        return 999;
    }
    public static int getCharonNuggetModelData() {
        return 998;
    }

    public static ItemStack generateCheck(double amount, CoinType coinType) {
        ItemStack itemStack = new ItemStack(Material.PAPER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aAssegno di &l" + coinType.getCoinName()));
            List<String> lore = new ArrayList<>();
            lore.add("Valore:");
            lore.add("" + amount);
            lore.add(coinType.getCoinName());
            itemMeta.setLore(lore);
            itemMeta.setCustomModelData(getCheckModelData());
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.addEnchant(Enchantment.DAMAGE_UNDEAD, 1, true);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack generateCharonNugget(){
        ItemStack itemStack = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Pepita di &lCaronte"));
            List<String> lore = new ArrayList<>();
            lore.add("&6Colleziona e consegna queste");
            lore.add("&6pepite a &lCaronte &6per");
            lore.add("&6ottenere gli Oboli");
            itemMeta.setLore(lore);
            itemMeta.setCustomModelData(getCharonNuggetModelData());
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.addEnchant(Enchantment.DAMAGE_UNDEAD, 1, true);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static boolean applyCheck(SomniumPlayer somniumPlayer, ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasCustomModelData()) {
            if (itemMeta.getCustomModelData() == getCheckModelData()) {
                if (itemMeta.hasLore()) {
                    List<String> lore = itemMeta.getLore();
                    if (lore != null && lore.size() >= 3) {
                        String amountString = lore.get(1);
                        BigDecimal amount = new BigDecimal(amountString);
                        String coinTypeString = lore.get(2).toUpperCase();
                        CoinType coinType = CoinType.valueOf(coinTypeString);
                        switch (coinType) {
                            case OBOLI:
                                somniumPlayer.getWallet().changeCharonCoins(amount.toBigInteger());
                                return true;
                            case GEMME:
                                somniumPlayer.getWallet().changeForcedCoins(amount.toBigInteger());
                                return true;
                            case MONETE:
                                Wallet.addBasicCoins(somniumPlayer.getPlayer(), amount);
                                return true;
                        }
                    }
                }
            }
        }
        return false;
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

    public BigInteger getObols() {
        return obols;
    }

    public void setObols(BigInteger obols) {
        this.obols = obols;
    }

    public BigInteger getGems() {
        return gems;
    }

    public void setGems(BigInteger gems) {
        this.gems = gems;
    }

    public void changeCharonCoins(BigInteger amount) {
        obols = obols.add(amount);
    }

    public void changeForcedCoins(BigInteger amount) {
        gems = gems.add(amount);
    }
}
