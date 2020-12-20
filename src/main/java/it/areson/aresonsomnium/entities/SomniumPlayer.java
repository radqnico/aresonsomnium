package it.areson.aresonsomnium.entities;

import it.areson.aresonsomnium.database.MySQLObject;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SomniumPlayer extends MySQLObject {

    public static long DEFAULT_TIME_PLAYED = 0L;
    private final Player player;
    private long timePlayed;

    public SomniumPlayer(MySqlDBConnection mySqlDBConnection, String tableName, Player player) {
        super(mySqlDBConnection, tableName);
        this.player = player;
        this.timePlayed = DEFAULT_TIME_PLAYED;
        updateFromDB();
    }

    public String getPlayerName() {
        return player.getName();
    }

    public Player getPlayer() {
        return player;
    }

    public void addTimePlayed(long seconds) {
        timePlayed += seconds;
    }

    public long getTimePlayed() {
        return timePlayed;
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
            if (update >= 0) {
                mySqlDBConnection.getLogger().info("Tabella '" + tableName + "' creata correttamente.");
            } else {
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
        String query = String.format("INSERT INTO %s (playerName, timePlayed) values ('%s', %d) ON DUPLICATE KEY " +
                        "UPDATE timePlayed=%d",
                tableName,
                getPlayerName(),
                timePlayed,
                timePlayed);
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
                mySqlDBConnection.getLogger().info("Dai del giocatore '" + getPlayerName() + "' recuperati dal DB");
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
