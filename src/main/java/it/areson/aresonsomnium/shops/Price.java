package it.areson.aresonsomnium.shops;

import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.players.SomniumPlayer;

import java.math.BigDecimal;

public class Price {

    private BigDecimal basicCoins;
    private int charonCoins;
    private int forcedCoins;

    public Price(BigDecimal basicCoins, int charonCoins, int forcedCoins) {
        this.basicCoins = basicCoins;
        this.charonCoins = charonCoins;
        this.forcedCoins = forcedCoins;
    }

    public Price() {
        this(BigDecimal.valueOf(-1), -1, -1);
    }

    public BigDecimal getBasicCoins() {
        return basicCoins;
    }

    public void setBasicCoins(BigDecimal basicCoins) {
        this.basicCoins = basicCoins;
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

    public boolean canAffordThis(SomniumPlayer somniumPlayer) {
        return Wallet.getBasicCoins(somniumPlayer.getPlayer()).compareTo(basicCoins) >= 0 ||
                somniumPlayer.getWallet().getCharonCoins() >= charonCoins ||
                somniumPlayer.getWallet().getForcedCoins() >= forcedCoins;
    }

    public void removeFrom(SomniumPlayer somniumPlayer) {
        Wallet.addBasicCoins(somniumPlayer.getPlayer(), basicCoins.negate());
        somniumPlayer.getWallet().changeCharonCoins(-charonCoins);
        somniumPlayer.getWallet().changeForcedCoins(-forcedCoins);
    }

    public boolean isPriceReady() {
        return basicCoins.compareTo(BigDecimal.valueOf(0)) >= 0 && charonCoins >= 0 && forcedCoins >= 0;
    }
}
