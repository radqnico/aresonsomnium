package it.areson.aresonsomnium.database;

import it.areson.aresonsomnium.AresonSomnium;

import java.sql.*;

import static it.areson.aresonsomnium.Constants.*;

public class MySqlDBConnection {

    private final AresonSomnium aresonSomnium;

    public MySqlDBConnection(AresonSomnium aresonSomnium) {
        this.aresonSomnium = aresonSomnium;
        testConnection();
    }

    public void testConnection() {
        try {
            Connection connection = connect();
            connection.close();
        } catch (SQLException exception) {
            printSqlExceptionDetails(exception);
        }
    }

    public Connection connect() {
        try {
            return DriverManager.getConnection("jdbc:mysql://" + DB_HOST + "/" + DB_NAME + "?user=" + DB_USER + "&password=" + DB_PASSWORD + "&serverTimezone=CET");
        } catch (SQLException exception) {
            printSqlExceptionDetails(exception);
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
        aresonSomnium.getLogger().severe("Errore SQL");
        exception.printStackTrace();
    }
}
