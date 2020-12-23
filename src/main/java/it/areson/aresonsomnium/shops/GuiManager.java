package it.areson.aresonsomnium.shops;

import it.areson.aresonsomnium.database.MySqlDBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.TreeMap;

public class GuiManager {

    private final TreeMap<String, CustomGUI> permanentGuis;
    private final TreeMap<String, CustomGUI> editingGuis;
    private final MySqlDBConnection mySqlDBConnection;
    private final String tableName;

    public GuiManager(MySqlDBConnection connection, String tableName) {
        this.editingGuis = new TreeMap<>();
        this.permanentGuis = new TreeMap<>();
        this.mySqlDBConnection = connection;
        this.tableName = tableName;
        fetchAllFromDB();
    }

    private void fetchAllFromDB() {
        String query = "select guiName from " + tableName;
        String guiName = "ERRORE NON DOVUTO ALLA GUI";
        try {
            Connection connection = mySqlDBConnection.connect();
            ResultSet resultSet = mySqlDBConnection.select(connection, query);
            while (resultSet.next()) {
                // Presente
                guiName = resultSet.getString("guiName");
                addFromResultSet(guiName, resultSet);
                mySqlDBConnection.getLogger().info("Dati GUI '" + guiName + "' recuperati dal DB");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getLogger().severe("Impossibile connettersi per recuperare la GUI '" + guiName + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    private void addFromResultSet(String guiName, ResultSet resultSet) throws SQLException {
        permanentGuis.put(guiName, CustomGUI.getFromDB(mySqlDBConnection, guiName));
    }

    public boolean isPermanent(String guiName) {
        return permanentGuis.containsKey(guiName);
    }

    public boolean isEditing(String guiName) {
        return editingGuis.containsKey(guiName);
    }

    public CustomGUI getPermanentGui(String guiName) {
        return permanentGuis.get(guiName);
    }

    public CustomGUI createGui(String name, String guiTitle) {
        CustomGUI fromDB = CustomGUI.getFromDB(mySqlDBConnection, name);
        if (Objects.nonNull(fromDB)) {
            return fromDB;
        }
        return editingGuis.put(name, new CustomGUI(name, guiTitle, mySqlDBConnection));
    }
}
