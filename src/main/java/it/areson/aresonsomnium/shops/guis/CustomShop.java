package it.areson.aresonsomnium.shops.guis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.areson.aresonsomnium.database.MySQLObject;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import it.areson.aresonsomnium.shops.items.SerializedShopItem;
import it.areson.aresonsomnium.shops.items.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static it.areson.aresonsomnium.database.MySqlConfig.GUIS_TABLE_NAME;

public class CustomShop extends MySQLObject {

    public static String tableQuery = "create table if not exists %s (" +
            "guiName varchar(255) not null primary key,\n" +
            "guiTitle varchar(255) not null,\n" +
            "shopItems text not null\n" +
            ");";

    private final String name;
    private final TreeMap<Integer, ShopItem> items;
    private String title;

    public CustomShop(String name, String title, MySqlDBConnection mySqlDBConnection) {
        super(mySqlDBConnection, GUIS_TABLE_NAME);
        this.title = title;
        this.name = name;
        this.items = new TreeMap<>();
    }

    public static CustomShop getFromDB(MySqlDBConnection connection, String name) {
        CustomShop customShop = new CustomShop(name, null, connection);
        if (customShop.updateFromDB()) {
            return customShop;
        } else {
            return null;
        }
    }

    public TreeMap<Integer, ShopItem> getItems() {
        return items;
    }

    public Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', title));
        for (Map.Entry<Integer, ShopItem> entry : items.entrySet()) {
            Integer key = entry.getKey();
            ShopItem value = entry.getValue();
            ItemMeta itemMeta = value.getItemStack().getItemMeta();
            if (Objects.nonNull(itemMeta)) {
                List<String> lore = itemMeta.getLore();
                if (Objects.nonNull(lore)) {
                    lore.add("");
                    lore.addAll(value.getPrice().toLore());
                }
            }
            value.getItemStack().setItemMeta(itemMeta);
            inventory.setItem(key, value.getItemStack());
        }
        return inventory;
    }

    @Override
    public void saveToDB() {
        createTableIfNotExists(String.format(tableQuery, tableName));
        String saveQuery = getSaveQuery();
        try {
            Connection connection = mySqlDBConnection.connect();
            int update = mySqlDBConnection.update(connection, saveQuery);
            if (update >= 0) {
                mySqlDBConnection.getDebugger().debugSuccess("Aggiornata GUI '" + name + "' sul DB.");
            } else {
                mySqlDBConnection.getDebugger().debugWarning("GUI '\" + name + \"' NON aggiornata sul DB.");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getDebugger().debugError("Impossibile connettersi per aggiornare la GUI '" + name + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
    }

    public Map<Integer, SerializedShopItem> getSerializedShopItems() {
        return items.entrySet().parallelStream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().toSerializedShopItem()
        ));
    }

    public String getSaveQuery() {
        Gson gson = new Gson();
        String itemsJson = gson.toJson(getSerializedShopItems());
        return String.format("INSERT INTO %s (guiName, guiTitle, shopItems) " +
                        "values ('%s', '%s', '%s') ON DUPLICATE KEY " +
                        "UPDATE guiTitle='%s', shopItems='%s'",
                tableName,
                name, title, itemsJson,
                title, itemsJson
        );
    }

    @Override
    public boolean updateFromDB() {
        createTableIfNotExists(String.format(tableQuery, tableName));
        String query = "select * from " + GUIS_TABLE_NAME + " where guiName='" + name + "'";
        try {
            Connection connection = mySqlDBConnection.connect();
            ResultSet resultSet = mySqlDBConnection.select(connection, query);
            if (resultSet.next()) {
                // Presente
                setFromResultSet(resultSet);
                mySqlDBConnection.getDebugger().debugSuccess("Dati GUI '" + name + "' recuperati dal DB");
                return true;
            } else {
                // Non presente
                mySqlDBConnection.getDebugger().debugWarning("GUI '" + name + "' non presente sul DB");
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlDBConnection.getDebugger().debugError("Impossibile connettersi per recuperare la GUI '" + name + "'");
            mySqlDBConnection.printSqlExceptionDetails(exception);
        }
        return false;
    }

    private void setFromResultSet(ResultSet resultSet) throws SQLException {
        Gson gson = new Gson();
        this.items.clear();
        Type type = new TypeToken<HashMap<Integer, SerializedShopItem>>() {
        }.getType();
        Map<Integer, SerializedShopItem> serializedShopItemMap = gson.fromJson(resultSet.getString("shopItems"), type);
        this.items.putAll(serializedShopItemMap.entrySet().parallelStream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().toShopItem()
        )));
        this.title = resultSet.getString("guiTitle");
    }

    public boolean isShopReady() {
        return items.values().stream().allMatch(value -> value.getPrice().isPriceReady());
    }

}
