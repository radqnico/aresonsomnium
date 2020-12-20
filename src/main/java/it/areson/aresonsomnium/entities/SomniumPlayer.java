package it.areson.aresonsomnium.entities;

import it.areson.aresonsomnium.database.MySQLObject;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.economy.Wallet;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;

public class SomniumPlayer extends MySQLObject {

    public static long DEFAULT_TIME_PLAYED = 0L;

    private final Player player;
    private LocalDateTime timeJoined;
    private long timePlayed;

    private Wallet wallet;

    public SomniumPlayer(MySqlDBConnection mySqlDBConnection, String tableName, Player player) {
        super(mySqlDBConnection, tableName);
        this.player = player;
        updateFromDB();
    }

    private void setAllDefault() {
        this.timeJoined = LocalDateTime.now();
        this.timePlayed = DEFAULT_TIME_PLAYED;
        this.wallet = Wallet.DEFAULT_WALLET;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public long getSecondsPlayedTotal() {
        return timePlayed + Duration.between(timeJoined, LocalDateTime.now()).toMillis() / 1000;
    }

    public String getPlayerName() {
        return player.getName();
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void createTableIfNotExists() {
        String query = "create table if not exists " + tableName + "\n" +
                "(\n" +
                "    playerName varchar(255) not null\n" +
                "        primary key,\n" +
                "    timePlayed bigint default " + DEFAULT_TIME_PLAYED + " null\n" +
                ");";
        try {
            Connection connection = mySqlDBConnection.connect();
            int update = mySqlDBConnection.update(connection, query);
            if (update < 0) {
                mySqlDBConnection.getLogger().warning("Creazione tabella '" + tableName + "' non riuscita.");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getLogger().severe("Impossibile connettersi per creare '" + tableName + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    @Override
    public void saveToDB() {
        createTableIfNotExists();
        Wallet wallet = getWallet();
        String query = String.format("INSERT INTO %s (playerName, timePlayed, basicCoins, charonCoins, forcedCoins) " +
                        "values ('%s', %d, %d, %d, %d) ON DUPLICATE KEY " +
                        "UPDATE timePlayed=%d, basicCoins=%d, charonCoins=%d, forcedCoins=%d",
                tableName,
                getPlayerName(), getSecondsPlayedTotal(), wallet.getBasicCoins(), wallet.getCharonCoins(), wallet.getForcedCoins(),
                getSecondsPlayedTotal(), wallet.getBasicCoins(), wallet.getCharonCoins(), wallet.getForcedCoins());
        try {
            Connection connection = mySqlDBConnection.connect();
            int update = mySqlDBConnection.update(connection, query);
            if (update >= 0) {
                mySqlDBConnection.getLogger().info("Aggiornato giocatore '" + getPlayerName() + "' sul DB.");
            } else {
                mySqlDBConnection.getLogger().warning("Giocatore '" + getPlayerName() + "' non aggiornato.");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getLogger().severe("Impossibile connettersi per aggiornare il giocatore '" + getPlayerName() + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    @Override
    public void updateFromDB() {
        createTableIfNotExists();
        String query = String.format("select * from somniumPlayer where playerName='%s'",
                getPlayerName());
        try {
            Connection connection = mySqlDBConnection.connect();
            ResultSet select = mySqlDBConnection.select(connection, query);
            if (select.next()) {
                // Presente
                this.timePlayed = select.getLong("timePlayed");
                this.wallet.changeBasicCoins(select.getInt("basicCoins"));
                this.wallet.changeCharonCoins(select.getInt("charonCoins"));
                this.wallet.changeForcedCoins(select.getInt("forcedCoins"));
                mySqlDBConnection.getLogger().info("Dati del giocatore '" + getPlayerName() + "' recuperati dal DB");
            } else {
                // Non presente
                mySqlDBConnection.getLogger().warning("Giocatore '" + getPlayerName() + "' non presente sul DB");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getLogger().severe("Impossibile connettersi per recuperare il giocatore '" + getPlayerName() + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }
}
