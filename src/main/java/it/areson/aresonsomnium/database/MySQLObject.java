package it.areson.aresonsomnium.database;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class MySQLObject {

    protected final MySqlDBConnection mySqlDBConnection;
    protected final String tableName;

    public MySQLObject(MySqlDBConnection mySqlDBConnection, String tableName) {
        this.mySqlDBConnection = mySqlDBConnection;
        this.tableName = tableName;
    }

    protected void createTableIfNotExists(String query) {
        try {
            Connection connection = mySqlDBConnection.connect();
            int update = mySqlDBConnection.update(connection, query);
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    public abstract void saveToDB();

    public abstract boolean updateFromDB();

}
