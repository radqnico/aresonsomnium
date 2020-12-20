package it.areson.aresonsomnium.database;

public abstract class MySQLObject {

    protected final MySqlDBConnection mySqlDBConnection;
    protected final String tableName;

    public MySQLObject(MySqlDBConnection mySqlDBConnection, String tableName) {
        this.mySqlDBConnection = mySqlDBConnection;
        this.tableName = tableName;
    }

    public abstract void createTableIfNotExists();

    public abstract void saveToDB();

    public abstract void updateFromDB();

}
