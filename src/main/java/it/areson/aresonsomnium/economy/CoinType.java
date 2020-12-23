package it.areson.aresonsomnium.economy;

public enum CoinType {
    BASIC("Basic"),
    CHARON("Charon"),
    FORCED("Forced");

    String coinName;

    CoinType(String coinName) {
        this.coinName = coinName;
    }

    public String getCoinName() {
        return coinName;
    }
}
