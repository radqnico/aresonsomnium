package it.areson.aresonsomnium.economy;

public class Wallet {

    public static Wallet DEFAULT_WALLET = new Wallet(0, 0, 0);

    private int basicCoins;
    private int charonCoins;
    private int forcedCoins;

    public Wallet(int basicCoins, int charonCoins, int forcedCoins) {
        this.basicCoins = basicCoins;
        this.charonCoins = charonCoins;
        this.forcedCoins = forcedCoins;
    }

    public int getBasicCoins() {
        return basicCoins;
    }

    public void setBasicCoins(int basicCoins) {
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

    public void changeBasicCoins(int amount) {
        basicCoins += amount;
    }

    public void changeCharonCoins(int amount) {
        charonCoins += amount;
    }

    public void changeForcedCoins(int amount) {
        forcedCoins += amount;
    }
}
