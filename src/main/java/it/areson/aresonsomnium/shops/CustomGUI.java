package it.areson.aresonsomnium.shops;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.areson.aresonsomnium.database.MySQLObject;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static it.areson.aresonsomnium.database.MySqlConfig.GUIS_TABLE_NAME;

public class CustomGUI extends MySQLObject {

    public static String tableQuery = "create table if not exists %s (" +
            "guiName varchar(255) not null primary key,\n" +
            "guiTitle varchar(255) not null,\n" +
            "guiObjects text not null\n" +
            ");";

    private final String name;
    private final TreeMap<Integer, ItemStack> items;
    private String title;

    public CustomGUI(String name, String title, MySqlDBConnection mySqlDBConnection) {
        super(mySqlDBConnection, GUIS_TABLE_NAME);
        this.title = title;
        this.name = name;
        this.items = new TreeMap<>();
    }

    public static CustomGUI getFromDB(MySqlDBConnection connection, String name) {
        CustomGUI customGUI = new CustomGUI(name, null, connection);
        if (customGUI.updateFromDB()) {
            return customGUI;
        } else {
            return null;
        }
    }

    public Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            Integer key = entry.getKey();
            ItemStack value = entry.getValue();
            inventory.setItem(key, value);
        }
        return inventory;
    }

    public void updateFromInventory(Inventory inventory) {
        items.clear();
        int size = inventory.getSize();
        for (int i = 0; i < size; i++) {
            ItemStack item = inventory.getItem(i);
            if (Objects.nonNull(item) && !item.getType().equals(Material.AIR)) {
                items.put(i, item);
            }
        }
    }

    @Override
    public void saveToDB() {
        createTableIfNotExists(String.format(tableQuery, tableName));
        String saveQuery = getSaveQuery();
        try {
            Connection connection = mySqlDBConnection.connect();
            int update = mySqlDBConnection.update(connection, saveQuery);
            if (update >= 0) {
                mySqlDBConnection.getLogger().info("Aggiornata GUI '" + name + "' sul DB.");
            } else {
                mySqlDBConnection.getLogger().warning("GUI '\" + name + \"' NON aggiornata sul DB.");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getLogger().severe("Impossibile connettersi per aggiornare la GUI '" + name + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    public String getSaveQuery() {
        Gson gson = new Gson();
        Map<String, String> serialized = items.entrySet().parallelStream().collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> Base64.getEncoder().encodeToString(e.getValue().serializeAsBytes())
        ));
        String itemsJson = gson.toJson(serialized);
        return String.format("INSERT INTO %s (guiName, guiTitle, guiItems) " +
                        "values ('%s', '%s', '%s') ON DUPLICATE KEY " +
                        "UPDATE guiTitle=%s, guiItems=%s",
                tableName,
                name, title, itemsJson,
                title, itemsJson
        );
    }

    @Override
    public boolean updateFromDB() {
        createTableIfNotExists(String.format(tableQuery, tableName));
        String query = "select * from somniumGuis where guiName='" + name + "'";
        try {
            Connection connection = mySqlDBConnection.connect();
            ResultSet resultSet = mySqlDBConnection.select(connection, query);
            if (resultSet.next()) {
                // Presente
                setFromResultSet(resultSet);
                mySqlDBConnection.getLogger().info("Dati GUI '" + name + "' recuperati dal DB");
                return true;
            } else {
                // Non presente
                mySqlDBConnection.getLogger().warning("GUI '" + name + "' non presente sul DB");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getLogger().severe("Impossibile connettersi per recuperare la GUI '" + name + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
        return false;
    }

    private void setFromResultSet(ResultSet resultSet) throws SQLException {
        this.title = resultSet.getString("guiTitle");
        this.items.clear();
        String guiItems = resultSet.getString("guiItems");
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        Gson gson = new Gson();
        HashMap<String, String> serializedItems = gson.fromJson(guiItems, type);
        for (Map.Entry<String, String> entry : serializedItems.entrySet()) {
            items.put(
                    Integer.parseInt(entry.getKey()),
                    ItemStack.deserializeBytes(Base64.getDecoder().decode(entry.getValue()))
            );
        }
    }
}
