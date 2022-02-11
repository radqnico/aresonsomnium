package it.areson.aresonsomnium.economy;

public enum CoinType {
    COINS("Monete"),
    GEMS("Gemme"),
    OBOLS("Oboli");

    private String coinName;

    CoinType(String coinName) {
        this.coinName = coinName;
    }

    public String getCoinName() {
        return coinName;
    }

}
