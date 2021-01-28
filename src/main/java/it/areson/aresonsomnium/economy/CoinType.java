package it.areson.aresonsomnium.economy;

public enum CoinType {
    CHARON("Charon"),
    FORCED("Forced"),
    BASIC("Basic");

    String coinName;

    CoinType(String coinName) {
        this.coinName = coinName;
    }

    public String getCoinName() {
        return coinName;
    }
}
