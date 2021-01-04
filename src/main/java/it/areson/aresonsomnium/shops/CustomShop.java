package it.areson.aresonsomnium.shops;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.areson.aresonsomnium.database.MySQLObject;
import it.areson.aresonsomnium.database.MySqlDBConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static it.areson.aresonsomnium.database.MySqlConfig.GUIS_TABLE_NAME;

public class CustomShop extends MySQLObject {

    public static String tableQuery = "create table if not exists %s (" +
            "guiName varchar(255) not null primary key,\n" +
            "guiTitle varchar(255) not null,\n" +
            "guiObjects text not null\n" +
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
            ShopItem shopItem = entry.getValue().cloneShopItem();
            ItemMeta itemMeta = shopItem.getItemMeta();
            if (Objects.nonNull(itemMeta)) {
                final List<String> lore = itemMeta.getLore();
                final List<String> loreIfNull = new ArrayList<>();
                if (Objects.nonNull(lore)) {
                    lore.add("non");
                    shopItem.getPriceMap().forEach(((coinType, price) -> lore.add(ChatColor.GOLD + "" + price + " " + ChatColor.BOLD + coinType.getCoinName() + " Coins")));
                    itemMeta.setLore(lore);
                } else {
                    loreIfNull.add("null");
                    shopItem.getPriceMap().forEach(((coinType, price) -> loreIfNull.add(ChatColor.GOLD + "" + price + " " + ChatColor.BOLD + coinType.getCoinName() + " Coins")));
                    itemMeta.setLore(loreIfNull);
                }
                shopItem.setItemMeta(itemMeta);
            } else {
                Bukkit.getLogger().warning("ItemMeta nulli per l'oggetto " + shopItem.toString());
            }
            inventory.setItem(key, shopItem);
        }
        return inventory;
    }

    @Override
    public void saveToDB() {
        createTableIfNotExists(String.format(tableQuery, tableName));
        String saveQuery = getInsertQuery();
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

    public String getInsertQuery() {
        JsonObject itemsJson = new JsonObject();
        for (Map.Entry<Integer, ShopItem> entry : items.entrySet()) {
            String key = entry.getKey().toString();
            ShopItem.SerializedShopItem serializedShopItem = entry.getValue().toSerializedShopItem();
            itemsJson.add(key, serializedShopItem.toJsonElement());
        }

        return String.format("INSERT INTO %s (guiName, guiTitle, guiItems) " +
                        "values ('%s', '%s', '%s') ON DUPLICATE KEY " +
                        "UPDATE guiTitle='%s', guiItems='%s'",
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
                applyResultSet(resultSet);
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

    private void applyResultSet(ResultSet resultSet) throws SQLException {
        this.title = resultSet.getString("guiTitle");
        this.items.clear();
        String guiItems = resultSet.getString("guiItems");
        JsonObject jsonObject = new JsonParser().parse(guiItems).getAsJsonObject();
        jsonObject.entrySet().forEach(entry -> {
            String slotString = entry.getKey();
            ShopItem.SerializedShopItem serializedShopItem = new Gson().fromJson(entry.getValue(), ShopItem.SerializedShopItem.class);
            ShopItem shopItem = serializedShopItem.toShopItem();
            try {
                int slot = Integer.parseInt(slotString);
                items.put(slot, shopItem);
            } catch (NumberFormatException e) {
                Bukkit.getLogger().severe("Slot non valido oggetto " + shopItem.toString());
                e.printStackTrace();
            }
        });
    }

    public boolean isShopReady() {
        return items.values().stream().noneMatch(value -> value.getPriceMap().values().stream().anyMatch(price -> price <= 0));
    }
}
