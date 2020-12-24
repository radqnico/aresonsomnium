package it.areson.aresonsomnium.economy;

public class Wallet {

    private float basicCoins;
    private float charonCoins;
    private float forcedCoins;

    public Wallet(float basicCoins, float charonCoins, float forcedCoins) {
        this.basicCoins = basicCoins;
        this.charonCoins = charonCoins;
        this.forcedCoins = forcedCoins;
    }

    public static Wallet getNewDefaultWallet() {
        return new Wallet(0, 0, 0);
    }

    public float getBasicCoins() {
        return basicCoins;
    }

    public void setBasicCoins(float basicCoins) {
        this.basicCoins = basicCoins;
    }

    public float getCharonCoins() {
        return charonCoins;
    }

    public void setCharonCoins(float charonCoins) {
        this.charonCoins = charonCoins;
    }

    public float getForcedCoins() {
        return forcedCoins;
    }

    public void setForcedCoins(float forcedCoins) {
        this.forcedCoins = forcedCoins;
    }

    public void changeBasicCoins(float amount) {
        basicCoins += amount;
    }

    public void changeCharonCoins(float amount) {
        charonCoins += amount;
    }

    public void changeForcedCoins(float amount) {
        forcedCoins += amount;
    }
}
