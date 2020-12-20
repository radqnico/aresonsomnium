package it.areson.aresonsomnium.economy;

import it.areson.aresonsomnium.database.MySQLObject;
import it.areson.aresonsomnium.database.MySqlDBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Wallet {

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

    public int getCharonCoins() {
        return charonCoins;
    }

    public int getForcedCoins() {
        return forcedCoins;
    }
}
