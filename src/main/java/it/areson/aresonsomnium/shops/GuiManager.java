package it.areson.aresonsomnium.shops;

import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.utils.PlayerComparator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.TreeMap;

public class GuiManager {

    private final TreeMap<String, CustomGUI> permanentGuis;
    private final TreeMap<String, CustomGUI> volatileGuis;
    private final TreeMap<Player, String> editingGuis;
    private final MySqlDBConnection mySqlDBConnection;
    private final String tableName;

    public GuiManager(MySqlDBConnection connection, String tableName) {
        this.volatileGuis = new TreeMap<>();
        this.permanentGuis = new TreeMap<>();
        this.editingGuis = new TreeMap<>(new PlayerComparator());
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

    public CustomGUI getPermanentGui(String guiName) {
        return permanentGuis.get(guiName);
    }

    public CustomGUI createNewGui(String name, String guiTitle) {
        if (permanentGuis.containsKey(name)) {
            CustomGUI customGUI = permanentGuis.get(name);
            volatileGuis.put(name, customGUI);
            return customGUI;
        }
        CustomGUI customGUI = new CustomGUI(name, guiTitle, mySqlDBConnection);
        volatileGuis.put(name, customGUI);
        return customGUI;
    }

    public void beginEditGui(Player player, String guiName) {
        editingGuis.put(player, guiName);
    }

    public boolean endEditGui(Player player, Inventory inventory) {
        String guiName = editingGuis.remove(player);
        if (Objects.nonNull(guiName)) {
            CustomGUI customGUI = volatileGuis.remove(guiName);
            customGUI.updateFromInventory(inventory);
            customGUI.saveToDB();
            return true;
        }
        return false;
    }

    public boolean someoneEditing() {
        return !editingGuis.isEmpty();
    }
}
