package it.areson.aresonsomnium.shops.items;

import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;
import it.areson.aresonsomnium.utils.MessageUtils;
import org.bukkit.ChatColor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

public class Price {

    private BigDecimal coins;
    private BigInteger obols;
    private BigInteger gems;

    public Price(BigDecimal coins, BigInteger obols, BigInteger gems) {
        this.coins = coins;
        this.obols = obols;
        this.gems = gems;
    }

    public Price() {
        this(BigDecimal.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0));
    }

    public BigDecimal getCoins() {
        return coins;
    }

    public void setCoins(BigDecimal coins) {
        this.coins = coins;
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

    public boolean canAffordThis(SomniumPlayer somniumPlayer) {
        return Wallet.getCoins(somniumPlayer.getPlayer()).compareTo(coins) >= 0 &&
                somniumPlayer.getWallet().getObols().compareTo(obols) >= 0 &&
                somniumPlayer.getWallet().getGems().compareTo(gems) >= 0;
    }

    public void removeFrom(SomniumPlayer somniumPlayer) {
        Wallet.addCoins(somniumPlayer.getPlayer(), coins.negate());
        somniumPlayer.getWallet().changeObols(obols.negate());
        somniumPlayer.getWallet().changeGems(gems.negate());
    }

    public void addTo(SomniumPlayer somniumPlayer) {
        Wallet.addCoins(somniumPlayer.getPlayer(), coins);
        somniumPlayer.getWallet().changeObols(obols);
        somniumPlayer.getWallet().changeGems(gems);
    }

    public boolean isPriceReady() {
        return coins.compareTo(BigDecimal.valueOf(0)) > 0 ||
                obols.compareTo(BigInteger.valueOf(0)) > 0 ||
                gems.compareTo(BigInteger.valueOf(0)) > 0;
    }

    @Override
    public String toString() {
        return "Price{coins=" + coins + ",obols=" + obols + ",gems=" + gems + "}";
    }

    public ArrayList<String> toLore() {
        ArrayList<String> lore = new ArrayList<>();
        if (coins.compareTo(BigDecimal.valueOf(0)) > 0) {
            lore.add(ChatColor.translateAlternateColorCodes('&', "&a$ " + coins.toPlainString()));
        }
        if (obols.compareTo(BigInteger.valueOf(0)) > 0) {
            lore.add(MessageUtils.errorMessage("Oboli " + obols.toString()));
        }
        if (gems.compareTo(BigInteger.valueOf(0)) > 0) {
            lore.add(MessageUtils.successMessage("Gemme " + gems.toString()));
        }
        return lore;
    }

    public void setCoins(CoinType coinType, BigDecimal price) {
        switch (coinType) {
            case OBOLI:
                setObols(price.toBigInteger());
                break;
            case GEMME:
                setGems(price.toBigInteger());
                break;
            case MONETE:
                setCoins(price);
                break;
        }
    }
}
