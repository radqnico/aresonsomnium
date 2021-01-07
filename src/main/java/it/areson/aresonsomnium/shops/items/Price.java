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

    private BigDecimal basicCoins;
    private BigInteger charonCoins;
    private BigInteger forcedCoins;

    public Price(BigDecimal basicCoins, BigInteger charonCoins, BigInteger forcedCoins) {
        this.basicCoins = basicCoins;
        this.charonCoins = charonCoins;
        this.forcedCoins = forcedCoins;
    }

    public Price() {
        this(BigDecimal.valueOf(-1), BigInteger.valueOf(-1), BigInteger.valueOf(-1));
    }

    public BigDecimal getBasicCoins() {
        return basicCoins;
    }

    public void setBasicCoins(BigDecimal basicCoins) {
        this.basicCoins = basicCoins;
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

    public boolean canAffordThis(SomniumPlayer somniumPlayer) {
        return Wallet.getBasicCoins(somniumPlayer.getPlayer()).compareTo(basicCoins) >= 0 &&
                somniumPlayer.getWallet().getCharonCoins().compareTo(charonCoins) >= 0 &&
                somniumPlayer.getWallet().getForcedCoins().compareTo(forcedCoins) >= 0;
    }

    public void removeFrom(SomniumPlayer somniumPlayer) {
        Wallet.addBasicCoins(somniumPlayer.getPlayer(), basicCoins.negate());
        somniumPlayer.getWallet().changeCharonCoins(charonCoins.negate());
        somniumPlayer.getWallet().changeForcedCoins(forcedCoins.negate());
    }

    public boolean isPriceReady() {
        return basicCoins.compareTo(BigDecimal.valueOf(0)) >= 0 &&
                charonCoins.compareTo(BigInteger.valueOf(0)) >= 0 &&
                forcedCoins.compareTo(BigInteger.valueOf(0)) >= 0;
    }

    @Override
    public String toString() {
        return "Price{basicCoins=" + basicCoins + ",charonCoins=" + charonCoins + ",forcedCoins=" + forcedCoins + "}";
    }

    public ArrayList<String> toLore() {
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&f$ " + basicCoins.toPlainString()));
        lore.add(MessageUtils.errorMessage("Oboli " + charonCoins.toString()));
        lore.add(MessageUtils.successMessage("Gemme " + forcedCoins.toString()));
        return lore;
    }

    public void setCoins(CoinType coinType, BigDecimal price) {
        switch (coinType) {
            case CHARON:
                setCharonCoins(price.toBigInteger());
                break;
            case FORCED:
                setForcedCoins(price.toBigInteger());
                break;
            case BASIC:
                setBasicCoins(price);
                break;
        }
    }
}
