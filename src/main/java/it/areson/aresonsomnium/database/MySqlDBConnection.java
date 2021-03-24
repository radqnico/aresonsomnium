package it.areson.aresonsomnium.database;

import it.areson.aresonsomnium.AresonSomnium;

import java.sql.*;

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
        } catch (SQLException e) {
            printSqlExceptionDetails(e);
        }
    }

    public Connection connect() {
        try {
            String host = MySqlConfig.HOST;
            String database = MySqlConfig.DB;
            String user = MySqlConfig.USER;
            String pass = MySqlConfig.PASS;
            return DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "?user=" + user + "&password=" + pass + "&serverTimezone=CET");
        } catch (SQLException e) {
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
        aresonSomnium.getLogger().severe("Errore SQL");
        exception.printStackTrace();
    }
}
