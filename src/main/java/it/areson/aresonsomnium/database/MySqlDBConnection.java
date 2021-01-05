package it.areson.aresonsomnium.database;

import it.areson.aresonsomnium.utils.Debugger;

import java.sql.*;

public class MySqlDBConnection {

    protected final Debugger debugger;

    public MySqlDBConnection(Debugger debugger) {
        this.debugger = debugger;
        testConnection();
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public void testConnection() {
        try {
            Connection connection = connect();
            connection.close();
        } catch (SQLException e) {
            debugger.debugError("Impossibile connettersi all'SQL.");
            printSqlExceptionDetails(e);
        }
    }

    public Connection connect() {
        try {
            String host = MySqlConfig.HOST;
            String database = MySqlConfig.DB;
            String user = MySqlConfig.USER;
            String pass = MySqlConfig.PASS;
            return DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "?user=" + user + "&password=" + pass);
        } catch (SQLException e) {
            debugger.debugError("Impossibile connettersi all'SQL.");
            printSqlExceptionDetails(e);
        }
        return null;
    }

    public ResultSet select(Connection connection, String sqlQuery) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(sqlQuery);
    }

    public int update(Connection connection, String sqlQuery) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeUpdate(sqlQuery);
    }

    public void printSqlExceptionDetails(SQLException exception) {
        debugger.debugError("SQL State: " + exception.getSQLState());
        debugger.debugError("SQL Error: " + exception.getErrorCode());
        exception.printStackTrace();
    }
}
