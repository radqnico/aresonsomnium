package it.areson.aresonsomnium.economy;

public enum CoinType {
    MONETE("Monete"),
    GEMME("Gemme"),
    OBOLI("Oboli");

    String coinName;

    CoinType(String coinName) {
        this.coinName = coinName;
    }

    public String getCoinName() {
        return coinName;
    }
}
