package it.areson.aresonsomnium.players;

import it.areson.aresonsomnium.database.MySQLObject;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.economy.CoinType;
import it.areson.aresonsomnium.economy.Wallet;
import it.areson.aresonsomnium.exceptions.CantAffordException;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;

public class SomniumPlayer extends MySQLObject {

    public static long DEFAULT_TIME_PLAYED = 0L;
    public static String tableQuery = "create table if not exists somniumPlayer\n" +
            "(\n" +
            "    playerName  varchar(255)     not null\n" +
            "        primary key,\n" +
            "    timePlayed  bigint default 0 null,\n" +
            "    charonCoins float  default 0 not null,\n" +
            "    forcedCoins float  default 0 not null\n" +
            ");";

    private final Player player;
    private LocalDateTime timeJoined;
    private long timePlayed;

    private Wallet wallet;

    public SomniumPlayer(MySqlDBConnection mySqlDBConnection, String tableName, Player player) {
        super(mySqlDBConnection, tableName);
        this.player = player;
        setAllDefault();
        updateFromDB();
    }

    private void setAllDefault() {
        this.timeJoined = LocalDateTime.now();
        this.timePlayed = DEFAULT_TIME_PLAYED;
        this.wallet = Wallet.getNewDefaultWallet();
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
    public void saveToDB() {
        createTableIfNotExists(String.format(tableQuery, tableName));
        String query = getSaveQuery();
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
    public boolean updateFromDB() {
        createTableIfNotExists(String.format(tableQuery, tableName));
        String query = String.format("select * from somniumPlayer where playerName='%s'",
                getPlayerName());
        try {
            Connection connection = mySqlDBConnection.connect();
            ResultSet resultSet = mySqlDBConnection.select(connection, query);
            if (resultSet.next()) {
                // Presente
                setFromResultSet(resultSet);
                mySqlDBConnection.getLogger().info("Dati del giocatore '" + getPlayerName() + "' recuperati dal DB");
                return true;
            } else {
                // Non presente
                mySqlDBConnection.getLogger().warning("Giocatore '" + getPlayerName() + "' non presente sul DB");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getLogger().severe("Impossibile connettersi per recuperare il giocatore '" + getPlayerName() + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
        return false;
    }

    public String getSaveQuery() {
        return String.format("INSERT INTO %s (playerName, timePlayed, charonCoins, forcedCoins) " +
                        "values ('%s', %d, %d, %d) ON DUPLICATE KEY " +
                        "UPDATE timePlayed=%d, charonCoins=%d, forcedCoins=%d",
                tableName,
                getPlayerName(), getSecondsPlayedTotal(), wallet.getCharonCoins(), wallet.getForcedCoins(),
                getSecondsPlayedTotal(), wallet.getCharonCoins(), wallet.getForcedCoins());
    }

    public void setFromResultSet(ResultSet resultSet) throws SQLException {
        this.timePlayed = resultSet.getLong("timePlayed");
        this.wallet.changeCharonCoins(resultSet.getInt("charonCoins"));
        this.wallet.changeForcedCoins(resultSet.getInt("forcedCoins"));
    }

    public boolean canAfford(CoinType coinType, int amount) {
        switch (coinType) {
            case CHARON:
                return getWallet().getCharonCoins() >= amount;
            case FORCED:
                return getWallet().getForcedCoins() >= amount;
            default:
                return false;
        }
    }

    public void changeCoins(CoinType coinType, int amount) {
        if (canAfford(coinType, amount)) {
            switch (coinType) {
                case CHARON:
                    getWallet().changeCharonCoins(amount);
                    break;
                case FORCED:
                    getWallet().changeForcedCoins(amount);
                    break;
            }
        } else {
            throw new CantAffordException("Can't remove " + amount + " " + coinType.getCoinName() + " Coins from player '" + getPlayerName() + "'");
        }
    }
}
