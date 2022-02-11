package it.areson.aresonsomnium.players;

import it.areson.aresonsomnium.Constants;
import it.areson.aresonsomnium.database.MySQLObject;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.economy.Price;
import it.areson.aresonsomnium.economy.Wallet;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;

public class SomniumPlayer extends MySQLObject {

    public static long DEFAULT_TIME_PLAYED = 0L;
    public static String tableQuery = """
            create table if not exists somniumPlayer
            (
                playerName  varchar(255)     not null
                    primary key,
                timePlayed  bigint default 0 null,
                obols float  default 0 not null,
                gems float  default 0 not null
            );""";

    private final Player player;
    private LocalDateTime timeJoined;
    private long timePlayed;

    private Wallet wallet;

    public SomniumPlayer(MySqlDBConnection mySqlDBConnection, Player player) {
        super(mySqlDBConnection, Constants.DB_PLAYER_TABLE);
        this.player = player;
        setAllDefault();
        updateFromDB();
    }

    private void setAllDefault() {
        this.timeJoined = LocalDateTime.now();
        this.timePlayed = DEFAULT_TIME_PLAYED;
        this.wallet = new Wallet();
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
            mySqlDBConnection.update(connection, query);
            connection.close();
        } catch (SQLException exception) {
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
                return true;
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
        return false;
    }

    public String getSaveQuery() {
        return String.format("INSERT INTO %s (playerName, timePlayed, obols, gems) " +
                        "values ('%s', %d, %d, %d) ON DUPLICATE KEY " +
                        "UPDATE timePlayed=%d, obols=%d, gems=%d",
                tableName,
                getPlayerName(), getSecondsPlayedTotal(), wallet.getObols(), wallet.getGems(),
                getSecondsPlayedTotal(), wallet.getObols(), wallet.getGems());
    }

    public void setFromResultSet(ResultSet resultSet) throws SQLException {
        this.timePlayed = resultSet.getLong("timePlayed");
        this.wallet.changeObols(BigInteger.valueOf(resultSet.getLong("obols")));
        this.wallet.changeGems(BigInteger.valueOf(resultSet.getLong("gems")));
    }

    public boolean canAfford(Price price) {
        return price.canAffordThis(this);
    }

    public void givePriceAmount(Price price) {
        Wallet.addCoins(getPlayer(), price.getCoins());
        wallet.changeObols(price.getObols());
        wallet.changeGems(price.getGems());
    }

    public boolean takePriceAmount(Price price) {
        if (canAfford(price)) {
            Price negated = price.negate();
            Wallet.addCoins(getPlayer(), negated.getCoins());
            wallet.changeObols(negated.getObols());
            wallet.changeGems(negated.getGems());
            return true;
        }
        return false;
    }

}
