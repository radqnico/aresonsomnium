package it.areson.aresonsomnium.database;

import java.sql.*;
import java.util.logging.Logger;

public class MySqlDBConnection {

    protected final Logger logger;

    public MySqlDBConnection(Logger logger) {
        this.logger = logger;
        testConnection();
    }

    public Logger getLogger() {
        return logger;
    }

    public void testConnection() {
        try {
            Connection connection = connect();
            connection.close();
        } catch (SQLException e) {
            logger.severe("Impossibile connettersi all'SQL.");
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
            logger.severe("Impossibile connettersi all'SQL.");
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
        logger.severe("SQL State: " + exception.getSQLState());
        logger.severe("SQL Error: " + exception.getErrorCode());
        exception.printStackTrace();
    }
}
